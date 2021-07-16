package nexus.engine.service

import mu.KotlinLogging
import nexus.plugin.Plugin
import org.slf4j.Logger

/**
 * This is a delagate for a given plugin service
 */
class Services(pluginId: String) {
    val pluginId = Plugin.PluginID(pluginId)
    val logger: Logger = KotlinLogging.logger { }

//    operator fun getValue(thisRef: Any?, property: KProperty<*>): ExtensionWrapper {
//        val serviceManager: ServiceManager = Application.instance
//        val plugin = serviceManager.get(this.pluginId)
//        if (plugin == null) {
//            logger.error("failed to find plugin with pluginID: $pluginId")
//            throw InvalidServiceException("Failed to find plugin ")
//        }
//        return plugin.manager[ExtensionKey(property.name)]
//    }
}

