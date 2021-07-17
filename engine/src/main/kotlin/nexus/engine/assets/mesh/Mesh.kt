package nexus.engine.assets.mesh

import nexus.engine.assets.Asset
import nexus.engine.assets.AssetFactory
import nexus.engine.assets.AssetType
import nexus.engine.assets.RegisterAssetType
import nexus.engine.resource.ResourceUrn


/**
 * This is used to load the stuff for the mesh
 */
@RegisterAssetType(
    format = MeshFormat::class,
    /*This specifies our creation of the mesh*/
    factory = Mesh.Factory::class
)
class Mesh(resourceUrn: ResourceUrn, assetType: AssetType<Mesh, MeshData>) : Asset<MeshData>(resourceUrn, assetType) {
    /**
     * create a new mesh wit the given mesh data
     */
    private constructor(resourceUrn: ResourceUrn, assetType: AssetType<Mesh, MeshData>, data: MeshData) : this(
        resourceUrn,
        assetType) {
        reload(data)
    }

    /**
     * Called to reload an asset with the given data.
     *
     * @param data The data to load.
     * @throws org.terasology.gestalt.assets.exceptions.InvalidAssetDataException If the asset data is invalid or cannot be loaded
     */
    override fun doReload(data: MeshData) {


    }

    /**
     * The factory is what actually is used to create the Mesh
     */
    class Factory : AssetFactory<Mesh, MeshData> {
        /**
         * @param urn       The urn of the asset to construct
         * @param assetType The assetType the asset belongs to
         * @param data      The data for the asset
         * @return The built asset
         * @throws org.terasology.gestalt.assets.exceptions.InvalidAssetDataException If the asset failed to load due to invalid data
         */
        override fun build(urn: ResourceUrn, assetType: AssetType<Mesh, MeshData>, data: MeshData): Mesh =
            Mesh(urn, assetType, data)

    }
}