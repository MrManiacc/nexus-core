package nexus.editor.gui.core.widgets

import imgui.ImColor
import imgui.ImGui
import imgui.flag.ImGuiCol
import nexus.editor.gui.Drawable
import nexus.editor.gui.Node
import nexus.editor.gui.Transformable
import nexus.editor.gui.icons.Icon
import nexus.editor.gui.icons.Icons
import nexus.editor.gui.internal.ID
import nexus.editor.gui.internal.WindowFlag

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
) : Node, Transformable, Drawable {

    private val color: Int = ImColor.rgbToColor(color)
    private val hoverColor: Int = ImColor.rgbToColor(hoverColor)
    private val activeColor: Int = ImColor.rgbToColor(activeColor)

    /**
     * This is used as a name and id. Example: testfolder/subfolder/test.png
     */
    override val nameId: ID = ID(fileName)

    /**
     * This method actually is used to render the nodes
     */
    override fun render() {
        ImGui.pushStyleColor(ImGuiCol.Button, this.color)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, this.activeColor)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, this.hoverColor)
        if (ImGui.imageButton(icon.renderId, 256f, 256f))
            println("clicked $displayName")
        ImGui.popStyleColor(3)
        ImGui.text("${nameId.fileName}.${nameId.fileExtension}")
    }


    /**
     * This should be created from the enum values for the flags
     */
    override val flags: Int = WindowFlag.None()
}