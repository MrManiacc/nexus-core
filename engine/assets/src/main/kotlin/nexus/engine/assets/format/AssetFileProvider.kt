package nexus.engine.assets.format

import nexus.engine.reflection.combineToList
import java.nio.file.Path
import java.util.*


/**
 * ModuleFileSource provides an interface for all providers of files (resources) that are part
 * of the content of a Module. This includes file discovery and reading.
 *
 *
 * As a number of different mechanisms can be used to provide files, ModuleFileSource provides a
 * simplified view, where:
 *
 *  * A file path is a list of strings, each representing a folder or step in the path
 *  * It is possible to discover what paths are within a path
 *  * It is possible to discover what files are within a path, optionally recursively
 *  * A file can be streamed
 *
 *
 *
 * Paths are represented as a [List] of String path elements, where each element is a
 * directory or file. For example, if the ModuleFileSource is reading from a directory
 * "content/stuff/blurg", this would be represented as the path ["content", "stuff", "blurg"]
 */
interface AssetFileProvider : Iterable<FileReference> {
    /**
     * Obtain the handle to a specific file. The file path should be provided as one or more
     * string elements that together compose the path
     *
     * @param path     The path to the file
     * @param morePath More path to the file
     * @return The requested file, or [Optional.empty] if it doesn't exist
     */
    fun getFile(path: String, vararg morePath: String): Optional<FileReference> {
        return getFile(combineToList(path, *morePath))
    }

    /**
     * Obtain the handle to a specific file
     *
     * @param filepath The path to the file. Should not be empty
     * @return The requested file, or [Optional.empty] if it doesn't exist
     */
    fun getFile(filepath: List<String>): Optional<FileReference>


    /**
     * @return A collection of all files provided by this ModuleFileSource
     */
    val files: Collection<FileReference>
        get() = getFilesInPath(true)

    /**
     * Finds all files within a path
     *
     * @param recursive Whether to recurse through subpaths
     * @param path      The path to search
     * @return A collection of handles to all files in the give path
     */
    fun getFilesInPath(recursive: Boolean, vararg path: String): Collection<FileReference> =
        getFilesInPath(recursive, listOf(*path))


    /**
     * Finds all files within a path
     *
     * @param recursive Whether to recurse through subpaths
     * @param path      The path to search
     * @return A collection of handles to all files in the give path
     */
    fun getFilesInPath(recursive: Boolean, path: List<String>): Collection<FileReference>

    /**
     * Finds all subpaths in the given path
     *
     * @param fromPath The path to search
     * @return A list of the immediate subpaths in the given path
     */
    fun getSubpaths(vararg fromPath: String): Set<String> = getSubpaths(listOf(*fromPath))


    /**
     * Finds all subpaths in the given path
     *
     * @param fromPath The path to search
     * @return A list of the immediate subpaths in the given path
     */
    fun getSubpaths(fromPath: List<String>): Set<String>

    /**
     * @return A list of all the root paths of this file source, that
     */
    val rootPaths: List<Path> get() = emptyList()
}
