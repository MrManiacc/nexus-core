package nexus.engine.assets.format

import mu.KotlinLogging
import nexus.engine.assets.AssetData
import nexus.engine.module.ex.InvalidAssetFilenameException
import nexus.engine.module.naming.Name
import nexus.engine.module.naming.name
import nexus.engine.module.resources.FileReference
import java.util.function.Predicate


/**
 * A base implementation of [AssetFileFormat][org.terasology.gestalt.assets.format.AssetFileFormat] that will handle files with specified file extensions.
 * The name of the corresponding asset is assumed to be the non-extension part of the file name.
 *
 */
abstract class AbstractAssetFileFormat<T : AssetData> : AssetFileFormat<T> {
    protected val logger = KotlinLogging.logger {  }
    override var fileMatcher: Predicate<FileReference>
        protected set

    /**
     * @param fileExtension  A file extension that this file format will handle
     * @param fileExtensions Additional file extensions that this file format will handle
     */
    constructor(fileExtension: String, vararg fileExtensions: String) {
        fileMatcher = createFileExtensionPredicate(fileExtensions.toMutableList().apply { add(fileExtension) })
    }

    constructor(fileMatcher: Predicate<FileReference>) {
        this.fileMatcher = fileMatcher
    }

    private fun createFileExtensionPredicate(extensions: List<String>): Predicate<FileReference> {
        return extensions.stream().map { ext: String -> ".$ext" }.map { x: String ->
            Predicate { moduleFile: FileReference ->
                moduleFile.name.endsWith(x)
            }
        }.reduce(
            Predicate { x: FileReference -> false }
        ) { obj: Predicate<FileReference>, other: Predicate<FileReference> -> obj.or(other) }
    }

    /**
     * This should return the asset's name without the extension
     */
    @Throws(InvalidAssetFilenameException::class) override fun getAssetName(filename: String): Name =
        name(filename.substringBeforeLast(".", filename))
}
