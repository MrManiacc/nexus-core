package nexus.engine.module

import com.google.common.collect.Sets
import nexus.engine.module.naming.Name
import nexus.engine.module.sandbox.JavaModuleClassloader
import nexus.engine.module.sandbox.PermissionProviderFactory
import nexus.engine.module.sandbox.StandardPermissionProviderFactory
import nexus.engine.module.sandbox.WarnOnlyProviderFactory
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.io.File
import java.io.IOException
import java.lang.Boolean
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.stream.Collectors
import kotlin.RuntimeException
import kotlin.reflect.KClass

/**
 * This is where all of the dirty internal work happens...
 */
class ModuleManager {
    private val registry: ModuleRegistry = TableModuleRegistry()
    private val metadataReader: ModuleMetadataYamlAdapater = newMetadataReader()
    private val moduleFactory: ModuleFactory = ModuleManager.newModuleFactory(metadataReader)
    private val permissionProviderFactory = StandardPermissionProviderFactory()
    private val wrappingPermissionProviderFactory: PermissionProviderFactory =
        WarnOnlyProviderFactory(permissionProviderFactory)

    /**This is used for all kinds of magic, this wraps around all of the modules**/
    private lateinit var environment: ModuleEnvironment
    private var engineModule: Module = loadAndConfigureEngineModule(moduleFactory, emptyList())

    /** Create a ModuleFactory configured for Terasology modules.  */
    private fun newModuleFactory(metadataReader: ModuleMetadataYamlAdapater): ModuleFactory {
        val moduleFactory: ModuleFactory
        if (Boolean.getBoolean(ModuleManager.LOAD_CLASSPATH_MODULES_PROPERTY)) {
            moduleFactory = ClasspathCompromisingModuleFactory()
        } else {
            moduleFactory = ModuleFactory()
        }
        moduleFactory.setDefaultLibsSubpath("build/libs")
        val mmlm = moduleFactory.getModuleMetadataLoaderMap()
        mmlm.put(MODULE_INFO_FILENAME, metadataReader)
        return moduleFactory
    }

    fun loadEnvironment(
        modules: Set<Module>,
        asPrimary: kotlin.Boolean,
        permissions: kotlin.Boolean,
    ): ModuleEnvironment {
        val finalModules: MutableSet<Module> = Sets.newLinkedHashSet(modules)
        finalModules.add(engineModule)
        val newEnvironment: ModuleEnvironment = if (permissions) {
            ModuleEnvironment(modules,
                wrappingPermissionProviderFactory,
                ModuleEnvironment.ClassLoaderSupplier { module, parent, permissionProvider ->
                    JavaModuleClassloader.create(module, parent, permissionProvider)
                }, ModuleEnvironment::class.java.classLoader)
        } else {
            ModuleEnvironment(finalModules,
                permissionProviderFactory,
                ModuleEnvironment.ClassLoaderSupplier { module, parent, permissionProvider ->
                    JavaModuleClassloader.create(module, parent, permissionProvider)
                },
                ModuleEnvironment::class.java.classLoader)
        }
        if (asPrimary) {
            environment = newEnvironment
        }
        return newEnvironment
    }

