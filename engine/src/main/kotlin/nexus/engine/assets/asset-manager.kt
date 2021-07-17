@file:Suppress("UNCHECKED_CAST")

package nexus.engine.assets

import nexus.engine.assets.format.AssetFileProvider
import nexus.engine.assets.format.ClassgraphFileSource
import org.reflections8.Reflections
import org.reflections8.scanners.ResourcesScanner
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * This implements our asset manager. It's a static instance of the manager
 */
internal object AssetManagerImpl : AssetTypeManager {
    private val registeredTypes: MutableMap<KClass<out Asset<*>>, AssetType<*, *>> = HashMap()

    /**
     * This is used for creating the actual references to the assets
     */
    override lateinit var source: AssetFileProvider
        private set

    init {
        try {
            source = ClassgraphFileSource(Reflections(ResourcesScanner()))
        } catch (ex: Exception) {
            //Ignored
        }
    }

    /**
     * Retrieves the AssetType for a given class of Asset, if available.
     *
     * @param type The class of Asset to get the type of
     * @param <T>  The type of Asset
     * @param <U>  The type of AssetData
     * @return The requested AssetType if available
    </U></T> */
    override

    fun <T : Asset<U>, U : AssetData> getAssetType(type: KClass<T>): Optional<AssetType<T, U>> {
        if (registeredTypes.containsKey(type)) {
            return Optional.ofNullable(registeredTypes[type] as AssetType<T, U>)
        }
        return Optional.empty()
    }

    /**
     * Retrieves the possible AssetTypes for a given class of Asset. This should include subtypes.
     *
     *
     * Example: given AssetB and AssetC which are subtypes of AssetA, getAssetTypes(AssetA.class) should return all of
     * the AssetTypes for AssetA, AssetB and AssetC which are available.
     *
     *
     * @param type The class of Asset to get the AssetTypes for
     * @param <T>  The class of Asset
     * @return A list of available AssetTypes.
    </T> */
    override fun <T : Asset<*>> getAssetTypes(type: KClass<T>): List<AssetType<out T, *>> {
        return registeredTypes.filterKeys { type.java.isAssignableFrom(it.java) }.values as List<AssetType<out T, *>>
    }

    /**
     * Removes and closes an asset type.
     *
     * @param type The type of asset to remove
     * @param <T>  The type of asset
     * @param <U>  The type of asset data
    </U></T> */
    override fun <T : Asset<U>, U : AssetData> removeAssetType(type: KClass<T>) {
        val removed = registeredTypes.remove(type) ?: return
        removed.close()
    }

    /**
     * Creates and registers an asset type
     *
     * @param type           The type of asset the AssetType will handle
     * @param factory        The factory for creating an asset from asset data
     * @param subfolderNames The names of the subfolders providing asset files, if any
     * @param <T>            The type of Asset
     * @param <U>            The type of AssetData
     * @return The new AssetType
    </U></T> */
    override fun <T : Asset<U>, U : AssetData> createAssetType(
        type: KClass<T>,
        factory: KClass<AssetFactory<T, U>>,
        vararg subfolderNames: String,
    ): AssetType<T, U> {
        return createAssetType(type, factory, subfolderNames.toList())
    }

    /**
     * Creates and registers an asset type
     *
     * @param type           The type of asset the AssetType will handle
     * @param factory        The factory for creating an asset from asset data
     * @param subfolderNames The names of the subfolders providing asset files, if any
     * @param <T>            The type of Asset
     * @param <U>            The type of AssetData
     * @return The new AssetType
    </U></T> */
    override fun <T : Asset<U>, U : AssetData> createAssetType(
        type: KClass<T>,
        factory: KClass<AssetFactory<T, U>>,
        subfolderNames: Collection<String>,
    ): AssetType<T, U> {
        return AssetType(type, factory)
    }

    /**
     * Registers an asset type
     *
     * @param assetType      The AssetType to register
     * @param subfolderNames The names of the subfolders providing asset files, if any
     * @param <T>            The type of Asset
     * @param <U>            The type of AssetData
     * @return The new AssetType
    </U></T> */
    override fun <T : Asset<U>, U : AssetData> addAssetType(
        assetType: AssetType<T, U>,
        vararg subfolderNames: String,
    ): AssetType<T, U> {
        return addAssetType(assetType, subfolderNames.toMutableList())
    }

    /**
     * Registers an asset type
     *
     * @param assetType      The AssetType to register
     * @param subfolderNames The names of the subfolders providing asset files, if any
     * @param <T>            The type of Asset
     * @param <U>            The type of AssetData
     * @return The new AssetType
    </U></T> */
    override fun <T : Asset<U>, U : AssetData> addAssetType(
        assetType: AssetType<T, U>,
        subfolderNames: Collection<String>,
    ): AssetType<T, U> {
        registeredTypes[assetType.assetClass] = assetType
        return assetType
    }

    /**
     * @param assetType The AssetType to get the AssetFileDataProducer for. This must be an AssetType handled by this AssetTypeManager
     * @param <T>       The type of Asset handled by the AssetType
     * @param <U>       The type of AssetData handled by the AssetType
     * @return The AssetFileDataProducer for the given AssetType.
    </U></T> */
    override fun <T : Asset<U>, U : AssetData> getAssetFileDataProducer(assetType: AssetType<T, U>): AssetDataProducer<U> {
        TODO("Not implemented")
    }

    /**
     * @return Retrieves a list of all available asset types
     */
    override fun assetTypes(): Collection<AssetType<*, *>> {
        return this.registeredTypes.values.toList()
    }

    /**
     * Disposes any assets that are unused (not referenced)
     */
    override fun disposedUnusedAssets() {
        assetTypes().forEach(Consumer { type: AssetType<*, *> -> type.processDisposal() })
    }

    /**
     * Reloads all assets.
     */
    override fun reloadAssets() {

//        assetTypes().forEach { it.getLoadedAssets().forEach { it.relo } }

        // TODO load data from file
    }


}

/**
 * This allows use to access the
 */
operator fun AssetTypeManager.Companion.invoke(): AssetTypeManager = AssetManagerImpl