package nexus.editor.api.graph

import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import nexus.editor.api.impl.ContainerElement

/**
 * A node is an element that is rendered within the context of
 */
abstract class Node(name: String, vararg attribute: Attribute) : ContainerElement(name, emptyArray()) {

    init {
        addAll(*attribute)
    }

    /**
     * This is used for keeping track of our own attirubtes
     */
    val attributes: Collection<Attribute> get() = find(Attribute::class)

    /**
     * This is a unique id that is computed from the hash of the [uniqueId]
     */
    val uniqueId: Int get() = ImGui.getID(nameId.uniqueId)

    /**
     * This method actually is used to render the nodes
     */
    override fun render() {
        ImNodes.beginNode(uniqueId)
        ImNodes.beginNodeTitleBar()
        renderTitle()
        ImNodes.endNodeTitleBar()
        ImNodes.endNode()
    }

    /**
     * This is used to render out the title bar
     */
    abstract fun renderTitle()


}