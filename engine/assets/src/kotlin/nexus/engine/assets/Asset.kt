package nexus.engine.assets

import kotlin.reflect.KClass

interface Asset<A : Asset<A, D>, D : AssetData<A, D>> {
    val type: KClass<out AssetInstance<A, D>>

    /**
     * This should prepare the asset
     */
    fun initialize(data: D): Asset<A, D> {
        return this
    }

    /**
     * This should return a new asset instance of this type from the givnen asset
     */
    fun instantiate(data: D): AssetInstance<A, D>
}
