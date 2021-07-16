package nexus.editor.gui.core.panels

import imgui.ImGui
import nexus.editor.gui.impl.BaseToolPanel
import nexus.editor.gui.internal.Anchor
import nexus.editor.gui.internal.DockFlag
import nexus.editor.gui.internal.WindowFlag
import nexus.editor.gui.theme.PanelTheme
import nexus.engine.Application
import nexus.engine.events.Events
import nexus.engine.math.MathDSL.Extensions.by
import nexus.engine.math.Vec2
import nexus.engine.render.RenderAPI
import nexus.engine.render.framebuffer.FramebufferFormat

/**
 * This panel is used for the main viewport
 */
class ViewportPanel<API : RenderAPI>(val app: Application<API>) : BaseToolPanel(
    "nexus.editor.viewport", Anchor.Center,
    flags = arrayOf(WindowFlag.NoCollapse, WindowFlag.NoBringToFrontOnFocus),
    dockFlags = arrayOf(DockFlag.AutoHideTabBar)
) {
    private val viewportSize: Vec2 = 1920 by 1080f

    /**
     * This allow for the user to customize the window before it's creation
     */
    override fun PanelTheme.customize() {
        setFramePadding(3f, 3f)
        setWindowPadding(0f, 0f)
    }

    /**
     * This should render the content of the tool window.
     */
    override fun renderContent() {

        //This is used to render our actual viewport panel
        val size = ImGui.getContentRegionAvail()
        if (size.x != viewportSize.x || size.y != viewportSize.y) {
            viewportSize.set(size.x, size.y)
            app.publish(Events.Camera.Resize(size.x.toInt(), size.y.toInt()))
        }
        app.viewport[FramebufferFormat.Attachment.ColorImage]?.renderID?.let {
            ImGui.image(it, viewportSize.x, viewportSize.y)
        }
    }
}