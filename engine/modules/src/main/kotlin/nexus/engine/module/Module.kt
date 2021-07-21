package nexus.engine.module

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import mu.KotlinLogging
import nexus.engine.module.naming.Name
import nexus.engine.module.naming.Version
import nexus.engine.module.resources.ModuleFileSource
import org.reflections.Reflections
import java.io.File
import kotlin.reflect.KClass

/**
 * A module is an identified and versioned set of code and/or resources that can be loaded and used at runtime. This class encapsulates information on a
 * module.
 */
class Module(
    /**
     * The metadata describing the module
     */
    val metadata: ModuleMetadata,

    /**
     * This is used to build the manifest of the module. the [ModuleMetadata] should provide package or "namespace" as I
     * call it, this means we can scan the classpath using that root package recursively to locate all of the
     * classes that belong to this module
     */
    val moduleManifest: Reflections,
    /**
     * Any sources of files that compose the module. Must not be null - can be {@link EmptyFileSource}
     */
    val resources: ModuleFileSource,
    /**
     * Predicate to determine what classes to include from the main classpath (classes from the unloaded classpaths will be included automatically)
     */
    val classPredicate: (klass: KClass<*>) -> Boolean,
    /**
     * A collection of extran files to load onto the class path. This could be api class files etc.
     */
    classpaths: Collection<File>,
) {

    /**
     * A collection of extran files to load onto the class path. This could be api class files etc.
     */
    val classpaths: List<File> = ImmutableList.copyOf(classpaths)


    /**
     * @return The list of permission sets required by this module
     */
    val requiredPermissions: ImmutableSet<String> get() = ImmutableSet.copyOf(metadata.requiredPermissions)

    /**
     * The identifier for the module
     */
    val id: Name
        get() = metadata.id

    /**
     * The Version for the module
     */
    val version: Version
        get() = metadata.version


    companion object {
        private val logger = KotlinLogging.logger { }
    }
}