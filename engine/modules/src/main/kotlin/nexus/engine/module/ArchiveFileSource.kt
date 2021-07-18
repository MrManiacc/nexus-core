package nexus.engine.module

import com.google.common.base.Joiner
import com.google.common.collect.HashMultimap
import com.google.common.collect.Maps
import com.google.common.collect.SetMultimap
import nexus.engine.module.resources.FileReference
import nexus.engine.module.resources.ModuleFileSource
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * ModuleFileSource that exposes the content of an archive file (zip/jar, etc)
 */
class ArchiveFileSource(file: File, contentsFilter: Predicate<String>, vararg subpath: String) :
    ModuleFileSource {
    private val contents: MutableMap<String, FileReference> = Maps.newLinkedHashMap()
    private val subpaths: SetMultimap<List<String>, String> = HashMultimap.create()

    /**
     * Creates an archive file source over the given archive file. All .class files in the archive
     * will be ignored
     *
     * @param file    The archive file to load
     * @param subpath The path within the archive to use as the base of the exposed files
     * @throws IOException If there is any issue reading the archive file
     */
    constructor(file: File, vararg subpath: String) : this(file,
        Predicate<String> { x: String ->
            !x.endsWith(".class")
        }, *subpath) {
    }

    private fun buildPathString(subpath: List<String?>): String {
        val basePathBuilder = StringBuilder()
        PATH_JOINER.appendTo(basePathBuilder, subpath)
        if (basePathBuilder.length > 0) {
            basePathBuilder.append(PATH_SEPARATOR)
        }
        return basePathBuilder.toString()
    }

    override fun getFile(filepath: List<String>): Optional<FileReference> {
        return Optional.ofNullable<FileReference>(contents[PATH_JOINER.join(filepath)])
    }

    override val files: Collection<FileReference>
        get() = Collections.unmodifiableCollection(contents.values)

    override fun getFilesInPath(recursive: Boolean, path: List<String>): Collection<FileReference> {
        val basePath = buildPathString(path)
        return contents.entries.stream()
            .filter { x: Map.Entry<String, FileReference> ->
                x.key.startsWith(basePath) && (recursive || !x.key.substring(basePath.length)
                    .contains(PATH_SEPARATOR))
            }
            .map(Function<Map.Entry<String, FileReference>, FileReference> { it.value })
            .toList()
    }

    override fun getSubpaths(fromPath: List<String>): Set<String> {
        return subpaths[fromPath]
    }

    override operator fun iterator(): Iterator<FileReference> {
        return contents.values.iterator()
    }

    private class ArchiveFileReference internal constructor(
        private val zipFile: File,
        private val internalFile: String,
        private val basePath: String,
    ) :
        FileReference {
        override val name: String
            get() {
                val lastPathSeparator = internalFile.lastIndexOf(PATH_SEPARATOR)
                return if (lastPathSeparator >= 0) {
                    internalFile.substring(lastPathSeparator + 1)
                } else internalFile
            }

        /**
         * @return The name of the file
         */
        override val fullName: String = zipFile.path

        override val path: List<String>
            get() {
                val parts = Arrays.asList(*internalFile.substring(
                    basePath.length).split(PATH_SEPARATOR).toTypedArray())
                return parts.subList(0, parts.size - 1)
            }

        @Throws(IOException::class) override fun open(): InputStream {
            val zip = ZipFile(zipFile)
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if ((entry.name == internalFile)) {
                    return zip.getInputStream(entry)
                }
            }
            throw FileNotFoundException("Could not find file " + internalFile + " in " + zipFile.path)
        }

        override fun toString(): String {
            return name
        }

        override fun hashCode(): Int {
            return Objects.hash(zipFile, internalFile, basePath)
        }

        override fun equals(o: Any?): Boolean {
            if (o === this) {
                return true
            }
            if (o is ArchiveFileReference) {
                val other = o
                return (zipFile == other.zipFile) && (internalFile == other.internalFile) && (basePath == other.basePath)
            }
            return false
        }
    }

    companion object {
        private val PATH_SEPARATOR = "/"
        private val PATH_JOINER = Joiner.on(PATH_SEPARATOR)
    }

    /**
     * Creates an archive file source over the given archive file.
     *
     * @param file           The archive file to load
     * @param contentsFilter A predicate to use to filter what files to expose
     * @param subpath        The path within the archive to use as the base of the exposed files
     * @throws IOException If there is any issue reading the archive file
     */
    init {
        val basePath = buildPathString(Arrays.asList(*subpath))
        ZipFile(file).use { zip ->
            val entries: Enumeration<out ZipEntry> = zip.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry = entries.nextElement()
                if (entry.isDirectory() && entry.getName().startsWith(basePath)) {
                    val pathParts: List<String> =
                        Arrays.asList(*entry.getName().substring(basePath.length)
                            .split(PATH_SEPARATOR).toTypedArray())
                    if (!pathParts.get(0).isEmpty()) {
                        subpaths.put(pathParts.subList(0, pathParts.size - 1), pathParts.get(pathParts.size - 1))
                    }
                } else if (entry.getName().startsWith(basePath)) {
                    val archiveFile: ArchiveFileReference =
                        ArchiveFileReference(file, entry.getName(), basePath)
                    if (contentsFilter.test(archiveFile.name)) {
                        contents.put(entry.getName().substring(basePath.length), archiveFile)
                    }
                }
            }
        }
    }
}