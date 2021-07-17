package nexus.engine.assets.old

/**
 * This class should have no arguments for the constructor in the implementation.
 * IT should however be able to accept [OldAssetData] upon creation.
 */
interface AssetInstanceOld<A : AssetTypeOld<A, D>, D : OldAssetData<A, D>> {
    /**
     * Delete of the asset instance
     */
    fun dispose()
}

