package nexus.engine.assets.format

import nexus.engine.assets.AssetData
import nexus.engine.resource.InvalidAssetFilenameException
import nexus.engine.resource.Name
import nexus.engine.resource.name
import java.util.function.Predicate


/**
 * A base implementation of [AssetFileFormat][org.terasology.gestalt.assets.format.AssetFileFormat] that will handle files with specified file extensions.
 * The name of the corresponding asset is assumed to be the non-extension part of the file name.
 *
 * @author Immortius
 */
abstract class AbstractAssetFileFormat<T : AssetData> : AssetFileFormat<T> {
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
