package nexus.editor.gui.ext

import nexus.editor.gui.impl.DockspaceNode
import nexus.plugin.Plugin

/**
 * This is used for allowing the creation of a dockspacel
 */
interface DockspaceFactory {
    fun createDockspace(plugin: Plugin): DockspaceNode
}