    /**
     * Load and configure the engine module.
     *
     *
     * The engine module is the parts of the engine which are available to be called directly
     * from other modules. Unlike other modules, engine classes are on the classpath and not
     * restricted by the ModuleClassLoader.
     *
     *
     * This function is static so it can be tested without needing a ModuleManager instance.
     *
     * @param moduleFactory used to create the module
     * @param classesOnClasspathsToAddToEngine added to the module's reflections manifest
     */
    fun loadAndConfigureEngineModule(
        moduleFactory: ModuleFactory,
        classesOnClasspathsToAddToEngine: List<KClass<*>>,
    ): Module {
        // Start by creating a gestalt Module for the Java package `org.terasology.engine`.
        val packageModule: Module = moduleFactory.createPackageModule("nexus.engine")

        // We need to add reflections from our subsystems and other classes.
        val packageReflections: Reflections = packageModule.moduleManifest
        val config: ConfigurationBuilder = reflectionsConfigurationFrom(packageReflections)
        val classPaths: MutableCollection<File> = HashSet(packageModule.classpaths)
        for (aClass in classesOnClasspathsToAddToEngine) {
            val url: URL = ClasspathHelper.forClass(aClass.java)
            config.addUrls(url) // include this in reflections scan
            classPaths.add(urlToFile(url)) // also include in Module.moduleClasspaths
        }
        if (!config.getUrls().isEmpty()) {
            val reflectionsWithSubsystems = Reflections(config)
            packageReflections.merge(reflectionsWithSubsystems)
        }

        // We need the class predicate to include classes in subsystems and whatnot. We can't change it in an
        // existing module, so make a new one based on the one from the factory.
        // TODO: expand the ModuleFactory interface to make this whole thing less awkward
        return Module(
            metadata = packageModule.metadata,
            resources = packageModule.resources,
            classpaths = classPaths,
            moduleManifest = packageReflections,
            classPredicate = { clazz ->
                (packageModule.classPredicate(clazz)
                        || config.urls.contains(ClasspathHelper.forClass(clazz.java)))
            }
        )
    }


    /**
     * Ensure all modules declare a dependency on the engine module.
     *
     *
     * This is to ensure that the set of modules is a graph with a single root.
     * We need this to ensure the engine is loaded *before* other modules
     * when things iterate over the module list in dependency order.
     *
     *
     * See [#1450](https://github.com/MovingBlocks/Terasology/issues/1450).
     */
    private fun ensureModulesDependOnEngine() {
        val engineDep = DependencyInfo()
        engineDep.setId(engineModule.id)
        engineDep.setMinVersion(engineModule.version)
        val engineModules: MutableSet<Name> = Sets.newHashSet(engineModule.id)
        engineModules.addAll(engineModule.metadata.dependencies.stream().map(DependencyInfo::getId).collect(
            Collectors.toList()))
        registry.stream()
            .filter { mod -> !engineModules.contains(mod.id) }
            .forEach { mod -> mod.metadata.dependencies.add(engineDep) }
    }


    private fun urlToFile(url: URL): File {
        var fileUrl = url
        if (url.protocol == "jar") {
            fileUrl = try {
                val connection = url.openConnection() as JarURLConnection
                connection.jarFileURL
                // despite the method name, openConnection doesn't open anything unless we
                // call connect(), so we needn't clean up anything here.
            } catch (e: IOException) {
                throw RuntimeException("Failed to get file from $url")
            }
        }
        return try {
            File(fileUrl.toURI())
        } catch (e: URISyntaxException) {
            throw RuntimeException("Failed to get file from $url", e)
        }
    }

    fun reflectionsConfigurationFrom(packageReflections: Reflections): ConfigurationBuilder {
        val config = ConfigurationBuilder()
        val scanners = packageReflections.configuration.scanners
        config.setScanners(*scanners.toTypedArray())
        return config
    }


    companion object {
        const val MODULE_INFO_FILENAME = "module.yaml"

        private fun newModuleFactory(metadataReader: ModuleMetadataYamlAdapater): ModuleFactory {
            val moduleFactory: ModuleFactory
            moduleFactory = if (Boolean.getBoolean(LOAD_CLASSPATH_MODULES_PROPERTY)) {
                ClasspathCompromisingModuleFactory()
            } else {
                ModuleFactory()
            }
            moduleFactory.setDefaultLibsSubpath("build/libs")
            val mmlm = moduleFactory.getModuleMetadataLoaderMap()
            mmlm.put(MODULE_INFO_FILENAME, metadataReader)
            return moduleFactory
        }

        private fun newMetadataReader(): ModuleMetadataYamlAdapater {
            val metadataJsonAdapter = ModuleMetadataYamlAdapater()
//            for (ext in StandardModuleExtension.values()) {
//                metadataJsonAdapter.registerExtension(ext.getKey(), ext.getValueType())
//            }
//            for (ext in ExtraDataModuleExtension.values()) {
//                metadataJsonAdapter.registerExtension(ext.getKey(), ext.getValueType())
//            }
            return metadataJsonAdapter
        }


        const val LOAD_CLASSPATH_MODULES_PROPERTY = "nexus.engine.load_classpath_modules"

    }
}