package nexus.engine.module

import com.google.common.collect.*
import nexus.engine.module.naming.Name
import nexus.engine.module.resources.ModuleFileSource
import nexus.engine.module.sandbox.ObtainClassloader
import nexus.engine.module.sandbox.PermissionProvider
import nexus.engine.module.sandbox.PermissionProviderFactory
import org.reflections8.Reflections
import org.reflections8.ReflectionsException
import org.reflections8.scanners.SubTypesScanner
import org.reflections8.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors
import kotlin.reflect.KClass


/**
 * An environment composed of a set of modules. A chain of class loaders is created for each module that isn't on the classpath, such that dependencies appear before
 * dependants. Classes of interest can then be discovered by the types they inherit or annotations they have.
 *
 *
 * When the environment is no longer in use it should be closed - this closes all the class loaders. Memory used by the ClassLoaders will then be available for garbage
 * collection once the last instance of a class loaded from it is freed.
 *
 *
 * @author Immortius
 */
class ModuleEnvironment(
    modules: Set<Module>,
    permissionProviderFactory: PermissionProviderFactory,
    classLoaderSupplier: ClassLoaderSupplier,
    apiClassLoader: ClassLoader,
) :
    AutoCloseable, Iterable<Module> {
    private val modules: ImmutableMap<Name, Module>
    private val apiClassLoader: ClassLoader
    private val finalClassLoader: ClassLoader
    private val managedClassLoaders: ImmutableList<ModuleClassLoader>
    private val moduleDependencies: ImmutableSetMultimap<Name, Name>
    private val fullReflections: Reflections
    private val modulesOrderByDependencies: ImmutableList<Module>
    private val moduleIdsOrderedByDependencies: ImmutableList<Name>
    private val resources: ModuleFileSource


    private fun hasClassContent(module: Module): Boolean {
        return module.moduleManifest.store.getAll(SubTypesScanner::class.java.simpleName, Any::class.java.name)
            .iterator().hasNext()
    }

    /**
     * Builds a map of modules, keyed by id, from an iterable.
     *
     * @param moduleList The list of modules to map
     * @return The final map
     */
    private fun buildModuleMap(moduleList: Iterable<Module>): ImmutableMap<Name, Module> {
        val builder: ImmutableMap.Builder<Name, Module> = ImmutableMap.builder<Name, Module>()
        for (module in moduleList) {
            builder.put(module.id, module)
        }
        return builder.build()
    }

    /**
     * @param module                    The module to determine the classloader for
     * @param parent                    The classloader to parent any new classloader off of
     * @param permissionProviderFactory The provider of api information
     * @return The new module classloader to use for this module, or absent if the parent classloader should be used.
     */
    private fun buildModuleClassLoader(
        module: Module, parent: ClassLoader,
        permissionProviderFactory: PermissionProviderFactory,
        classLoaderSupplier: ClassLoaderSupplier,
    ): ModuleClassLoader {
        val permissionProvider: PermissionProvider =
            permissionProviderFactory.createPermissionProviderFor(module) { x -> false }
        return AccessController.doPrivileged(PrivilegedAction {
            classLoaderSupplier.create(module,
                parent,
                permissionProvider)
        })!!
    }

    /**
     * Builds Reflections information over the entire module environment, combining the information of all individual modules
     *
     * @param reflectionsByModule A map of reflection information for each module
     */
    private fun buildFullReflections(reflectionsByModule: Map<Name, Reflections>): Reflections {
        val fullBuilder: ConfigurationBuilder = ConfigurationBuilder()
            .addClassLoader(apiClassLoader)
            .addClassLoader(finalClassLoader)
        val reflections = Reflections(fullBuilder)
        for (moduleReflection in reflectionsByModule.values) {
            reflections.merge(moduleReflection)
        }
        return reflections
    }

    private fun buildModuleDependencies(): ImmutableSetMultimap<Name, Name> {
        val moduleDependenciesBuilder: SetMultimap<Name, Name> = HashMultimap.create<Name, Name>()
        for (module in modulesOrderedByDependencies) {
            for (dependency in module.metadata.dependencies) {
                moduleDependenciesBuilder.put(module.id, dependency.getId())
                moduleDependenciesBuilder.putAll(module.id, moduleDependenciesBuilder[dependency.getId()])
            }
        }
        return ImmutableSetMultimap.copyOf(moduleDependenciesBuilder)
    }

    private fun calculateModulesOrderedByDependencies(): ImmutableList<Module> {
        val result: MutableList<Module> = Lists.newArrayList()
        val alphabeticallyOrderedModules =
            Lists.newArrayList(modules.values).toSortedSet(Comparator.comparing(Module::id))
        for (module in alphabeticallyOrderedModules) {
            addModuleAfterDependencies(module, result)
        }
        return ImmutableList.copyOf(result)
    }

    private fun addModuleAfterDependencies(module: Module, out: MutableList<Module>) {
        if (!out.contains(module)) {
            module.metadata.dependencies.stream().filter { obj: Any? -> Objects.nonNull(obj) }
                .map(DependencyInfo::getId).sorted().forEach { dependency ->
                    val dependencyModule = modules[dependency]
                    dependencyModule?.let { addModuleAfterDependencies(it, out) }
                }
            out.add(module)
        }
    }

    override fun close() {
        for (classLoader in managedClassLoaders) {
            try {
                classLoader.close()
            } catch (e: IOException) {
                logger.error("Failed to close classLoader for module '" + classLoader.moduleId.toString() + "'", e)
            }
        }
    }

    /**
     * @param id The id of the module to return
     * @return The desired module, or null if it is not part of the environment
     */
    operator fun get(id: Name): Module? {
        return modules[id]
    }

    /**
     * The resulting list is sorted so that dependencies appear before modules that depend on them. Additionally,
     * modules are alphabetically ordered where there are no dependencies.
     *
     * @return A list of modules in the environment, sorted so any dependencies appear before a module
     */
    val modulesOrderedByDependencies: List<Module>
        get() = modulesOrderByDependencies

    /**
     * @return A list of modules in the environment, sorted so any dependencies appear before a module
     */
    fun getModuleIdsOrderedByDependencies(): List<Name> {
        return moduleIdsOrderedByDependencies
    }

    /**
     * Determines the module from which the give class originates from.
     *
     * @param type The type to find the module for
     * @return The module providing the class, or null if it doesn't come from a module.
     */
    fun getModuleProviding(type: KClass<*>): Name? {
        val classLoader: ClassLoader = AccessController.doPrivileged(ObtainClassloader(type.java))
        if (classLoader is ModuleClassLoader) {
            return (classLoader as ModuleClassLoader).moduleId
        }
        for (module in modulesOrderByDependencies) {
            if (module.classPredicate(type)) {
                return module.id
            }
        }
        return null
    }

    /**
     * @param moduleId The id of the module to get the dependencies
     * @return The ids of the dependencies of the desired module
     */
    fun getDependencyNamesOf(moduleId: Name?): Set<Name> {
        return moduleDependencies[moduleId]
    }

    /**
     * @return The available resources across all modules
     */
    fun getResources(): ModuleFileSource {
        return resources
    }

    /**
     * @param type The type to find subtypes of
     * @param <U>  The type to find subtypes of
     * @return A Iterable over all subtypes of type that appear in the module environment
    </U> */
    fun <U> getSubtypesOf(type: Class<U>): Iterable<Class<out U>> {
        return try {
            fullReflections.getSubTypesOf(type)
        } catch (e: ReflectionsException) {
            throw ReflectionsException("Could not obtain subtypes of '$type' - possible subclass without permission", e)
        }
    }

    /**
     * @param type   The type to find subtypes of
     * @param <U>    The type to find subtypes of
     * @param filter A filter to apply to the returned subtypes
     * @return A Iterable over all subtypes of type that appear in the module environment
    </U> */
    fun <U> getSubtypesOf(type: Class<U>?, filter: Predicate<Class<*>?>?): Iterable<Class<out U>> {
        return fullReflections.getSubTypesOf(type).stream().filter(filter).collect(Collectors.toSet())
    }

    /**
     * @param annotation The annotation of interest
     * @return All types in the environment that are either marked by the given annotation, or are subtypes of a type marked with the annotation if the annotation is marked
     * as @Inherited
     */
    fun getTypesAnnotatedWith(annotation: Class<out Annotation?>?): Iterable<Class<*>> {
        return fullReflections.getTypesAnnotatedWith(annotation, true)
    }

    /**
     * @param annotation The annotation of interest
     * @param filter     Further filter on the returned types
     * @return All types in the environment that are either marked by the given annotation, or are subtypes of a type marked with the annotation if the annotation is marked
     * as @Inherited
     */
    fun getTypesAnnotatedWith(annotation: Class<out Annotation>, filter: Predicate<Class<*>>): Iterable<Class<*>> {
        return fullReflections.getTypesAnnotatedWith(annotation, true).stream().filter(filter)
            .collect(Collectors.toSet())
    }

    override fun iterator(): Iterator<Module> {
        return modules.values.iterator()
    }

    fun interface ClassLoaderSupplier {
        fun create(module: Module, parent: ClassLoader, permissionProvider: PermissionProvider): ModuleClassLoader?
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModuleEnvironment::class.java)
    }

    /**
     * @param modules                   The modules this environment should encompass.
     * @param permissionProviderFactory A factory for producing a PermissionProvider for each loaded module
     * @param classLoaderSupplier       A supplier for producing a ModuleClassLoader for a module
     * @param apiClassLoader            The base classloader the module environment should build upon.
     * @throws java.lang.IllegalArgumentException if the Iterable contains multiple modules with the same id.
     */
    init {
        val reflectionsByModule: MutableMap<Name, Reflections> = Maps.newLinkedHashMap<Name, Reflections>()
        this.modules = buildModuleMap(modules)
        this.apiClassLoader = apiClassLoader
        modulesOrderByDependencies = calculateModulesOrderedByDependencies()
        moduleIdsOrderedByDependencies =
            ImmutableList.copyOf(Collections2.transform(modulesOrderByDependencies, { it?.id }))
        val managedClassLoaderListBuilder = ImmutableList.builder<ModuleClassLoader>()
        var lastClassLoader = apiClassLoader
        val orderedModules = modulesOrderedByDependencies
        for (module in orderedModules) {
            if (!module.classpaths.isEmpty() && !hasClassContent(module)) {
                val classLoader =
                    buildModuleClassLoader(module, lastClassLoader, permissionProviderFactory, classLoaderSupplier)
                managedClassLoaderListBuilder.add(classLoader)
                lastClassLoader = classLoader.classLoader
            }
            reflectionsByModule[module.id] = module.moduleManifest
        }
        finalClassLoader = lastClassLoader
        fullReflections = buildFullReflections(reflectionsByModule)
        managedClassLoaders = managedClassLoaderListBuilder.build()
        moduleDependencies = buildModuleDependencies()
        resources = CompositeFileSource(modulesOrderedByDependencies.stream().map(Module::resources).collect(
            Collectors.toList()))
    }
}
