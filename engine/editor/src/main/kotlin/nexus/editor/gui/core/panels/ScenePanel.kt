package nexus.editor.gui.core.panels

import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import nexus.editor.gui.impl.AbstractToolPanel
import nexus.editor.gui.internal.Anchor
import nexus.editor.gui.internal.DockFlag
import nexus.editor.gui.theme.PanelTheme
import nexus.engine.Application
import nexus.engine.render.RenderAPI

/**
 * This panel is used for the main viewport
 */
class ScenePanel<API : RenderAPI>(val app: Application<API>) : AbstractToolPanel(
    id = "nexus.editor.scene",
    anchor = Anchor.Left,
    sizeRatio = 0.2f,
    dockFlags = arrayOf(DockFlag.AutoHideTabBar)
) {

    /**
     * This allow for the user to customize the window before it's creation
     */
    override fun PanelTheme.customize() {
        push(ImGuiStyleVar.FrameRounding, 10f)
        push(ImGuiCol.ButtonHovered, color("#ffffff"))
    }

    /**
     * This should render the content of the tool window.
     */
    override fun renderContent() {
        //TODO render the actual scene.
    }
}