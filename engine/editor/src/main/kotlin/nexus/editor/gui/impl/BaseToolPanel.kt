package nexus.editor.gui.impl

import imgui.ImGui
import imgui.type.ImInt
import nexus.editor.gui.Dockable
import nexus.editor.gui.ToolPanel
import nexus.editor.gui.internal.Anchor
import nexus.editor.gui.internal.DockFlag
import nexus.editor.gui.internal.ID
import nexus.editor.gui.internal.WindowFlag
import nexus.editor.gui.theme.PanelTheme
import nexus.editor.gui.theme.Theme
import nexus.editor.gui.theme.Themes

/**
 * This is used for the creationg of a base tool panel.
 */
abstract class BaseToolPanel(
    /**
     * This gets mapped to a [ID], the [nameID] is derived via this [id]
     */
    id: String,
    /**
     * This is used to map our [anchor] input value to the actual anchor value found in the [ToolPanel]
     */
    override val anchor: Anchor = Anchor.None,
    /**
     * This is the size of the panel 0.15 will be 15% of the parent and appropriate
     * for side panels.
     */
    override val sizeRatio: Float = 0.15f,
    /**
     * Allows us to specify n'th number of flags
     */
    flags: Array<WindowFlag> = arrayOf(WindowFlag.NoCollapse, WindowFlag.NoBringToFrontOnFocus),

    /**
     * Allows us to specify n'th number of flags
     */
    dockFlags: Array<DockFlag> = arrayOf(DockFlag.AutoHideTabBar),
    /**
     * This stores all of the default themes we wish to use.
     * Pass empty array to have complete theme control or your own theme
     */
    private val themes: Array<Theme> = arrayOf(Themes.BaseTheme)

) : ContainerNode(id, flags), ToolPanel {
    private val properties = PanelTheme()

    override val dockFlags: Int = DockFlag.combine(dockFlags)

    override fun dock(dockspaceID: ImInt): ImInt {
        val dockspace = super.dock(dockspaceID)
        forEach {
            if (it is Dockable) {
                it.dock(dockspace)
            }
        }
        return dockspace
    }

    /**
     * This allow for the user to customize the window before it's creation
     */
    protected open fun PanelTheme.customize() {}


    /**
     * This method actually is used to render the nodes
     */
    override fun render() {
        properties.apply {
            themes.forEach {
                this.applyTheme(it)
            }
            customize()
        }
        if (ImGui.begin(this.displayName, flags)) {
            renderContent()
            renderChildren()
            properties.reset()
        }
        ImGui.end()
    }

    /**
     * This should render the content of the tool window.
     */
    protected abstract fun renderContent()

}
