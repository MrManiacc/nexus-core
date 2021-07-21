package nexus.engine.assets.mesh

import nexus.engine.assets.format.producer.AssetDataProducer
import nexus.engine.module.naming.ResourceUrn
import java.util.*

/**
 * This uses the asset manager to get the mesh asset type, and produce the correct data using asssimp
 */
class MeshAssimp : AssetDataProducer<MeshData> {

    /**
     * This should be used to generate/load asset data for a given urn. This allows us to ability of using kotlins nice
     * unit's for easy creation of asset data. This allow for matching of certain types based upon the [urn]
     *
     * @param urn The urn to get AssetData for
     * @return An optional with the AssetData, if available
     * @throws IOException If there is an error producing the AssetData.
     */
    override fun produce(urn: ResourceUrn): Optional<MeshData> {

        return Optional.empty()
    }
}