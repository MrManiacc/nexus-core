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
abstract class AbstractToolPanel(
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
    private val themes: Array<Theme> = arrayOf(Themes.BaseTheme),
) : ContainerElement(id, flags), ToolPanel {
    /**
     * This uses the input dock flags to create the dock instance
     */
    override val dockFlags: Int = DockFlag.combine(dockFlags)

    /**
     * This is used for theming this panel
     */
    private val properties = PanelTheme()

    /**Imgui window related propertiesl**/
    private var appearing = false
    private var docked = false
    private var focused = false
    private var hovered = false



    /**
     * This is called only once recursively from the parents about the docking
     */
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
            this.appearing = ImGui.isWindowAppearing()
            this.docked = ImGui.isWindowDocked()
            this.focused = ImGui.isWindowFocused()
            this.hovered = ImGui.isWindowFocused()
            renderContent()
            renderChildren()
        }
        ImGui.end()
        properties.reset()
    }

    /**
     * This should return true only when the tool window has im gui's focus.
     * this is to be used for input related activies
     */
    override val isFocused: Boolean
        get() = focused

    /**
     * This should return true only when the tool window has im gui's focus.
     * this is to be used for input related activies
     */
    override val isHovered: Boolean
        get() = hovered

    /**
     * This should be true when the window is actually visble to the user
     */
    override val isAppearing: Boolean
        get() = appearing

    /**
     * This should be true when the window is docked within another window
     */
    override val isDocked: Boolean
        get() = docked

    /**
     * This should render the content of the tool window.
     */
    protected abstract fun renderContent()

    override fun toString(): String =
        "${this.javaClass.simpleName}(naem=${displayName}, anchor=$anchor, sizeRatio=$sizeRatio, isFocused=$isFocused, isHovered=$isHovered, isAppearing=$isAppearing, isDocked=$isDocked)"


}
