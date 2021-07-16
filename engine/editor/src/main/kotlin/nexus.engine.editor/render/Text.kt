package nexus.engine.editor.render

import imgui.ImGui

class Text(override val name: String) : NamedElement {
    /**
     * This is the internal method that will do the actual rendering
     */
    override fun process() {
        ImGui.text(name)
    }
}