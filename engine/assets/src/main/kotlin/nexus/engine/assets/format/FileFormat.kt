package nexus.engine.assets.format

import nexus.engine.module.ex.InvalidAssetFilenameException
import nexus.engine.module.naming.Name
import nexus.engine.module.resources.FileReference
import java.util.function.Predicate

/**
 * Common base interface for all file formats.  A file format is used to load one or more files and either create or modify an
 * [AssetData][org.terasology.gestalt.assets.AssetData].
 *
 * 
 */
interface FileFormat {
    /**
     * @return A path matcher that will filter for files relevant for this format.
     */
    val fileMatcher: Predicate<FileReference>

    /**
     * This method is use to obtain the name of the resource represented by the given filename. The ModuleAssetDataProducer will combine it with a module id to
     * determine the complete ResourceUrn.
     *
     * @param filename The filename of an asset, including extension
     * @return The asset name corresponding to the given filename
     * @throws org.terasology.gestalt.assets.exceptions.InvalidAssetFilenameException if the filename is not valid for this format.
     */
    @Throws(InvalidAssetFilenameException::class) fun getAssetName(filename: String): Name
}
