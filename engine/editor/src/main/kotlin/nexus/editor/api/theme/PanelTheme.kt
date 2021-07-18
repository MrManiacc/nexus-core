package nexus.editor.api.theme

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCol.*
import imgui.flag.ImGuiStyleVar.FramePadding
import imgui.flag.ImGuiStyleVar.WindowPadding
import nexus.engine.math.Vec4


class PanelTheme internal constructor() {
    private var styleVars = 0
    private var styleColors = 0

    /**
     * this will take the given theme and apply it
     */
    fun applyTheme(theme: Theme) =
        this.apply(theme.panelTheme)

    fun setWindowSize(width: Float, height: Float) =
        ImGui.setNextWindowSize(width, height)

    fun color(colorString: String) = Colors.color(colorString)
    fun setWindowPadding(width: Float, height: Float) = push(WindowPadding, width, height)
    fun setFramePadding(width: Float, height: Float) = push(FramePadding, width, height)


    fun setWindowPos(x: Float, y: Float) =
        ImGui.setNextWindowPos(x, y)


    fun push(targetId: Int, color: Vec4) {
        ImGui.pushStyleColor(targetId, color.x, color.y, color.z, color.w)
        styleColors++
    }


    fun push(targetId: Int, x: Float) {
        ImGui.pushStyleVar(targetId, x)
        styleVars++
    }

    fun push(targetId: Int, x: Float, y: Float) {
        ImGui.pushStyleVar(targetId, x, y)
        styleVars++
    }

    /**
     * This will set all text color to this
     */
    fun setTextColor(color: Vec4) {
        push(Text, color)
        push(TextDisabled, color)
    }


    /**
     * This will set all text color to this
     */
    fun setBodyColor(color: Vec4) {
        push(WindowBg, color)
        push(Border, color)
        push(BorderShadow, color)
        push(Tab, color)
        push(TabUnfocusedActive, color)
    }

    /**
     * This sets the main area color
     */
    fun setAreaColor(color: Vec4) {
        push(ChildBg, color)
        push(FrameBg, color)
        push(ScrollbarBg, color)
        push(TableRowBg, color)
        push(TabUnfocused, color)
    }


    fun setTitleBar(color: Vec4) {
        push(ImGuiCol.TitleBg, Vec4(0.10f, 0.09f, 0.12f, 1.00f))
        push(ImGuiCol.TitleBgCollapsed, Vec4(1.00f, 0.98f, 0.95f, 0.75f))
        push(ImGuiCol.TitleBgActive, Vec4(0.07f, 0.07f, 0.09f, 1.00f))
        push(ImGuiCol.MenuBarBg, Vec4(0.10f, 0.09f, 0.12f, 1.00f))
    }

    /**
     * This is the main color. Used for most things
     */
    fun setHeadColor(color: Vec4) {


        push(FrameBgHovered, color)
        push(FrameBgActive, color)
        push(ScrollbarGrab, color)
        push(ScrollbarGrabHovered, color)
        push(ScrollbarGrabActive, color)
        push(CheckMark, color)
        push(SliderGrab, color)
        push(SliderGrabActive, color)
        push(Button, color)
        push(ButtonHovered, color)
        push(ButtonActive, color)
        push(Header, color)
        push(HeaderActive, color)
        push(HeaderHovered, color)
        push(TableHeaderBg, color)
        push(ResizeGrip, color)
        push(ResizeGripActive, color)
        push(ResizeGripHovered, color)
        push(TextSelectedBg, color)
        push(TabHovered, color)
        push(TabActive, color)
        push(Separator, color)
        push(SeparatorActive, color)
        push(SeparatorHovered, color)
    }

    /**
     * This reset the state
     */
    internal fun reset() {
        if (styleVars > 0)
            ImGui.popStyleVar(styleVars)
        if (styleColors > 0)
            ImGui.popStyleColor(styleColors)
        styleVars = 0
        styleColors = 0
    }
}
