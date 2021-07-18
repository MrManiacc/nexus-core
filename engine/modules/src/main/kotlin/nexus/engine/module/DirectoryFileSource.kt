package nexus.engine.module

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import com.google.common.collect.Queues
import nexus.engine.module.resources.FileReference
import nexus.engine.module.resources.ModuleFileSource
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.file.Path
import java.util.*
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Collectors


/**
 * A ModuleFileSource that reads files from a directory on the file system, using Java's [File] class.
 */
class DirectoryFileSource @JvmOverloads constructor(
    directory: File,
    val filter: Predicate<File> = Predicate { x: File ->
        !x.name.endsWith(".class")
    },
) :
    ModuleFileSource {
    private var rootDirectory: File = directory.canonicalFile ?: error("Failed to canonical file $directory")

    override val files: Collection<FileReference> =
        Lists.newArrayList(DirectoryIterator(rootDirectory, rootDirectory, filter, true))

    override fun getFile(filepath: List<String>): Optional<FileReference> {
        val file = buildFilePath(filepath)
        try {
            if (!file.canonicalPath.startsWith(rootDirectory.path)) {
                return Optional.empty<FileReference>()
            }
        } catch (e: IOException) {
            return Optional.empty<FileReference>()
        }
        return if (file.isFile && filter.test(file)) {
            Optional.of(DirectoryFileReference(file, rootDirectory))
        } else Optional.empty<FileReference>()
    }

    private fun buildFilePath(filepath: List<String>): File {
        var file = rootDirectory
        for (part in filepath) {
            file = File(file, part)
        }
        return file
    }

    override fun getFilesInPath(recursive: Boolean, path: List<String>): Collection<FileReference> {
        val dir = buildFilePath(path)
        try {
            if (!dir.canonicalPath.startsWith(rootDirectory.path)) {
                return emptyList<FileReference>()
            }
        } catch (e: IOException) {
            return emptyList<FileReference>()
        }
        return Lists.newArrayList(DirectoryIterator(rootDirectory, dir, filter, recursive))
    }

    override fun getSubpaths(fromPath: List<String>): Set<String> {
        val dir = buildFilePath(fromPath)
        try {
            if (dir.canonicalPath.startsWith(rootDirectory.path)) {
                val contents = dir.listFiles()
                if (contents != null) {
                    return Arrays.stream(contents).filter { obj: File -> obj.isDirectory }
                        .map { obj: File -> obj.name }.collect(Collectors.toSet())
                }
            }
        } catch (e: IOException) {
            logger.error("Failed to canonicalize path", e)
        }
        return emptySet()
    }

    override val rootPaths: List<Path>
        get() = listOf(rootDirectory.toPath())

    override operator fun iterator(): Iterator<FileReference> {
        return DirectoryIterator(rootDirectory, rootDirectory, filter, true)
    }

    class DirectoryFileReference(private val file: File, private val baseDirectory: File) : FileReference {
        override val name: String
            get() = file.name
        override val path: List<String>
            get() = try {
                val filePath = file.parentFile.canonicalPath
                val basePath = baseDirectory.path
                Arrays.asList(*filePath.substring(basePath.length + 1).split(Pattern.quote(File.separator))
                    .toTypedArray())
            } catch (e: IOException) {
                logger.warn("Failed to canonicalize path", e)
                emptyList()
            }
        override val fullName: String
            get() = file.path

        override @Throws(IOException::class) fun open(): InputStream {
            return BufferedInputStream(FileInputStream(file))
        }

        override fun toString(): String {
            return name
        }

        override fun hashCode(): Int {
            return file.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }
            if (other is DirectoryFileReference) {
                return other.file == file
            }
            return false
        }
    }

    private class DirectoryIterator internal constructor(
        private val rootDirectory: File,
        baseDirectory: File,
        private val filter: Predicate<File>,
        private val recursive: Boolean,
    ) : Iterator<FileReference> {
        private val files: Deque<File> = Queues.newArrayDeque()
        private var next: FileReference? = null
        private fun findNext() {
            next = null
            while (next == null && !files.isEmpty()) {
                val file = files.pop()
                if (file.isDirectory && recursive) {
                    addDirectoryContentsToQueue(file)
                } else if (file.isFile && filter.test(file)) {
                    next = DirectoryFileReference(file, rootDirectory)
                }
            }
        }

        private fun addDirectoryContentsToQueue(file: File?) {
            val contents = file!!.listFiles()
            if (contents != null) {
                files.addAll(Arrays.asList(*contents))
            }
        }

        override fun hasNext(): Boolean {
            return next != null
        }

        override fun next(): FileReference {
            val result: FileReference? = next
            findNext()
            return result ?: error("failed to find next directory for directory iterator!")
        }

        init {
            addDirectoryContentsToQueue(baseDirectory)
            findNext()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DirectoryFileSource::class.java)
    }
    /**
     * Creates a DirectoryFileSource
     *
     * @param directory     The directory to read resources from
     * @param contentFilter A predicate to filter which files are exposed
     */
    /**
     * Creates a standard DirectoryFileSource, excluding all .class files from the given directory
     *
     * @param directory The directory to read resources from
     */
    init {
        Preconditions.checkArgument(directory.isDirectory, "Not a directory")
    }
}
