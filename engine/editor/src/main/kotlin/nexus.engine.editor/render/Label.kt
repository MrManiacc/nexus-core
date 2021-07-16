package nexus.engine.editor.render

import imgui.ImGui

class Label(override val name: String, var labelText: String) : NamedElement {
    /**
     * This is the internal method that will do the actual rendering
     */
    override fun process() {
        ImGui.labelText(name, labelText)
    }
}