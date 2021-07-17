package nexus.project

import nexus.plugin.Plugin
import nexus.plugin.PluginDiscovery

/**
 * A project represents a runnable instance keen to a specific lifecyle of the application.
 * It allows for management over all of the plugins their and extensions.
 */
abstract class Project(
    /**
     * This is used to discover all of the plugins on path.
     */
    val discovery: PluginDiscovery,
) {
    /**
     * This stores the actual reference to our plugins
     */
    private var plugins: MutableCollection<Plugin> = ArrayList()



    /**
     * This refreshes all of our plugins
     */
    fun refresh() {
        plugins.clear()
        plugins.addAll(discovery.discover())
    }

    init {
        refresh()
    }
}