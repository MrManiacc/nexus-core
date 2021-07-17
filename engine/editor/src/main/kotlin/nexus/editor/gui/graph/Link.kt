package nexus.editor.gui.graph

import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import nexus.editor.gui.Element
import nexus.editor.gui.internal.ID
import java.util.*

/**
 * A node is an element that is rendered within the context of
 */
data class Link(val start: Attribute, val stop: Attribute) : Element {

    /**
     * This should be created from the enum values for the flags
     */
    override val flags: Int = 0


    /**
     * This is used as a name and id
     */
    override val nameId: ID = ID(UUID.randomUUID().toString())

    /**
     * This is a unique id that is computed from the hash of the [uniqueId]
     */
    val uniqueId: Int get() = ImGui.getID(nameId.uniqueId)

    /**
     * This method actually is used to render the nodes
     */
    override fun render() {
        ImNodes.link(uniqueId, start.uniqueId, stop.uniqueId)
    }


}