package nexus.engine.assets

import kotlin.reflect.KClass

/**
 * This is used to specify an asset factory
 */
@Retention(AnnotationRetention.RUNTIME) @Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class RegisterAssetType(
    /**
     * This is the required type, we must specify the factory if we wish to auto load.
     */
    val factory: KClass<out AssetFactory<*, *>>,

    /**
     * Any extensions will be automatically mapped and loaded to this asset's factory
     */
    vararg val extension: String,
) {
}