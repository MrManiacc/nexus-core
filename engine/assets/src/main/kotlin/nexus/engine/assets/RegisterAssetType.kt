package nexus.engine.assets

import nexus.engine.assets.format.AssetFileFormat
import nexus.engine.assets.format.producer.AssetDataProducer
import kotlin.reflect.KClass

/**
 * This is used to specify an asset factory
 */
@Retention(AnnotationRetention.RUNTIME) @Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class RegisterAssetType(

    /**
     * This is the required type, we must specify the factory if we wish to auto load.
     */
    val format: KClass<out AssetFileFormat<*>> = AssetFileFormat::class,
    /**
     * This allows us custom delegation for the creation of the given asset
     */
    val factory: KClass<out AssetFactory<*, *>> = AssetFactory.Companion.AssetFactoryAdapter::class,
    /**
     * This allows us to specific addinational optional producers, alongside the format
     */
    val producers: Array<KClass<out AssetDataProducer<*>>>,
)