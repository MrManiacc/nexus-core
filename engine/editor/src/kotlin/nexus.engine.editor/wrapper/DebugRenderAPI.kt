package nexus.engine.editor.wrapper

import imgui.*
import imgui.extension.nodeditor.*
import imgui.flag.*
import imgui.gl3.*
import imgui.glfw.*
import imgui.type.*
import marx.engine.nexus.plugins.glfw.IWindow
import marx.engine.scene.*
import marx.engine.utils.MathUtils.orEquals
import nexus.plugins.opengl.*
import org.lwjgl.glfw.*
import java.lang.IllegalStateException
import imgui.internal.ImGui as ImGuiInternal
import imgui.internal.flag.ImGuiDockNodeFlags as ImGuiDockNodeInternalFlags

/*
 * This is kind of a wrapper around imgui for ease of use with kotlin.
 */
class DebugRenderAPI(
    window: IWindow,
    scene: RenderScene
) : GLRenderAPI(window, scene) {
    /*The constant dockspace id for the main dockspace.**/
    private val dockspaceId = "main_dockspace"

    /*Dynamically gets the window long reference.**/
    private val handle: Long get() = window.handle

    /*This stores the glfw backend implementation for imgui**/
    private val imGuiGlfw = ImGuiImplGlfw()

    /*This stores the opengl backend implementation for imgui**/
    private val imGuiGl3 = ImGuiImplGl3()

    /*Used for getting the content region**/
    private val minBuffer = ImVec2()

    /*Used for getting the content region**/
    private val maxBuffer = ImVec2()

    private lateinit var nodeEditor: NodeEditorContext
    private var init = false

    /*
   This will initialize the gui
     */

    override fun init() {
        if (!init) {
            initImGui()
            imGuiGlfw.init(handle, true);
            nodeEditor = NodeEditor.createEditor() ?: throw IllegalStateException("Failed to create node editor context")
            imGuiGl3.init("#version 120"); //Use version of #330 for mac support (minium required version otherwise exception on mac)
            init = true
        }
    }

    /*
   This will initialize the imgui stuff
     */
    private fun initImGui() {
        ImGui.createContext();
        setupStyle(ImGui.getStyle())
        val io = ImGui.getIO();
        io.iniFilename = null
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleViewports)
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts)
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)
        io.configViewportsNoTaskBarIcon = true
    }

    /*
   This initializes our style.
     */
    private fun setupStyle(style: ImGuiStyle) {
        style.windowPadding.set(15f, 15f)
        style.windowRounding = 5.0f
        style.framePadding.set(5.0f, 5.0f)
        style.itemSpacing.set(12.0f, 8.0f)
        style.itemInnerSpacing.set(8f, 6f)
        style.indentSpacing = 25f
        style.scrollbarSize = 15.0f
        style.scrollbarRounding = 9.0f
        style.grabRounding = 3.0f
        setColor(ImGuiCol.Text, ImVec4(0.80f, 0.80f, 0.83f, 1.00f))
        setColor(ImGuiCol.TextDisabled, ImVec4(0.24f, 0.23f, 0.29f, 1.00f))
        setColor(ImGuiCol.WindowBg, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.ChildBg, ImVec4(0.07f, 0.07f, 0.09f, 1.00f))
        setColor(ImGuiCol.PopupBg, ImVec4(0.07f, 0.07f, 0.09f, 1.00f))
        setColor(ImGuiCol.Border, ImVec4(0.80f, 0.80f, 0.83f, 0.88f))
        setColor(ImGuiCol.BorderShadow, ImVec4(0.92f, 0.91f, 0.88f, 0.00f))
        setColor(ImGuiCol.FrameBg, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.FrameBgHovered, ImVec4(0.24f, 0.23f, 0.29f, 1.00f))
        setColor(ImGuiCol.FrameBgActive, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.TitleBg, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.TitleBgCollapsed, ImVec4(1.00f, 0.98f, 0.95f, 0.75f))
        setColor(ImGuiCol.TitleBgActive, ImVec4(0.07f, 0.07f, 0.09f, 1.00f))
        setColor(ImGuiCol.MenuBarBg, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.ScrollbarBg, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.ScrollbarGrab, ImVec4(0.80f, 0.80f, 0.83f, 0.31f))
        setColor(ImGuiCol.ScrollbarGrabHovered, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.ScrollbarGrabActive, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.CheckMark, ImVec4(0.80f, 0.80f, 0.83f, 0.31f))
        setColor(ImGuiCol.SliderGrab, ImVec4(0.80f, 0.80f, 0.83f, 0.31f))
        setColor(ImGuiCol.SliderGrabActive, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.Button, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.ButtonHovered, ImVec4(0.24f, 0.23f, 0.29f, 1.00f))
        setColor(ImGuiCol.ButtonActive, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.Header, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.HeaderHovered, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.HeaderActive, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.ResizeGrip, ImVec4(0.00f, 0.00f, 0.00f, 0.00f))
        setColor(ImGuiCol.ResizeGripHovered, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.ResizeGripActive, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.PlotLines, ImVec4(0.40f, 0.39f, 0.38f, 0.63f))
        setColor(ImGuiCol.PlotLinesHovered, ImVec4(0.25f, 1.00f, 0.00f, 1.00f))
        setColor(ImGuiCol.PlotHistogram, ImVec4(0.40f, 0.39f, 0.38f, 0.63f))
        setColor(ImGuiCol.PlotHistogramHovered, ImVec4(0.25f, 1.00f, 0.00f, 1.00f))
        setColor(ImGuiCol.TextSelectedBg, ImVec4(0.25f, 1.00f, 0.00f, 0.43f))
        setColor(ImGuiCol.ModalWindowDimBg, ImVec4(1.00f, 0.98f, 0.95f, 0.73f))
    }

    /*
   This sets a color for imgui
     */
    private fun setColor(
        colorIndex: Int,
        color: ImVec4
    ) {
        val style = ImGui.getStyle()
        style.setColor(colorIndex, color.x, color.y, color.z, color.w)
    }

    /*
   This will begin the imigui frame
     */
    private fun startFrame() {
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    /*
   This is a magic kotlin wrapper for a frame start with an automatic end.
     */
    fun frame(frame: () -> Unit) {
        startFrame()
        frame()
        endFrame()
    }

    /*
   This will begin and end a node graph
     */
    fun nodeGraph(
        name: String,
        draw: () -> Unit
    ) {
        NodeEditor.begin("node_graph_$name")
        draw()
        NodeEditor.end()
    }

    /*Gets the center position**/
    fun getContentCenter(): ImVec2 {
        ImGui.getWindowContentRegionMin(minBuffer)
        ImGui.getWindowContentRegionMax(maxBuffer)
        val minX = ImGui.getWindowPosX() + minBuffer.x
        val minY = ImGui.getWindowPosY() + minBuffer.y
        val maxX = ImGui.getWindowPosX() + maxBuffer.x
        val maxY = ImGui.getWindowPosY() + maxBuffer.y
        return ImVec2((minX + maxX) / 2f, (minY + maxY) / 20f)
    }

    /*
   This will end the imgui frame
     */
    private fun endFrame() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            val backupPtr = GLFW.glfwGetCurrentContext()
            ImGui.updatePlatformWindows()
            ImGui.renderPlatformWindowsDefault()
            GLFW.glfwMakeContextCurrent(backupPtr)
        }
    }

    /*
   This will create the fullscreen dock-space window.
     */
    fun dockspace(
        name: String,
        nodes: () -> Unit,
        editor: () -> Unit
    ) {
        val flags = ImGuiWindowFlags.NoNavFocus.orEquals(
            ImGuiWindowFlags.NoTitleBar,
            ImGuiWindowFlags.NoCollapse,
            ImGuiWindowFlags.NoResize,
            ImGuiWindowFlags.NoMove,
            ImGuiWindowFlags.NoBringToFrontOnFocus
        )
        val viewport = ImGui.getWindowViewport()
        ImGui.setNextWindowPos(viewport.posX, viewport.posY)
        ImGui.setNextWindowSize(viewport.workSizeX, viewport.workSizeY)
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
        ImGui.begin("Window##$name", flags)
        ImGui.setNextWindowViewport(viewport.id)
        ImGui.popStyleVar()

        var dockspaceID = ImGui.getID(dockspaceId)
        val node = ImGuiInternal.dockBuilderGetNode(dockspaceID)
        if (node == null || node.ptr == 0L || node.id == 0) //Null ptr? it we should now create?
            createDock(name)
        dockspaceID = ImGui.getID(dockspaceId)
        ImGui.dockSpace(dockspaceID, 0f, 0f, ImGuiDockNodeFlags.None)
        ImGui.end()
        ImGui.begin("Editor##$name", ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoBringToFrontOnFocus)
        editor()
        ImGui.end()
        ImGui.begin("Nodes##$name", ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoBringToFrontOnFocus)
        nodes()
        ImGui.end()
    }

    /*
   This internally creates the dock when it's not present.
     */
    private fun createDock(name: String) {
        val viewport = ImGui.getWindowViewport()
        val dockspaceID = ImGui.getID(dockspaceId)
        ImGuiInternal.dockBuilderRemoveNode(dockspaceID)
        ImGuiInternal.dockBuilderAddNode(dockspaceID, ImGuiDockNodeInternalFlags.DockSpace)
        ImGuiInternal.dockBuilderSetNodeSize(dockspaceID, viewport.sizeX, viewport.sizeY)
        val dockMainId = ImInt(dockspaceID)
        val dockLeft: Int =
            ImGuiInternal.dockBuilderSplitNode(dockMainId.get(), ImGuiDir.Right, 0.15f, null, dockMainId)
        ImGuiInternal.dockBuilderDockWindow("Editor##$name", dockMainId.get())
        ImGuiInternal.dockBuilderDockWindow("Nodes##$name", dockLeft)
        ImGuiInternal.dockBuilderFinish(dockspaceID)
    }

    /*Called upon unloading of the given renderAPI **/
    override fun dispose() {
        ImGui.destroyContext()
        imGuiGlfw.dispose()
        imGuiGl3.dispose()
        init = false
    }
}
