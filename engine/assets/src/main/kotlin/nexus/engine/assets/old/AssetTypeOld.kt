package nexus.engine.assets.old

import kotlin.reflect.KClass

interface AssetTypeOld<A : AssetTypeOld<A, D>, D : OldAssetData<A, D>> {
    val type: KClass<out AssetInstanceOld<A, D>>

    /**
     * This should prepare the asset
     */
    fun initialize(data: D): AssetTypeOld<A, D> {
        return this
    }

    /**
     * This should return a new asset instance of this type from the givnen asset
     */
    fun instantiate(data: D): AssetInstanceOld<A, D>
}
