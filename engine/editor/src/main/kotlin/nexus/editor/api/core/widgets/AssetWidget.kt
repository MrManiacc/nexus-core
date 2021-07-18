package nexus.editor.api.core.widgets

import imgui.ImColor
import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import nexus.editor.api.Drawable
import nexus.editor.api.Transformable
import nexus.editor.api.assets.Icon
import nexus.editor.api.assets.Icons
import nexus.editor.api.impl.BaseElement
import nexus.editor.api.internal.WindowFlag
import nexus.engine.math.MathDSL.Extensions.by
import nexus.engine.math.Vec2

/**
 * This is used for creating a file widget
 */
class AssetWidget(
    /**
     * This gets translated to the required [nameId]. It is also used for displaying the correct
     *
     */
    fileName: String,
    /**
     * This is used to render the file widget
     */
    val icon: Icon = Icons.File.File,
    /**
     * This is used for setting the color of the buttong
     */
    color: String = "#6d7499",
    /**
     * This is used for when we hover over the button
     */
    hoverColor: String = "#636991",
    /**
     * This is used for when we hover over the button
     */
    activeColor: String = "#485182",
    /**
     * This allows us to have a custom size for the button
     */
    val size: Vec2 = 168f by 168f,
) : BaseElement(fileName), Transformable, Drawable {
    /**
     * We know we're an instance of a folder if we don't have an extension
     */
    val isFolder: Boolean = !fileName.contains(".")

    private val color: Int = ImColor.rgbToColor(color)
    private val hoverColor: Int = ImColor.rgbToColor(hoverColor)
    private val activeColor: Int = ImColor.rgbToColor(activeColor)

    /**
     * When false, this node wont be renderd in the Container node's default renderChildren method.
     */
    override val batchRender: Boolean = false

    /**
     * This method actually is used to render the nodes
     */
    override fun render() {
        ImGui.pushStyleColor(ImGuiCol.Button, this.color)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, this.activeColor)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, this.hoverColor)
        if (ImGui.imageButton(icon.renderId, size.x, size.y))
            println("clicked $displayName")
        ImGui.popStyleColor(3)
        ImGui.text("${nameId.fileName}.${nameId.fileExtension}")
    }

    override fun toString(): String {
        return "AssetWidget(id=${displayName}, icon=$icon, color=$color, hoverColor=$hoverColor, activeColor=$activeColor, flags=$flags)"
    }

    /**
     * This will render the asset inline instead of
     */
    fun renderInLine() {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0f, 15f)
        ImGui.setCursorPosY(cursorY + 5f)
        ImGui.selectable("##${nameId.uniqueId}.${nameId.fileExtension}")
        ImGui.sameLine()
        ImGui.setCursorPosY(cursorY - 5f)
        ImGui.setCursorPosX(cursorX + 5f)
        ImGui.image(icon.renderId, 24f, 24f)
        ImGui.popStyleVar()
        ImGui.sameLine()
        ImGui.setCursorPosY(cursorY + 5f)
        ImGui.textUnformatted(nameId.fileName)
        ImGui.setCursorPosY(cursorY - 5f)
    }


    /**
     * This should be created from the enum values for the flags
     */
    override val flags: Int = WindowFlag.None()


}