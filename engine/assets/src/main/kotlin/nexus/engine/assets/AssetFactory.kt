package nexus.engine.assets

import mu.KotlinLogging
import nexus.engine.module.naming.ResourceUrn
import org.slf4j.Logger
import kotlin.reflect.KClass


/**
 * AssetFactorys are used to load AssetData into new assets.
 *
 * For many assets, the assets just have one asset implementation so the factory would simply call the constructor for the implementation and pass the urn and data
 * straight through. However other assets may have multiple implementations (e.g. Texture may have an OpenGL and a DirectX implementation) so the factory installed
 * will determine that. Additionally the factory may pass through other information (OpenGL texture handle, or a reference to a central OpenGL context).
 *
 *
 */
fun interface AssetFactory<T : Asset<U>, U : AssetData> {
    /**
     * @param urn       The urn of the asset to construct
     * @param assetType The assetType the asset belongs to
     * @param data      The data for the asset
     * @return The built asset
     * @throws org.terasology.gestalt.assets.exceptions.InvalidAssetDataException If the asset failed to load due to invalid data
     */
    fun build(urn: ResourceUrn, assetType: AssetType<T, U>, data: U): T

    companion object {
        val logger: Logger = KotlinLogging.logger { }

        /**
         * This attempts to find the given constructor and create a new instance of the factory from the refied type
         */
        @Throws(NoUrnAssetTypeConstructorException::class)
        inline operator fun <reified T : Asset<U>, U : AssetData> invoke(): AssetFactory<T, U> {
            try {
                val cls = T::class.java.getConstructor(ResourceUrn::class.java, AssetType::class.java)
                return AssetFactory { urn, assetType, data ->
                    val asset = cls.newInstance(urn, assetType)
                    asset.reload(data)
                    logger.info("Created and reloading asset of type ${asset::class.simpleName}, with data: $data")
                    asset
                }
            } catch (ex: Exception) {
                throw NoUrnAssetTypeConstructorException(T::class)
            }
        }


        class AssetFactoryAdapter<T : Asset<U>, U : AssetData> : AssetFactory<T, U> {
            /**
             * @param urn       The urn of the asset to construct
             * @param assetType The assetType the asset belongs to
             * @param data      The data for the asset
             * @return The built asset
             * @throws org.terasology.gestalt.assets.exceptions.InvalidAssetDataException If the asset failed to load due to invalid data
             */
            @Throws(NoUrnAssetTypeConstructorException::class)

            override fun build(urn: ResourceUrn, assetType: AssetType<T, U>, data: U): T {
                val constructor =
                    assetType.assetClass.java.getConstructor(ResourceUrn::class.java, AssetType::class.java)
                val asset = constructor.newInstance(urn, assetType)
                asset.reload(data)
                logger.info("Created and reloading asset of type ${asset::class.simpleName}, with data: $data")
                return asset
            }
        }

    }

    class NoUrnAssetTypeConstructorException(assetClass: KClass<out Asset<*>>) :
        RuntimeException("Failed to find a constructor with a resource urn, followed by a asset type, in asset: '${assetClass.simpleName}")


}


