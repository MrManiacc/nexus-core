package nexus.plugin.extensions

import kotlin.reflect.KClass

/**
 * This is used to scan the classpath to find extension clases
 */
class ExtensionScanner(val key: ExtensionKey) {
    /**
     * this method attempts to find any kclass's on the classpath that have an annotation with the given key
     */
    fun scan(): List<KClass<*>> {
        return emptyList()
    }

}