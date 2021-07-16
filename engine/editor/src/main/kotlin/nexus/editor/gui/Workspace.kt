package nexus.editor.gui

import imgui.ImGui
import nexus.editor.gui.container.MutableContainer
import nexus.editor.gui.impl.NodeEmpty

/**
 * This is basically the editor's equivalent of an imgui dockpace. It uses various things from the engine
 */
interface Workspace : MutableContainer {
    /**
     * This store the flags for the dockspace, which are created via [DockFlag]
     */
    val dockspaceFlags: Int

    /**
     * This should be the id of the dockspace. It is derrived from the [nameId] id via ImGui's internal
     * ID system.
     */
    val dockspaceID: Int
        get() = ImGui.getID(this.nameId.id)

    /**
     * This is used for rendering the menuBar at the top of the window. If it's empty we won't render it
     */
    val menuBar: Node
        get() = NodeEmpty

    /**
     * THis is used to recreate/initialize the dockspace. This will use imgui's internal dockspace builder system
     * to create a infomative easy to use api without the need of having to work with imgui directly
     */
    fun invalidateDockspace()

    /**
     * This is used for rendering our children. This should be overriden to specify any dockspace specific settings.
     */
    override fun render() {
        renderChildren()
    }

}