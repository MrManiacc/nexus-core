package nexus.engine.assets

/**
 * Interface for a resource that can be disposed. This is used by asset to register a resource
 * to be disposed of when an asset is disposed, or after it is garbage collected.
 */
interface DisposableResource : AutoCloseable {
    /**
     * Closes the asset. It is expected this should only happen once.
     */
    override fun close()
}
