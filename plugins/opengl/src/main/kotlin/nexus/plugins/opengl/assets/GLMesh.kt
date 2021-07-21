package nexus.plugins.opengl.assets

import mu.KotlinLogging
import nexus.engine.assets.AssetType
import nexus.engine.assets.RegisterAssetType
import nexus.engine.assets.mesh.*
import nexus.engine.module.naming.ResourceUrn

/**
 * This is used to load the stuff for the mesh
 */
@RegisterAssetType(
    /*This specifies our creation of the mesh, via file extensions*/
    MeshFormat::class,
    /**This allows specific urns to be loaded up using certian data producers, in this case primitives**/
    producers = [MeshAssimp::class, MeshPrimitives::class]
)
class GLMesh(resourceUrn: ResourceUrn, assetType: AssetType<Mesh, MeshData>) : Mesh(resourceUrn, assetType) {
    private val logger = KotlinLogging.logger { }

    /**
     * Called to reload an asset with the given data.
     *
     * @param data The data to load.
     * @throws org.terasology.gestalt.assets.exceptions.InvalidAssetDataException If the asset data is invalid or cannot be loaded
     */
    override fun doReload(data: MeshData) {
        logger.info { "reloading opengl mesh with data: $data" }
    }
}