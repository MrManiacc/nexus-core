package nexus.editor.api.graph

import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import nexus.editor.api.Element
import nexus.editor.api.internal.ID

/**
 * A node is an element that is rendered within the context of
 */
abstract class Attribute(name: String) : Element {

    /**
     * This should be created from the enum values for the flags
     */
    override val flags: Int = 0

    /**
     * This is used as a name and id
     */
    override val nameId: ID = ID(name)


    /**
     * This is a unique id that is computed from the hash of the [uniqueId]
     */
    val uniqueId: Int get() = ImGui.getID(nameId.uniqueId)

    /**
     * This is used for determine the type of rendering todo
     */
    abstract val type: Type


    /**
     * This method actually is used to render the nodes
     */

    override fun render() {
        when (type) {
            Type.Input -> {
                ImNodes.beginInputAttribute(uniqueId)
                renderAttribute()
                ImNodes.endInputAttribute()
            }
            Type.Output -> {
                ImNodes.beginOutputAttribute(uniqueId)
                renderAttribute()
                ImNodes.endOutputAttribute()
            }
            Type.Static -> {
                ImNodes.beginStaticAttribute(uniqueId)
                renderAttribute()
                ImNodes.endStaticAttribute()
            }
        }
    }

    /**
     * This is used to render out the attribute body
     */
    abstract fun renderAttribute()

    /**
     * THis is used to keep track fo the type of attribute we are
     */
    enum class Type {
        Input, Output, Static
    }


}