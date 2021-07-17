package nexus.engine.assets

import nexus.engine.assets.format.AssetFileProvider
import nexus.engine.assets.format.FileReference
import java.util.*
import kotlin.reflect.KClass


/**
 * ModuleAwareAssetTypeManager is an AssetTypeManager that integrates with a ModuleEnvironment obtaining assets, registering extension classes and handling asset
 * disposal and reloading when environments change.
 * <p>
 * The major features of ModuleAwareAssetTypeManager are:
 * </p>
 * <ul>
 * <li>Registration of core AssetTypes, AssetDataProducers and file formats. These will remain across environment changes.</li>
 * <li>Automatic registration of extension AssetTypes, AssetDataProducers and file formats mark with annotations that are discovered within the module environment
 * being switched to, and removal of these extensions when the module environment is later unloaded</li>
 * <li>Optionally reload all assets from their modules - this is recommended after an environment switch to prevent changes to assets in a previous environment from persisting</li>
 * </ul>
 *
 * @author Immortius
 */

interface AssetTypeManager : AssetFileProvider {
    /**
     * This is used for creating the actual references to the assets
     */
    val source: AssetFileProvider

    /**
     * Obtain the handle to a specific file
     *
     * @param filepath The path to the file. Should not be empty
     * @return The requested file, or [Optional.empty] if it doesn't exist
     */
    override fun getFile(filepath: List<String>): Optional<FileReference> {
        return source.getFile(filepath)
    }

    /**
     * Finds all files within a path
     *
     * @param recursive Whether to recurse through subpaths
     * @param path      The path to search
     * @return A collection of handles to all files in the give path
     */
    override fun getFilesInPath(recursive: Boolean, path: List<String>): Collection<FileReference> {
        return source.getFilesInPath(recursive, path)
    }

    /**
     * Finds all subpaths in the given path
     *
     * @param fromPath The path to search
     * @return A list of the immediate subpaths in the given path
     */
    override fun getSubpaths(fromPath: List<String>): Set<String> {
        return source.getSubpaths(fromPath)
    }

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator(): Iterator<FileReference> {
        return source.iterator()
    }

    /**
     * Retrieves the AssetType for a given class of Asset, if available.
     *
     * @param type The class of Asset to get the type of
     * @param <T>  The type of Asset
     * @param <U>  The type of AssetData
     * @return The requested AssetType if available
    </U></T> */
    fun <T : Asset<U>, U : AssetData> getAssetType(type: KClass<T>): Optional<AssetType<T, U>>

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
    fun <T : Asset<*>> getAssetTypes(type: KClass<T>): List<AssetType<out T, *>>


    /**
     * Removes and closes an asset type.
     *
     * @param type The type of asset to remove
     * @param <T>  The type of asset
     * @param <U>  The type of asset data
    </U></T> */
    fun <T : Asset<U>, U : AssetData> removeAssetType(type: KClass<T>)


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
    fun <T : Asset<U>, U : AssetData> createAssetType(
        type: KClass<T>,
        factory: KClass<AssetFactory<T, U>>,
        vararg subfolderNames: String,
    ): AssetType<T, U>

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
    fun <T : Asset<U>, U : AssetData> createAssetType(
        type: KClass<T>,
        factory: KClass<AssetFactory<T, U>>,
        subfolderNames: Collection<String>,
    ): AssetType<T, U>

    /**
     * Registers an asset type
     *
     * @param assetType      The AssetType to register
     * @param subfolderNames The names of the subfolders providing asset files, if any
     * @param <T>            The type of Asset
     * @param <U>            The type of AssetData
     * @return The new AssetType
    </U></T> */
    fun <T : Asset<U>, U : AssetData> addAssetType(
        assetType: AssetType<T, U>,
        vararg subfolderNames: String,
    ): AssetType<T, U>

    /**
     * Registers an asset type
     *
     * @param assetType      The AssetType to register
     * @param subfolderNames The names of the subfolders providing asset files, if any
     * @param <T>            The type of Asset
     * @param <U>            The type of AssetData
     * @return The new AssetType
    </U></T> */
    fun <T : Asset<U>, U : AssetData> addAssetType(
        assetType: AssetType<T, U>,
        subfolderNames: Collection<String>,
    ): AssetType<T, U>


    /**
     * @param assetType The AssetType to get the AssetFileDataProducer for. This must be an AssetType handled by this AssetTypeManager
     * @param <T>       The type of Asset handled by the AssetType
     * @param <U>       The type of AssetData handled by the AssetType
     * @return The AssetFileDataProducer for the given AssetType.
    </U></T> */
    fun <T : Asset<U>, U : AssetData> getAssetFileDataProducer(assetType: AssetType<T, U>): AssetDataProducer<U>

    /**
     * @return Retrieves a list of all available asset types
     */
    fun assetTypes(): Collection<AssetType<*, *>>

    /**
     * Disposes any assets that are unused (not referenced)
     */
    fun disposedUnusedAssets()


    /**
     * Reloads all assets.
     */
    fun reloadAssets()

    companion object
}
