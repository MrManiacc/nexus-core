package nexus.engine.assets.registry

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import nexus.engine.assets.Asset
import nexus.engine.assets.AssetData
import nexus.engine.assets.AssetType
import nexus.engine.module.naming.ResourceUrn
import java.util.*
import kotlin.reflect.KClass


/**
 * AssetManager provides an simplified interface for working with assets across multiple asset types.
 * <p>
 * To do this it uses an {@link AssetTypeManager} to obtain the AssetTypes relating to an Asset
 * class of interest, and delegates down to them for actions such as obtaining and reloading assets.
 * </p>
 *
 */
open class AssetManager(val typeManager: AssetTypeManager) {

    /**
     * @param urn  The urn of the asset to check. Must not be an instance urn
     * @param type The Asset class of interest
     * @return whether an asset is loaded with the given urn
     */
    fun <T : Asset<U>, U : AssetData> isLoaded(urn: ResourceUrn, type: KClass<T>): Boolean {
        for (assetType in typeManager.getAssetTypes(type)) {
            if (assetType.isLoaded(urn))
                return true
        }
        return false
    }

    /**
     * Retrieves a set of the ResourceUrns for all loaded assets of the given Asset class (including subtypes)
     *
     * @param type The Asset class of interest
     * @param <T>  The Asset class
     * @return A set of the ResourceUrns of all loaded assets
    </T> */
    fun <T : Asset<U>, U : AssetData> getLoadedAssetUrns(type: KClass<T>): Set<ResourceUrn> {
        val assetTypes: List<AssetType<out T, *>> = typeManager.getAssetTypes(type)
        return when (assetTypes.size) {
            0 -> emptySet()
            1 -> assetTypes[0].getLoadedAssetUrns()
            else -> {
                val result: MutableSet<ResourceUrn> = Sets.newLinkedHashSet()
                for (assetType in assetTypes)
                    result.addAll(assetType.getLoadedAssetUrns())
                result
            }
        }
    }


    /**
     * Retrieves a list of all loaded assets of the given Asset class (including subtypes)
     *
     * @param type The Asset class of interest
     * @param <T>  The Asset class
     * @param <U>  The AssetData class
     * @return A list of all the loaded assets
    </U></T> */
    fun <T : Asset<U>, U : AssetData> getLoadedAssets(type: KClass<T>): Set<T> {
        val assetTypes: List<AssetType<out T, *>> = typeManager.getAssetTypes(type)
        return when (assetTypes.size) {
            0 -> emptySet()
            else -> {
                val builder: ImmutableSet.Builder<T> = ImmutableSet.builder()
                for (assetType in assetTypes) {
                    builder.addAll(assetType.getLoadedAssets())
                }
                builder.build()
            }
        }
    }

    /**
     * Retrieves a set of the ResourceUrns for all available assets of the given Asset class (including subtypes). An available asset is either a loaded asset, or one
     * which can be requested. The set is not necessarily complete as assets procedurally generated from their resource urn may not be included.
     *
     * @param type The Asset class of interest
     * @param <T>  The Asset class
     * @return A set of the ResourceUrns of all available assets
    </T> */
    fun <T : Asset<U>, U : AssetData> getAvailableAssets(type: KClass<T>): Set<ResourceUrn> {
        val assetTypes: List<AssetType<out T, *>> = typeManager.getAssetTypes(type)
        return when (assetTypes.size) {
            0 -> emptySet()
            1 -> assetTypes[0].getAvailableAssetUrns()
            else -> {
                val result: MutableSet<ResourceUrn> = Sets.newLinkedHashSet()
                for (assetType in assetTypes) {
                    result.addAll(assetType.getAvailableAssetUrns())
                }
                result
            }
        }
    }


    /**
     * Retrieves an asset with the given urn and type
     *
     * @param urn  The urn of the asset to retrieve
     * @param type The type of asset to retrieve
     * @param <T>  The class of Asset
     * @param <U>  The class of AssetData
     * @return An Optional containing the requested asset if successfully obtained
    </U></T> */
    fun <T : Asset<U>, U : AssetData> getAsset(urn: ResourceUrn, type: KClass<T>): Optional<T> {
        val assetTypes: List<AssetType<out T, *>> = typeManager.getAssetTypes(type)
        when (assetTypes.size) {
            0 -> return Optional.empty()
            1 -> {
                val result = assetTypes[0].getAsset(urn)
                if (result.isPresent) {
                    return Optional.of(result.get())
                }
            }
            else -> for (assetType in assetTypes) {
                val result = assetType.getAsset(urn)
                if (result.isPresent) {
                    return Optional.of(result.get())
                }
            }
        }
        return Optional.empty()
    }

    inline fun <reified T : Asset<U>, reified U : AssetData> getAsset(urn: ResourceUrn): Optional<T> =
        getAsset(urn, T::class)

    /**
     * Creates or reloads an asset with the given urn, data and type. The type must be the actual type of the asset, not a super type.
     *
     * @param urn  The urn of the asset
     * @param data The data to load the asset with
     * @param type The type of the asset
     * @param <T>  The class of Asset
     * @param <U>  The class of AssetData
     * @return The loaded asset
     * @throws java.lang.IllegalStateException if the asset type is not managed by this AssetManager.
    </U></T> */
    fun <T : Asset<U>, U : AssetData> loadAsset(urn: ResourceUrn, data: U, type: KClass<T>): T {
        val assetType: Optional<AssetType<T, U>> = typeManager.getAssetType(type)
        return if (assetType.isPresent) {
            assetType.get().loadAsset(urn, data)
        } else {
            throw IllegalStateException("$type is not a supported type of asset")
        }
    }

    inline fun <reified T : Asset<U>, reified U : AssetData> loadAsset(urn: ResourceUrn, data: U): T =
        loadAsset(urn, data, T::class)

}
