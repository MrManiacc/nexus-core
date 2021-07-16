package nexus.plugin

import nexus.plugin.extensions.ExtensionKey
import nexus.plugin.extensions.ExtensionManager
import nexus.plugin.extensions.ExtensionWrapper
import kotlin.reflect.full.createInstance

/**
 * A plugin is meant to encapsulate a group of extension points.
 */
interface Plugin {
    /**
     * The id of the plugin. This is mapped from the <id> <id/> node withing the plugin.xml
     */
    val id: PluginID

    /**
     * This stores all of the extensions. This should be processed and created at runtime, making sure to load
     * all of the classes
     */
    val manager: ExtensionManager

    /**
     * A simply helper for getting the extensions for the given key
     */
    operator fun get(key: String): ExtensionWrapper = manager[ExtensionKey(key)]



    /**
     * This is used to uniquely identify a plugin. The namespace should be auto generated,
     * where the last value is the actual name of the plugin. The name, meaning the last item after
     * the last comma, should be uppercase indicating the name, while the namespace, meaning everything
     * before the last comma, which should be path separated via periods, should be lowercase.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    @JvmInline value class PluginID(val id: String) {
        val namespace: String get() = id.substringBeforeLast(".", id)
        val name: String get() = id.substringAfterLast(".", id)
        override fun toString(): String = "PluginID(namespace=$namespace, name=$name, id=$id)"
    }

    /**
     * This is used for creating a plugin from the
     */
    data class PluginImpl internal constructor(
        /**
         * The id of the plugin. This is mapped from the <id> <id/> node withing the plugin.xml
         */
        override val id: PluginID,

        /**
         * This stores all of the extensions. This should be processed and created at runtime, making sure to load
         * all of the classes
         */
        override val manager: ExtensionManager,
    ) : Plugin

    /**
     * Provides static creation methods for plugins
     */
    companion object {
        /**
         * This will generate a new plugin based upon the plugin meta
         */
        fun create(pluginMeta: PluginMeta): Plugin {
            val manager = ExtensionManager(pluginMeta.extensions.namespace)
            pluginMeta.extensions.extensionList.forEach {
                val key = ExtensionKey(it.name)
                if (it is PluginMeta.Extensions.ImplementationExtension) {
                    val kclass = Class.forName(it.targetClass).kotlin
                    manager[key].add(kclass.createInstance())
                } else if (it is PluginMeta.Extensions.InterfaceExtension) {
                    try {
                        val kclass = Class.forName(it.targetClass).kotlin
                        manager[key].add(kclass)
                    } catch (ex: Exception) {
                        error("caught exception: ${ex.message}")
                    }
                }
            }
            return PluginImpl(PluginID(pluginMeta.id), manager)
        }
    }


}