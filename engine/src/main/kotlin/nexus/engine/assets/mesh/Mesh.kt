package nexus.engine.assets.mesh

import nexus.engine.assets.Asset
import nexus.engine.assets.AssetType
import nexus.engine.module.naming.ResourceUrn


/**
 * This is simply used as the definition of the mesh, it still must be implemented per framework
 */
abstract class Mesh(resourceUrn: ResourceUrn, assetType: AssetType<Mesh, MeshData>) :
    Asset<MeshData>(resourceUrn, assetType)