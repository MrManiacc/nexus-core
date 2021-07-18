package nexus.engine.assets.registry

import mu.KotlinLogging
import nexus.engine.assets.Asset
import nexus.engine.assets.AssetData
import nexus.engine.assets.AssetFactory
import nexus.engine.assets.AssetType
import nexus.engine.module.resources.ModuleFileSource
import nexus.engine.module.ClasspathFileSource
import nexus.engine.assets.format.producer.AssetDataProducer
import nexus.engine.assets.scan.AssetTypeScanner
import nexus.engine.assets.scan.ReflectiveAssetTypeScanner
import org.reflections8.Reflections
import org.reflections8.scanners.ResourcesScanner
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * This implements our asset manager. It's a static instance of the manager
 */
class AssetTypeManagerInternal internal constructor() : AssetTypeManager {
    private val registeredTypes: MutableMap<KClass<out Asset<out AssetData>>, AssetType<out Asset<out AssetData>, out AssetData>> =
        HashMap()
    private val logger = KotlinLogging.logger { }


    /**
     * This is used for scanning of the class path
     */
    override val scanners: MutableList<AssetTypeScanner> = arrayListOf(
        ReflectiveAssetTypeScanner()
    )

    /**
     * This method should be used to scan the class path to find asset types that need registartion.
     * It will use [scanners] to pass the [AssetTypeScanner] scanners for collection of asset types
     */
    override fun scan(scanners: Collection<AssetTypeScanner>): Collection<AssetType<*, *>> {
        val result = ArrayList<AssetType<out Asset<out AssetData>, out AssetData>>()
        scanners.forEach {
            logger.debug { "starting collection of asset types for scanner '${it::class.simpleName}'" }
            val collected = it.collect()
            logger.info { "collected ${collected.size} total asset types for scanner '${it::class.simpleName}. Collected types: ${collected.joinToString()}}'" }
            result.addAll(it.collect())
        }
        return result
    }

    /**
     * This is used for creating the actual references to the assets
     */
    override lateinit var source: ModuleFileSource
        private set


    /**
     * Reloads all assets.
     */
    override fun reloadAssets() {

//        assetTypes().forEach { it.getLoadedAssets().forEach { it.relo } }

        // TODO load data from file
    }

    /**
     * Retrieves the AssetType for a given class of Asset, if available.
     *
     * @param type The class of Asset to get the type of
     * @param <T>  The type of Asset
     * @param <U>  The type of AssetData
     * @return The requested AssetType if available
    </U></T> */
    override fun <T : Asset<U>, U : AssetData> getAssetType(type: KClass<T>): Optional<AssetType<T, U>> {
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
        factory: KClass<out AssetFactory<T, U>>,
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
        factory: KClass<out AssetFactory<T, U>>,
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
        TODO("Not implemented yet")
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
     * This is my attempt at mitigating the annoying exception messages
     */
    init {
        try {
            source = ClasspathFileSource(Reflections(ResourcesScanner()))
        } catch (ex: Exception) {
            //Ignored
        }
    }

    /**
     * This allows for internal creation of type manager
     */
    companion object {
        fun AssetTypeManager.Companion.create(): AssetTypeManager = AssetTypeManagerInternal()
    }
}