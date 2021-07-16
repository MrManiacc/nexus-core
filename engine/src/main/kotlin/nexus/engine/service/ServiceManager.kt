package nexus.engine.service

import mu.KotlinLogging
import nexus.document.DocumentScanner
import nexus.plugin.Plugin
import org.slf4j.Logger

/**
 * This class is responsible for keeping track of all of the active services.
 */
@Suppress("UNCHECKED_CAST")
abstract class ServiceManager {
    private val logger: Logger = KotlinLogging.logger { }
    private val scanner = DocumentScanner()
    private val plugins: MutableMap<Plugin.PluginID, Plugin> = HashMap()

    /**
     * This will scan the classpath for interfaces that extend [Services] and for implementations of said interfaces.
     */
    fun invalidate() {
        plugins.clear()
        logger.info("Starting the scan of documents...")
        val documents = scanner.scan()
        documents.forEach {
            it.metas.forEach { meta ->
                val plugin = Plugin.create(meta)
                plugins[plugin.id] = plugin
                logger.info("Created a new plugin[${plugin.id}] from meta: $meta")
            }
        }
    }

     fun get(pluginID: Plugin.PluginID): Plugin? = plugins[pluginID]


}