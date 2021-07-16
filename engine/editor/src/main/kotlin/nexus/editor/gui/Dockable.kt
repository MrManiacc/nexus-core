package nexus.editor.gui

import imgui.internal.ImGui
import imgui.type.ImInt
import nexus.editor.gui.internal.Anchor
import nexus.editor.gui.internal.DockFlag

interface Dockable : Node {

    val anchor: Anchor
        get() = Anchor.None

    val dockFlags: Int
        get() = DockFlag.None.value

    val sizeRatio: Float


    /**
     * This is used to allow for docking of a node.
     */
    fun dock(dockspaceID: ImInt): ImInt {
        if (anchor == Anchor.Center)
            ImGui.dockBuilderDockWindow(this.displayName, dockspaceID.get())
        if (anchor != Anchor.None && anchor != Anchor.Center) {
            val dock: Int = ImGui.dockBuilderSplitNode(dockspaceID.get(),
                this.anchor.value,
                this.sizeRatio,
                null,
                dockspaceID)
            ImGui.dockBuilderDockWindow(this.displayName, dock)
            return ImInt(dock)
        }
        return dockspaceID
    }
}