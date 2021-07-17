package nexus.plugin

import nexus.plugin.discover.PluginDiscoveryImpl

/**
 * This uses the class path to find and load all plugins. It does this by matching a resources "plugin.xml" file.
 * After locating all "plugin.xml" files it then maps all of the classes (they must start with a unique package that
 * maps to the id). Projects stores all of the plugins on the class path.
 */
interface PluginDiscovery {

    /**
     * This should locate all of the plugins on the classpath
     */
    fun discover(): Collection<Plugin>


    companion object {
        operator fun invoke(): PluginDiscovery = PluginDiscoveryImpl()
    }
}