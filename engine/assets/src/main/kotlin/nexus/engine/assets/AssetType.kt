package nexus.engine.assets

import com.google.common.base.Function
import com.google.common.base.Preconditions
import com.google.common.collect.*
import mu.KotlinLogging
import nexus.engine.reflection.getTypeParameterBindingForInheritedClass
import nexus.engine.resource.Name
import nexus.engine.resource.ResourceUrn
import nexus.engine.resource.urn
import org.slf4j.Logger
import java.io.Closeable
import java.io.IOException
import java.lang.ref.PhantomReference
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import java.util.*
import java.util.concurrent.Semaphore
import javax.annotation.Nullable
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * AssetType manages all assets of a particular type/class.  It provides the ability to resolve and load assets by Urn, and caches assets so that there is only
 * a single instance of a given asset shared by all users.
 * <p>
 * AssetType is thread safe.
 * </p>
 *
 * @param <T> The type of asset this AssetType manages
 * @param <U> The type of asset data required by the assets this AssetType manages
 */
class AssetType<T : Asset<U>, U : AssetData>(
    val assetClass: KClass<T>,
    val factoryClass: KClass<AssetFactory<T, U>>,
) : Closeable {
    private val factory: AssetFactory<T, U> = factoryClass.createInstance()
    private val logger: Logger = KotlinLogging.logger { }
    val assetDataType: KClass<U>
    private val producers: MutableList<AssetDataProducer<U>> = Lists.newCopyOnWriteArrayList()
    private val loadedAssets: MutableMap<ResourceUrn, T> = MapMaker().concurrencyLevel(4).makeMap()
    private val instanceAssets: ListMultimap<ResourceUrn, WeakReference<T>> = Multimaps.synchronizedListMultimap(
        ArrayListMultimap.create())

    // Per-asset locks to deal with situations where multiple threads attempt to obtain or create the same unloaded asset concurrently
    private val locks: MutableMap<ResourceUrn, ResourceLock> =
        MapMaker().concurrencyLevel(1).makeMap()

    private val references: MutableSet<AssetReference<out Asset<U>?>> = Sets.newConcurrentHashSet()
    private val disposalQueue = ReferenceQueue<T>()

    @Volatile
    private var closed = false

    @Volatile
    var resolutionStrategy: ResolutionStrategy = ResolutionStrategy { modules, context ->
        if (modules.contains(context)) {
            ImmutableSet.of(context)
        } else {
            modules.toMutableSet()
        }
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     *
     *  As noted in [AutoCloseable.close], cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * *mark* the `Closeable` as closed, prior to throwing
     * the `IOException`.
     *
     * @throws IOException if an I/O error occurs
     */
    override fun close() {
        if (!closed) {
            closed = true
            disposeAll()
            clearProducers()
        }
    }

    /**
     * Refreshes the AssetType. All loaded assets that are provided by the producers are reloaded, all other assets are disposed. Asset instances are reloaded with
     * the data of their parents or disposed along with them.
     *
     *
     * This method is useful when switching contexts (such as changing module environment)
     *
     */
    fun refresh() {
        if (!closed) {
            for (asset in loadedAssets.values) {
                if (followRedirects(asset.urn) != asset.urn || !reloadFromProducers(asset)) {
                    asset.dispose()
                    for (instanceRef in ImmutableList.copyOf(instanceAssets[asset.urn.instanceUrn])) {
                        val instance = instanceRef.get()
                        instance?.dispose()
                    }
                }
            }
        }
    }

    /**
     * Reloads an asset from the current producers, if one of them can produce it
     *
     * @param asset The asset to reload
     * @return Whether the asset was reloaded
     */
    private fun reloadFromProducers(asset: T): Boolean {
        try {
            for (producer in producers) {
                val data = producer.getAssetData(asset.urn)
                if (data.isPresent) {
                    asset.reload(data.get())
                    for (assetInstanceRef in instanceAssets[asset.urn.instanceUrn]) {
                        val assetInstance = assetInstanceRef.get()
                        assetInstance?.reload(data.get())
                    }
                    return true
                }
            }
        } catch (e: IOException) {
            logger.error("Failed to reload asset '{}', disposing", asset.urn)
        }
        return false
    }

    /**
     * Follows any redirects to determine the actual resource urn to use for a given urn
     *
     * @param urn The urn to resolve redirects for
     * @return The final urn to use
     */
    private fun followRedirects(urn: ResourceUrn): ResourceUrn {
        var lastUrn: ResourceUrn
        var finalUrn = urn
        do {
            lastUrn = finalUrn
            for (producer in producers) {
                finalUrn = producer.redirect(finalUrn)
            }
        } while (!lastUrn.equals(finalUrn))
        return finalUrn
    }

    /**
     * Obtains an asset from a string that may be a full or partial urn
     *
     * @param urn The full or partial urn of the asset
     * @return The requested asset if the urn was successfully resolved
     */
    fun getAsset(urn: ResourceUrn): Optional<T> {
        Preconditions.checkNotNull<Any>(urn)
        return if (urn.isInstance) {
            getInstanceAsset(urn)
        } else {
            getNormalAsset(urn)
        }
    }

    private fun getNormalAsset(urn: ResourceUrn): Optional<T> {
        val redirectUrn = followRedirects(urn)
        val asset = loadedAssets[redirectUrn] ?: return reload(redirectUrn)
        return Optional.ofNullable(asset)
    }

    /**
     * Forces a reload of an asset from a data producer, if possible.  The resource urn must not be an instance urn (it doesn't make sense to reload an instance by urn).
     * If there is no available source for the asset (it has no producer) then it will not be reloaded.
     *
     * @param urn The urn of the resource to reload.
     * @return The asset if it exists (regardless of whether it was reloaded or not)
     */
    private fun reload(urn: ResourceUrn): Optional<T> {
        Preconditions.checkArgument(!urn.isInstance, "Cannot reload an asset instance urn")
        val redirectUrn = followRedirects(urn)
        try {
            return AccessController.doPrivileged(PrivilegedExceptionAction {
                for (producer in producers) {
                    val data = producer.getAssetData(redirectUrn)
                    if (data.isPresent) {
                        return@PrivilegedExceptionAction Optional.of(loadAsset(redirectUrn, data.get()))
                    }
                }
                Optional.ofNullable(loadedAssets[redirectUrn])
            } as PrivilegedExceptionAction<Optional<T>>)
        } catch (e: PrivilegedActionException) {
            if (redirectUrn.equals(urn)) {
                logger.error("Failed to load asset '{}'", redirectUrn, e.cause)
            } else {
                logger.error("Failed to load asset '{}' redirected from '{}'", redirectUrn, urn, e.cause)
            }
        }
        return Optional.empty()

    }

    /**
     * Obtains an asset from a string that may be a full or partial urn
     *
     * @param urn The full or partial urn of the asset
     * @return The requested asset if the urn was successfully resolved
     */
    fun getAsset(urn: String): Optional<T> {
        return getAsset(urn, Name.Empty)
    }

    /**
     * Obtains an asset from a string that may be a full or partial urn
     *
     * @param urn           The full or partial urn of the asset
     * @param moduleContext The context to resolve the urn in
     * @return The requested asset if the urn was successfully resolved
     */
    fun getAsset(urn: String, moduleContext: Name): Optional<T> {
        val resolvedUrns: Set<ResourceUrn> = resolve(urn, moduleContext)
        if (resolvedUrns.size == 1) {
            return getAsset(resolvedUrns.iterator().next())
        } else if (resolvedUrns.size > 1) {
            logger.warn("Failed to resolve asset '{}' - multiple possibilities discovered", urn)
        } else {
            logger.warn("Failed to resolve asset '{}' - no matches found", urn)
        }
        return Optional.empty()
    }


    /**
     * Resolves a string urn that may be a full or partial urn, providing the available urns that match
     *
     * @param urn The string to resolve
     * @return A set of possible matching urns
     */
    fun resolve(urn: String): Set<ResourceUrn?>? {
        return resolve(urn, Name.Empty)
    }

    /**
     * Resolves a string urn that may be a full or partial urn, providing the available urns that match
     *
     * @param urn           The string to resolve
     * @param moduleContext The context to resolve within
     * @return A set of possible matching urns
     */
    fun resolve(urn: String, moduleContext: Name): Set<ResourceUrn> {
        if (ResourceUrn.isValid(urn)) {
            return ImmutableSet.of(urn(urn))
        }
        var urnToResolve = urn
        val instance = urn.endsWith(ResourceUrn.INSTANCE_INDICATOR)
        if (instance) {
            urnToResolve = urn.substring(0, urn.length - ResourceUrn.INSTANCE_INDICATOR.length)
        }
        val fragmentSeparatorIndex = urnToResolve.indexOf('#')
        val fragmentName: Name
        val resourceName: Name
        if (fragmentSeparatorIndex != -1) {
            resourceName = Name(urnToResolve.substring(0, fragmentSeparatorIndex))
            fragmentName = Name(urnToResolve.substring(fragmentSeparatorIndex + 1))
        } else {
            resourceName = Name(urnToResolve)
            fragmentName = Name.Empty
        }
        var possibleModules: MutableSet<Name> = Sets.newLinkedHashSet()
        for (producer in producers) {
            possibleModules.addAll(producer.getModulesProviding(resourceName))
        }
        if (!moduleContext.isEmpty) {
            possibleModules = resolutionStrategy.resolve(possibleModules, moduleContext)
        }
        return Sets.newLinkedHashSet(Collections2.transform(possibleModules, object : Function<Name, ResourceUrn> {
            @Nullable override fun apply(input: Name?): ResourceUrn {
                return ResourceUrn(input!!, resourceName, fragmentName, instance)
            }
        }))
    }


    private fun clearProducers() {
        producers.clear()
    }

    private fun disposeAll() {
        loadedAssets.values.forEach { it.dispose() }
        for (assetRef in ImmutableList.copyOf(instanceAssets.values())) {
            val asset = assetRef.get()
            asset?.dispose()
        }
        processDisposal()
        if (!loadedAssets.isEmpty()) {
            logger.error("Assets remained loaded after disposal - {}", loadedAssets.keys)
            loadedAssets.clear()
        }
        if (!instanceAssets.isEmpty) {
            logger.error("Asset instances remained loaded after disposal - {}", instanceAssets.keySet())
            instanceAssets.clear()
        }
    }

    fun processDisposal() {
        var ref: Reference<out Asset<U>?>? = disposalQueue.poll()
        while (ref != null) {
            val assetRef = ref as AssetReference<out Asset<U>>
            assetRef.dispose()
            references.remove(assetRef)
            ref = disposalQueue.poll()
        }
    }

    /**
     * Notifies the asset type when an asset is created
     *
     * @param asset The asset that was created
     */
    @Synchronized fun registerAsset(asset: Asset<U>, disposer: DisposalHook) {
        check(!closed) { "Cannot create asset for disposed asset type: $assetClass" }
        if (asset.urn.isInstance) {
            instanceAssets.put(asset.urn, WeakReference(assetClass.java.cast(asset)))
        } else {
            loadedAssets[asset.urn] = assetClass.java.cast(asset)
        }
        references.add(AssetReference<T>(asset as T, disposalQueue, disposer!!))
    }

    /**
     * Creates and returns an instance of an asset, if possible. The following methods are used to create the copy, in order, with the first technique to succeeed used:
     *
     *  1. Delegate to the parent asset to create the copy
     *  1. Loads the AssetData of the parent asset and create a new instance from that
     *
     *
     * @param urn The urn of the asset to create an instance of
     * @return An instance of the desired asset
     */
    fun getInstanceAsset(urn: ResourceUrn): Optional<T> {
        val parentAsset: Optional<out T> = getAsset(urn.parentUrn)
        return if (parentAsset.isPresent) {
            createInstance(parentAsset.get())
        } else {
            Optional.empty()
        }
    }

    /**
     * Creates an instance of the given asset
     *
     * @param asset The asset to create an instance of
     * @return The new instance, or [Optional.empty] if it could not be created
     */
    fun createInstance(asset: Asset<U>): Optional<T> {
        Preconditions.checkArgument(assetClass.java.isAssignableFrom(asset::class.java))
        val result: Optional<out Asset<U>> = asset.createCopy(asset.urn.instanceUrn)
        if (!result.isPresent) {
            try {
                return AccessController.doPrivileged(PrivilegedExceptionAction {
                    for (producer in producers) {
                        val data: Optional<U> = producer.getAssetData(asset.urn)
                        if (data.isPresent()) {
                            return@PrivilegedExceptionAction Optional.of(loadAsset(asset.urn.instanceUrn, data.get()))
                        }
                    }
                    Optional.ofNullable(assetClass.java.cast(result.get()))
                } as PrivilegedExceptionAction<Optional<T>>)
            } catch (e: PrivilegedActionException) {
                logger.error("Failed to load asset '" + asset.urn.instanceUrn.toString() + "'",
                    e.cause)
            }
        }
        return Optional.ofNullable(assetClass.java.cast(result.get()))
    }

    /**
     * Loads an asset with the given urn and data. If the asset already exists, it is reloaded with the data instead
     *
     * @param urn  The urn of the asset
     * @param data The data to load the asset with
     * @return The loaded (or reloaded) asset
     */
    fun loadAsset(urn: ResourceUrn, data: U): T {
        return if (urn.isInstance) {
            factory.build(urn, this, data)
        } else {
            var asset = loadedAssets[urn]
            if (asset != null) {
                asset.reload(data)
            } else {
                var lock: ResourceLock
                synchronized(locks) {
                    lock = locks.computeIfAbsent(urn,
                        Function<ResourceUrn, ResourceLock> { k: ResourceUrn? ->
                            ResourceLock(urn)
                        })
                }
                try {
                    lock.lock()
                    if (!closed) {
                        asset = loadedAssets[urn]
                        if (asset == null) {
                            asset = factory.build(urn, this, data)
                        } else {
                            asset.reload(data)
                        }
                    }
                    synchronized(locks) {
                        if (lock.unlock()) {
                            locks.remove(urn)
                        }
                    }
                } catch (e: InterruptedException) {
                    logger.error("Failed to load asset - interrupted awaiting lock on resource {}", urn)
                }
            }
            asset!!
        }
    }

    /**
     * @param urn The urn of the asset to check. Must not be an instance urn
     * @return Whether an asset is loaded with the given urn
     */
    fun isLoaded(urn: ResourceUrn): Boolean {
        Preconditions.checkArgument(!urn.isInstance, "Urn must not be an instance urn")
        return loadedAssets.containsKey(urn)
    }

    /**
     * @return A set of the urns of all the loaded assets.
     */
    fun getLoadedAssetUrns(): Set<ResourceUrn> {
        return ImmutableSet.copyOf(loadedAssets.keys)
    }

    /**
     * @return A list of all the loaded assets.
     */
    fun getLoadedAssets(): Set<T> {
        return ImmutableSet.copyOf(loadedAssets.values)
    }

    /**
     * @return A set of the urns of all the loaded assets and all the assets available from producers
     */
    fun getAvailableAssetUrns(): Set<ResourceUrn> {
        val availableAssets: MutableSet<ResourceUrn> = Sets.newLinkedHashSet(getLoadedAssetUrns())
        for (producer in producers) {
            availableAssets.addAll(producer.availableAssetUrns)
        }
        return availableAssets
    }

    internal fun onAssetDisposed(asset: Asset<U>) {
        if (asset.urn.isInstance) {
            instanceAssets[asset.urn].remove(WeakReference(assetClass.java.cast(asset)))
        } else {
            loadedAssets.remove(asset.urn)
        }
    }

    init {
        val assetDataType = getTypeParameterBindingForInheritedClass(assetClass.java, Asset::class, 0)
        if (assetDataType.isEmpty) IllegalArgumentException("Asset class must have a bound AssetData parameter - " + assetClass)
        this.assetDataType = (assetDataType.get() as Class<U>).kotlin
    }

    private class ResourceLock(private val urn: ResourceUrn) {
        private val semaphore = Semaphore(1)
        @Throws(InterruptedException::class) fun lock() {
            semaphore.acquire()
        }

        fun unlock(): Boolean {
            val lockFinished = !semaphore.hasQueuedThreads()
            semaphore.release()
            return lockFinished
        }

        override fun toString(): String {
            return "lock($urn)"
        }
    }

    private class AssetReference<T>(asset: T, queue: ReferenceQueue<T>, private val disposalHook: DisposalHook) :
        PhantomReference<T>(asset, queue) {
        fun dispose() {
            disposalHook.dispose()
        }
    }

}