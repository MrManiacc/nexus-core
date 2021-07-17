package nexus.engine.assets.format

import nexus.engine.assets.AssetData
import nexus.engine.resource.ResourceUrn
import java.io.IOException


/**
 * An AssetFileFormat handles loading a file representation of an asset into the appropriate [AssetData][org.terasology.gestalt.assets.AssetData].
 *
 * @author Immortius
 */
interface AssetFileFormat<T : AssetData> : FileFormat {
    /**
     * Creates an [AssetData][org.terasology.gestalt.assets.AssetData] from one or more files.
     *
     * @param urn    The urn identifying the asset being loaded.
     * @param inputs The inputs corresponding to this asset
     * @return The loaded asset
     * @throws IOException If there are any errors loading the asset
     */
    @Throws(IOException::class) fun load(urn: ResourceUrn, inputs: List<AssetDataFile>): T
}
