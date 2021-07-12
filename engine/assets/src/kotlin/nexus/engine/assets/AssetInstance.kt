package nexus.engine.assets

/**
 * This class should have no arguments for the constructor in the implementation.
 * IT should however be able to accept [AssetData] upon creation.
 */
interface AssetInstance<A : Asset<A, D>, D : AssetData<A, D>> {

    fun dispose()
}

