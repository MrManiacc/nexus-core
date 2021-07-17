package nexus.engine.assets.mesh

import nexus.engine.assets.RegisterAssetType
import nexus.engine.assets.format.AbstractAssetFileFormat
import nexus.engine.assets.format.AssetDataFile
import nexus.engine.resource.ResourceUrn
import java.util.function.Predicate

class MeshFormat : AbstractAssetFileFormat<MeshData>(
    fileMatcher = Predicate {
        it.name.endsWith(".fbx")
                || it.name.endsWith(".obj")
                || it.name.endsWith(".gltf")
    },
) {
    /**
     * Creates an [AssetData][org.terasology.gestalt.assets.AssetData] from one or more files.
     *
     * @param urn    The urn identifying the asset being loaded.
     * @param inputs The inputs corresponding to this asset
     * @return The loaded asset
     * @throws IOException If there are any errors loading the asset
     */

    override fun load(urn: ResourceUrn, inputs: List<AssetDataFile>): MeshData {
        logger.debug { "urn: $urn, files: ${inputs.joinToString { ", " }}" }
        return MeshData()
    }
}