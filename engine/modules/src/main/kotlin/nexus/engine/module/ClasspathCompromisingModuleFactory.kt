package nexus.engine.module

import com.google.common.base.MoreObjects
import com.google.common.collect.ImmutableSet
import org.reflections.util.ClasspathHelper
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.function.Predicate
import kotlin.reflect.KClass

/**
 * Creates modules that can own classes that were loaded without a ModuleClassLoader.
 * <p>
 * When {@link ModuleEnvironment#getModuleProviding(Class)} checks modules built using the default
 * ModuleFactory, it will only acknowledge the class as belonging to that module if it was loaded
 * using the module's ModuleClassLoader.
 * <p>
 * This factory will recognize classes as belonging to the module as long as that class's source
 * location is within the module's directory or archive.
 * <p>
 * ⚠ Usually <em>checking the classloader is sufficient</em> and thus you
 * should <em>not</em> find the need to use this in production code. It's useful in cases where
 * the module <em>cannot</em> be loaded using a ModuleClassLoader (e.g. a test runner) and it's
 * acceptable to run without the protections ModuleClassLoader provides.
 */
class ClasspathCompromisingModuleFactory : ModuleFactory() {
    override fun createDirectoryModule(metadata: ModuleMetadata, directory: File): Module {
        val module: Module = super.createDirectoryModule(metadata, directory)
        val classIn = ClassesInModule(module)

        return Module(
            metadata = module.metadata,
            resources = module.resources,
            classpaths = module.classpaths,
            moduleManifest = module.moduleManifest,
            classPredicate = { x -> classIn.test(x) })
    }

    override @Throws(IOException::class) fun createArchiveModule(metadata: ModuleMetadata, archive: File): Module {
        val module: Module = super.createArchiveModule(metadata, archive)
        val classIn = ClassesInModule(module)

        return Module(
            metadata = module.metadata, resources = module.resources,
            classpaths = module.classpaths, moduleManifest = module.moduleManifest,
            classPredicate = { x -> classIn.test(x) }
        )
    }

    internal class ClassesInModule(module: Module) :
        Predicate<KClass<*>> {
        private val classpaths: Set<URL>
        private val classLoaders: Array<ClassLoader>
        private val name: String

        override fun test(aClass: KClass<*>): Boolean {
            val classUrl: URL = ClasspathHelper.forClass(aClass.java, *classLoaders)
            return classpaths.contains(classUrl)
        }

        override fun toString(): String {
            return MoreObjects.toStringHelper(this)
                .add("name", name)
                .toString()
        }

        init {
            classpaths = module.classpaths.stream().map { f ->
                try {
                    val url: URL = f.toURI().toURL()
                    if (f.getName().endsWith(".jar")) {
                        // Code from jars has a `jar:` URL.
                        return@map URL("jar", null, "$url!/")
                    }
                    return@map url
                } catch (e: MalformedURLException) {
                    throw RuntimeException(e)
                }
            }.collect(ImmutableSet.toImmutableSet())
            classLoaders = module.moduleManifest.configuration.classLoaders ?: emptyArray()
            name = module.id.toString()
        }
    }
}