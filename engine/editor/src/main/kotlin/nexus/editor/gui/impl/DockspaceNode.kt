package nexus.editor.gui.impl

import imgui.ImGui
import imgui.flag.ImGuiStyleVar
import imgui.type.ImInt
import mu.KotlinLogging
import nexus.editor.gui.Dockable
import nexus.editor.gui.Node
import nexus.editor.gui.Workspace
import nexus.editor.gui.internal.DockFlag
import nexus.editor.gui.internal.WindowFlag
import org.slf4j.Logger
import imgui.internal.ImGui as ImGuiInternal

/**
 * This is a dockspace node. It should only be created to allow for adding of child windows
 */
class DockspaceNode(
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
    menuBar: Node? = null,
) : ContainerNode(id, flags), Workspace {
    private val logger: Logger = KotlinLogging.logger { }

    /**
     * This allows us to override the menu bar providing our own
     */
    override val menuBar: Node = menuBar ?: NodeEmpty

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
        ImGuiInternal.dockBuilderAddNode(dockspaceID, imgui.internal.flag.ImGuiDockNodeFlags.DockSpace)
        ImGuiInternal.dockBuilderSetNodeSize(dockspaceID, viewport.sizeX, viewport.sizeY)
        val dockMainId = ImInt(dockspaceID)
        findRecursive(Dockable::class).forEach {
            it.dock(dockMainId)
        }
        ImGuiInternal.dockBuilderFinish(dockspaceID)
    }


    /**
     * This is the internal method that will do the actual rendering
     */
    override fun render() {
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