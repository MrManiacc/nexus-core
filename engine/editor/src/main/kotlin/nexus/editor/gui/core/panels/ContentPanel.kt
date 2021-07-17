package nexus.editor.gui.core.panels

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import nexus.editor.gui.Drawable
import nexus.editor.gui.Element
import nexus.editor.gui.assets.Icons
import nexus.editor.gui.core.widgets.AssetWidget
import nexus.editor.gui.impl.AbstractToolPanel
import nexus.editor.gui.internal.Anchor
import nexus.editor.gui.internal.DockFlag
import nexus.engine.Application
import nexus.engine.assets.AssetTypeManager
import nexus.engine.assets.invoke
import nexus.engine.render.RenderAPI
import java.nio.file.Path

/**
 * This panel is used for the main viewport
 */
class ContentPanel<API : RenderAPI>(
    val app: Application<API>,
    /**
     * This is used to recursively build a tree of [AssetWidget]
     */
    val path: Path,
) : AbstractToolPanel(
    id = "nexus.editor.assets",
    anchor = Anchor.Down,
    sizeRatio = 0.33f,
    dockFlags = arrayOf(DockFlag.AutoHideTabBar),
), Drawable {
    private val upDirButton = AssetWidget("...", Icons.Folder.Up)

    init {
        app.subscribe(this)
    }

    /**
     * Adds our test data
     */
    override fun addedTo(parent: Element) {
        AssetTypeManager().source.files.filter { it.name.endsWith(".png") || it.name.endsWith(".xml") || it.name.endsWith("jpeg") }.forEach {
            add(AssetWidget(it.fullName, Icons.File.Image))
        }
    }


    /**
     * This should render out the left panel with the folders
     */
    private fun renderFolders(widgets: Collection<AssetWidget>) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 152f / 255, 152f / 255, 179f / 255, 1f)
        ImGui.pushStyleColor(ImGuiCol.Separator, 52f / 255, 52f / 255, 79f / 255, 1f)
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0f, 0f)
        ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 0f)
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 0f)
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 5f, 2f)
        if (ImGui.beginChild("folders##${this.id}", 300f, -1f)) {
            upDirButton.renderInLine()
            ImGui.separator()
            widgets.forEach {
                if (it.isFolder)
                    it.renderInLine()
            }
        }
        ImGui.endChild()
        ImGui.popStyleColor(2)
        ImGui.popStyleVar(4)
    }

    /**
     * This renders out our icons
     */
    private fun renderIcons(widgets: Collection<AssetWidget>) {

        val width = ImGui.getContentRegionAvailX()
        val max = (width / 210f).toInt() //250 = the size we allocate per icon
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 152f / 255, 152f / 255, 179f / 255, 1f)
        if (ImGui.beginChild("files_viewer##${this.id}")) {
            if (ImGui.button("do testing")) {
                val source = AssetTypeManager().source


            }


            if (ImGui.beginTable("asset_table", max)) {
                widgets.forEach {
                    ImGui.tableNextColumn()
                    it.render()
                }
                ImGui.endTable()
            }
        }
        ImGui.endChild()
        ImGui.popStyleColor()
    }


    /**
     * This should render the content of the tool window.
     */
    override fun renderContent() {
        val widgets = find(AssetWidget::class)
        renderFolders(widgets)
        ImGui.sameLine()
        renderIcons(widgets)
    }
}