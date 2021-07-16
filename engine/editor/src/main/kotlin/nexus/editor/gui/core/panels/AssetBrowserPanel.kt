package nexus.editor.gui.core.panels

import nexus.editor.gui.Node
import nexus.editor.gui.core.widgets.AssetWidget
import nexus.editor.gui.impl.BaseToolPanel
import nexus.editor.gui.internal.Anchor
import nexus.editor.gui.internal.DockFlag
import nexus.engine.Application
import nexus.engine.render.RenderAPI

/**
 * This panel is used for the main viewport
 */
class AssetBrowserPanel<API : RenderAPI>(val app: Application<API>) : BaseToolPanel(
    id = "nexus.editor.assets",
    anchor = Anchor.Down,
    sizeRatio = 0.33f,
    dockFlags = arrayOf(DockFlag.AutoHideTabBar),
) {
    override fun addedTo(parent: Node) {
        add(AssetWidget("assets/images/test.png"))
        add(AssetWidget("assets/images/test2.png"))
    }


    /**
     * This should render the content of the tool window.
     */
    override fun renderContent() {
    }
}