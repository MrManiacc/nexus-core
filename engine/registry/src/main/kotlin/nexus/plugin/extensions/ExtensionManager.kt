package nexus.plugin.extensions

/**
 * This should be able to get the instance of an extension at runtime after the initialization of the extension library
 */
class ExtensionManager(val namespace: String) {
    private val extensions: MutableMap<ExtensionKey, ExtensionWrapper> = HashMap()

    /**
     * this gets or creates a new extensions wrapper for the given key
     */
    operator fun get(key: ExtensionKey): ExtensionWrapper =
        extensions.getOrPut(key) { ExtensionWrapper() }


}