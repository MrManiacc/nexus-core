package nexus.engine.module

import com.google.common.base.Joiner
import io.github.classgraph.Resource
import nexus.engine.module.resources.FileReference
import nexus.engine.module.resources.ModuleFileSource
import org.reflections.Reflections
import java.io.InputStream
import java.util.*
import java.util.function.Function

/**
 * This will scan all of the classes on the class path using classgraph for classes annotated with [RegisterAssetType]
 */
class ClasspathFileSource(
    /**
     *  [manifest] - A reflections manifest indicating what files are available on the classpath
     */
    val manifest: Reflections,
    /**
     *  [basePath] -  A subpath in the classpath to expose resources from
     */
    basePath: String = CLASS_PATH_SEPARATOR,
    /**
     *  [classLoader] -  The classloader to use to access resources
     */
    val classLoader: ClassLoader = ClassLoader.getSystemClassLoader(),
) : ModuleFileSource {

    /**
     * This is used to create the full path recursively
     */
    var path: String
        private set

    /**
     * Obtain the handle to a specific file
     *
     * @param filepath The path to the file. Should not be empty
     * @return The requested file, or [Optional.empty] if it doesn't exist
     */
    override fun getFile(filepath: List<String>): Optional<FileReference> {
        val path = this.path + CLASS_PATH_JOINER.join(filepath)
        return manifest.getResources { x: String? -> true }.stream().filter { anObject: String? ->
            path == anObject
        }.map(Function<String, FileReference> { x: String ->
            ClasspathSourceFileReference(x,
                extractSubpath(x),
                classLoader)
        }).findAny()
    }


    /**
     * Finds all files within a path
     *
     * @param recursive Whether to recurse through subpaths
     * @param path      The path to search
     * @return A collection of handles to all files in the give path
     */
    override fun getFilesInPath(recursive: Boolean, path: List<String>): Collection<FileReference> {
        val fullPath = buildPathString(path)
        return manifest.getResources { true }.stream().filter { x: String ->
            x.startsWith(fullPath) && (recursive || !x.substring(
                fullPath.length)
                .contains(CLASS_PATH_SEPARATOR))
        }.map { x: String ->
            ClasspathSourceFileReference(
                x, extractSubpath(
                    x), classLoader)
        }.toList()
    }


    /**
     * Finds all subpaths in the given path
     *
     * @param fromPath The path to search
     * @return A list of the immediate subpaths in the given path
     */
    override fun getSubpaths(fromPath: List<String>): Set<String> {
        val fullPath = buildPathString(fromPath)
        return manifest.getResources { true }.stream().filter { x: String ->
            x.startsWith(fullPath) && x.substring(
                fullPath.length)
                .contains(CLASS_PATH_SEPARATOR)
        }.map { x: String ->
            val subpath = x.substring(fullPath.length)
            subpath.substring(0,
                subpath.indexOf(CLASS_PATH_SEPARATOR))
        }.toList().toSet()
    }

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator(): Iterator<FileReference> = this.files.iterator()

    /**
     * This returns the offset path
     * @param path the path to extra the offset for
     * @return a path that has been offset by the given length
     */
    private fun extractSubpath(path: String): String = path.substring(this.path.length)

    /**
     * This combines all of the strings and builds it into an output string
     */
    private fun buildPathString(path: List<String>): String {
        val fullPath: String
        if (path.isEmpty() || path.size == 1 && path[0].isEmpty()) {
            fullPath = this.path
        } else {
            fullPath = this.path + CLASS_PATH_JOINER.join(path) + CLASS_PATH_SEPARATOR
        }
        return fullPath
    }

    init {
        path = basePath
        if (path.isNotBlank() && !path.endsWith(CLASS_PATH_SEPARATOR))
            path += CLASS_PATH_SEPARATOR
        if (path.startsWith(CLASS_PATH_SEPARATOR))
            path = path.substring(1)

    }

    /**
     * This uses a [Resource] from class graph for it's internal manipulation
     */
    class ClasspathSourceFileReference internal constructor(
        resourcePath: String,
        private val subpath: String,
        private val classLoader: ClassLoader,
    ) :
        FileReference {
        private val myPath: String = resourcePath

        override val name: String
            get() {
                return subpath.substring(subpath.lastIndexOf(CLASS_PATH_SEPARATOR) + 1)
            }

        /**
         * @return The name of the file
         */
        override val fullName: String = subpath

        override val path: List<String>
            get() {
                val parts = listOf(*subpath.split(CLASS_PATH_SEPARATOR)
                    .toTypedArray())
                return parts.subList(0, parts.size - 1)
            }

        override fun open(): InputStream {
            return classLoader.getResourceAsStream(myPath) ?: error("failed to open resource stream for: ${name}")
        }

        override fun toString(): String {
            return name
        }

        override fun hashCode(): Int {
            return Objects.hash(myPath, classLoader)
        }

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }
            if (other is ClasspathSourceFileReference) {
                val other = other
                return other.myPath == myPath && other.classLoader == classLoader
            }
            return false
        }

    }

    companion object {
        const val CLASS_PATH_SEPARATOR = "/"
        private val CLASS_PATH_JOINER: Joiner = Joiner.on(CLASS_PATH_SEPARATOR)
    }

}