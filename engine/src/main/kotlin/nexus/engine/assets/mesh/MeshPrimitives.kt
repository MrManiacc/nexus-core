package nexus.engine.assets.mesh

import nexus.engine.assets.format.producer.AssetDataProducer
import nexus.engine.module.naming.ResourceUrn
import nexus.engine.module.naming.urn
import java.util.*

/**
 * This uses the asset manager to get the mesh asset type, and produce the correct data using asssimp
 */
class MeshPrimitives : AssetDataProducer<MeshData> {
    override val availableAssetUrns: Set<ResourceUrn> = hashSetOf(QuadUrn, CubeUrn, SphereUrn)

    /**
     * This should be used to generate/load asset data for a given urn. This allows us to ability of using kotlins nice
     * unit's for easy creation of asset data. This allow for matching of certain types based upon the [urn]
     *
     * @param urn The urn to get AssetData for
     * @return An optional with the AssetData, if available
     * @throws IOException If there is an error producing the AssetData.
     */
    override fun produce(urn: ResourceUrn): Optional<MeshData> =
        when (urn) {
            QuadUrn -> Optional.of(quad())
            CubeUrn -> Optional.of(cube())
            SphereUrn -> Optional.of(sphere())
            else -> Optional.empty()
        }

    private fun quad(): MeshData = MeshData().apply {
        vertices = floatArrayOf(
            -1f, -1f, 0f, //bottom left corner
            -1f, 1f, 0f, //top left corner
            1f, 1f, 0f, //top right corner
            1f, -1f, 0f //bottom right corner
        )

        indices = intArrayOf(
            0, 1, 2, // first triangle (bottom left - top left - top right)
            0, 2, 3 // second triangle (bottom left - top right - bottom right)
        )

        textureCoords = floatArrayOf(
            0f, 0f, //lower-left corner
            1f, 0f, // lower-right corner
            0f, 1f, //top left corner
            1f, 1f //top right corner
        )
    }

    private fun cube(): MeshData = MeshData().apply {

    }


    private fun sphere(): MeshData = MeshData().apply {

    }


    companion object {
        val QuadUrn: ResourceUrn = urn("engine:prims#quad")
        val CubeUrn: ResourceUrn = urn("engine:prims#cube")
        val SphereUrn: ResourceUrn = urn("engine:prims#sphere")
    }
}