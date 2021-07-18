package nexus.engine.module

import nexus.engine.module.resources.FileReference
import nexus.engine.module.resources.ModuleFileSource
import nexus.engine.module.utilities.combineToList
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors


/**
 * CompositeFileSource combines multiple ModuleFileSources together to act as a single ModuleFileSource
 */
class CompositeFileSource(private val sources: List<ModuleFileSource>) : ModuleFileSource {

    constructor(source: ModuleFileSource, vararg sources: ModuleFileSource) : this(combineToList(source, *sources))

    override fun getFile(filepath: List<String>): Optional<FileReference> {
        for (source: ModuleFileSource in sources) {
            val result: Optional<FileReference> = source.getFile(filepath)
            if (result.isPresent) {
                return result
            }
        }
        return Optional.empty<FileReference>()
    }

    override val files: Collection<FileReference>
        get() = sources.stream().flatMap { x: ModuleFileSource ->
            x.files.stream()
        }.collect(Collectors.toList())


    override fun getFilesInPath(recursive: Boolean, path: List<String>): Collection<FileReference> {
        return sources.stream().flatMap { x: ModuleFileSource ->
            x.getFilesInPath(recursive,
                path).stream()
        }.collect(Collectors.toList())
    }

    override fun getSubpaths(fromPath: List<String>): Set<String> {
        return sources.stream().flatMap { x: ModuleFileSource ->
            x.getSubpaths(fromPath).stream()
        }.collect(Collectors.toSet())
    }

    override fun iterator(): Iterator<FileReference> {
        return sources.stream().flatMap { x: ModuleFileSource ->
            x.files.stream()
        }.iterator()
    }

    override val rootPaths: List<Path>
        get() {
            return sources.stream().flatMap { x: ModuleFileSource ->
                x.rootPaths.stream()
            }.collect(Collectors.toList())
        }
}
