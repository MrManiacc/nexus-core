package nexus.engine.assets

import com.google.common.base.Preconditions
import nexus.engine.resource.ResourceUrn
import java.util.*
import javax.annotation.concurrent.ThreadSafe


/**
 * Abstract base class common to all assets.
 * <p>
 * An asset is a resource that is used by the game - a texture, sound, block definition and the like. These are typically
 * loaded from a module, although they can also be created at runtime. Each asset is identified by a ResourceUrn that uniquely
 * identifies it and can be used to obtain it. This urn provides a lightweight way to serialize a reference to an Asset.
 * </p>
 * <p>
 * Assets are created from a specific type of asset data. There may be a multiple implementations with a common base for a particular type of asset data
 * - this allows for implementation specific assets (e.g. OpenGL vs DirectX textures for example might have an OpenGLTexture and DirectXTexture implementing class
 * respectively, with a common Texture base class).
 * </p>
 * <p>
 * Assets may be reloaded by providing a new batch of data, or disposed to free resources - disposed assets may no
 * longer be used.
 * </p>
 * <p>
 * To support making Asset implementations thread safe reloading, creating an instance and disposal are all synchronized.
 * Implementations should consider thread safety around any methods they add if it is intended for assets to be used across multiple threads.
 * </p>
 *
 * 
 */
@ThreadSafe
abstract class Asset<U : AssetData>(val urn: ResourceUrn, val assetType: AssetType<*, U>) {
    private val disposalHook: DisposalHook = DisposalHook()

    constructor(urn: ResourceUrn, assetType: AssetType<*, U>, data: U) : this(urn, assetType) {
        reload(data)
    }


    /**
     * This keeps track of whether or not we've disposed our assets
     */
    @Volatile var isDisposed = false
        private set

    /**
     * Reloads this assets using the new data. This should be thread safe.
     *
     * @param data The data to reload the asset with.
     * @throws InvalidAssetDataException If the asset data is invalid or cannot be loaded
     */
    @Synchronized fun reload(data: U) {
        if (!isDisposed) {
            doReload(data)
        } else {
            throw IllegalStateException("Cannot reload disposed asset '" + urn() + "'")
        }
    }


    /**
     * Creates an instance of this asset. The instance will have the same urn as this asset, with the instance flag appended, and initially have the same data and settings.
     *
     *
     * Instance assets are reloaded back to the same value as their origin if their asset type is refreshed.
     *
     *
     * @param <U> The asset type
     * @return A new instance of the asset.
    </U> */
    fun <T : Asset<U>, U : AssetData> createInstance(): Optional<T> {
        Preconditions.checkState(!isDisposed)
        return assetType.createInstance(this) as Optional<T>
    }

    /**
     * This creates a copy using our own asset type
     */
    @Synchronized fun createCopy(copyUrn: ResourceUrn): Optional<out Asset<U>> {
        Preconditions.checkState(!isDisposed)
        return doCreateCopy(copyUrn, this.assetType)
    }

    /**
     * Attempts to create a copy of the asset, with the given urn. This is used as part of the process of creating an asset instance.
     *
     *
     * If direct copies are not supported, then [Optional.empty] should be returned.
     *
     *
     *
     * Implementing classes should create a copy of the asset. This may be done by creating an AssetData of the current asset and using it to create
     * a new asset, or may need to use more implementation specific methods (an OpenGL texture may use an OpenGL texture handle copy technique to produce the
     * copy, for example)
     *
     *
     * @param copyUrn         The urn for the new instance
     * @param parentAssetType The type of the parent asset
     * @return The created copy if any
     */
    protected open fun doCreateCopy(
        copyUrn: ResourceUrn?,
        parentAssetType: AssetType<*, U>,
    ): Optional<out Asset<U>> {
        return Optional.empty()
    }


    /**
     * Called to reload an asset with the given data.
     *
     * @param data The data to load.
     * @throws org.terasology.gestalt.assets.exceptions.InvalidAssetDataException If the asset data is invalid or cannot be loaded
     */
    protected abstract fun doReload(data: U)


    /**
     * Disposes this asset, freeing resources and making it unusable
     */
    @Synchronized fun dispose() {
        if (!isDisposed) {
            isDisposed = true
            assetType.onAssetDisposed(this)
            disposalHook.dispose()
        }
    }

    /**
     * This is used to initialize our asset inside our asset type.
     */
    init {
        assetType.registerAsset(this, disposalHook)
    }


}