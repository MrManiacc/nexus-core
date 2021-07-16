package nexus.plugin.extensions

/**
 * This is used for specifying a specific
 */
@JvmInline value class ExtensionKey(val key: String) {
    override fun toString(): String {
        return "ExtensionKey($key)"
    }
}