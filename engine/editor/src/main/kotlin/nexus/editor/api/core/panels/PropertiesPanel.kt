package nexus.editor.api.core.panels

import imgui.ImGui
import nexus.editor.api.impl.AbstractToolPanel
import nexus.editor.api.internal.Anchor
import nexus.editor.api.internal.DockFlag
import nexus.engine.Application
import nexus.engine.render.RenderAPI

/**
 * This panel is used for the main viewport
 */
class PropertiesPanel<API : RenderAPI>(val app: Application<API>) : AbstractToolPanel(
    id = "nexus.editor.properties",
    anchor = Anchor.Right,
    sizeRatio = 0.15f,
    dockFlags = arrayOf(DockFlag.AutoHideTabBar)
) {



    /**
     * This should render the content of the tool window.
     */
    override fun renderContent() {
        ImGui.text("Properties ---->")
    }
}