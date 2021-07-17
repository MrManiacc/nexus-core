package nexus.engine.assets.mesh

import nexus.engine.assets.Asset
import nexus.engine.assets.AssetData
import nexus.engine.assets.AssetFactory
import nexus.engine.assets.AssetType
import nexus.engine.resource.ResourceUrn

class AdapterFactory<T : Asset<U>, U : AssetData> : AssetFactory<T, U> {

    /**
     * @param urn       The urn of the asset to construct
     * @param assetType The assetType the asset belongs to
     * @param data      The data for the asset
     * @return The built asset
     * @throws org.terasology.gestalt.assets.exceptions.InvalidAssetDataException If the asset failed to load due to invalid data
     */
    override fun build(urn: ResourceUrn, assetType: AssetType<T, U>, data: U): T {
        TODO("Not yet implemented")
    }
}