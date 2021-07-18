package nexus.editor.api.impl

import imgui.ImGui
import imgui.flag.ImGuiStyleVar
import imgui.internal.flag.ImGuiDockNodeFlags.DockSpace
import imgui.type.ImInt
import mu.KotlinLogging
import nexus.editor.api.Dockable
import nexus.editor.api.Element
import nexus.editor.api.ToolPanel
import nexus.editor.api.Workspace
import nexus.editor.api.events.UIEvents
import nexus.editor.api.internal.DockFlag
import nexus.editor.api.internal.ID
import nexus.editor.api.internal.WindowFlag
import nexus.engine.Application
import org.slf4j.Logger
import imgui.internal.ImGui as ImGuiInternal

/**
 * This is a dockspace node. It should only be created to allow for adding of child windows
 */
class DockspaceElement(
    val app: Application<*>,
    /**
     * This is used to create the [ID] instance for the dockspacel
     */
    id: String,
    /**
     * This by default creates all of the needed flags for an unmovable dockspace window.
     */
    flags: Array<out WindowFlag> = arrayOf(
        WindowFlag.NoTitleBar, WindowFlag.NoCollapse, WindowFlag.NoResize,
        WindowFlag.NoMove, WindowFlag.NoBringToFrontOnFocus
    ),
    /**
     * This provides flags that are used for the actual dockspacce builderl
     */
    dockspaceFlags: Array<DockFlag> = arrayOf(
        DockFlag.None
    ),
    /**
     * This is used for rendering the menu bar
     */
    menuBar: Element? = null,
) : ContainerElement(id, flags), Workspace {
    private val logger: Logger = KotlinLogging.logger { }

    /**
     * This allows us to override the menu bar providing our own
     */
    override val menuBar: Element = menuBar ?: ElementEmpty

    /**
     * This store the flags for the dockspace, which are created via [DockFlag]
     */
    override val dockspaceFlags: Int = DockFlag.combine(dockspaceFlags)

    /**
     * THis is used to recreate/initialize the dockspace. This will use imgui's internal dockspace builder system
     * to create a infomative easy to use api without the need of having to work with imgui directly
     */
    override fun invalidateDockspace() {
        logger.debug("Invalidating dockspace, found ${this.count} children.")
        val viewport = ImGui.getWindowViewport()
        ImGuiInternal.dockBuilderRemoveNode(dockspaceID)
        ImGuiInternal.dockBuilderAddNode(dockspaceID, DockSpace)
        ImGuiInternal.dockBuilderSetNodeSize(dockspaceID, viewport.sizeX, viewport.sizeY)
        val dockMainId = ImInt(dockspaceID)
        find(Dockable::class).forEach {
            it.dock(dockMainId)
        }
        ImGuiInternal.dockBuilderFinish(dockspaceID)

    }

    private var lastActivePanel: ID = ID("")

    /**
     * This gets the selected node. This will
     */
    override val activePanel: Element
        get() {
            for (panel in find(ToolPanel::class)) {
                if (panel.isFocused)
                    return panel
            }
            return ElementEmpty
        }

    /**
     * This is the internal method that will do the actual rendering
     */
    override fun render() {
        if (lastActivePanel != activePanel.nameId) {
            val active = activePanel
            lastActivePanel = active.nameId
            app.publish(UIEvents.NodeFocusSwitched(active))
        }
        val viewport = ImGui.getWindowViewport()
        ImGui.setNextWindowPos(viewport.posX, viewport.posY)
        ImGui.setNextWindowSize(viewport.workSizeX, viewport.workSizeY)
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
        ImGui.begin(displayName, flags)///Uses our window flags
        ImGui.setNextWindowViewport(viewport.id)
        ImGui.popStyleVar()
        var dockspaceID = ImGui.getID(nameId.id)
        val node = ImGuiInternal.dockBuilderGetNode(dockspaceID)
        if (node == null || node.ptr == 0L || node.id == 0) //Null ptr? it we should now create?
            invalidateDockspace()
        dockspaceID = ImGui.getID(nameId.id)
        ImGui.dockSpace(dockspaceID, 0f, 0f, dockspaceFlags)
        ImGui.end()
        //This wil render our children..
        renderChildren()



    }


}