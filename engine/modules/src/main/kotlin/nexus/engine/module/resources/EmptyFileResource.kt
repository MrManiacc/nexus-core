package nexus.engine.module.resources

import java.util.*


/**
 * EmptyFileSource, a null object for when no file source is desired.
 */
class EmptyFileSource : ModuleFileSource {
    override fun getFile(filepath: List<String>): Optional<FileReference> = Optional.empty()

    override fun getFilesInPath(recursive: Boolean, path: List<String>): Collection<FileReference> =  emptyList()

    override fun getSubpaths(fromPath: List<String>): Set<String> = emptySet()

    override fun iterator(): Iterator<FileReference> = Collections.emptyIterator()
}
