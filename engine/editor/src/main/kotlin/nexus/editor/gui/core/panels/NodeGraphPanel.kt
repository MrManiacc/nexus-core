package nexus.editor.gui.core.panels

import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.ImNodesContext
import nexus.editor.gui.Element
import nexus.editor.gui.graph.Graph
import nexus.editor.gui.impl.AbstractToolPanel
import nexus.editor.gui.internal.Anchor
import nexus.engine.Application

/**
 * This panel allows for rendering of nodes inside it's own contained contenxt
 */
class NodeGraphPanel(val app: Application<*>, name: String = "nexus.editor.nodegraph", anchor: Anchor = Anchor.Center) :
    AbstractToolPanel(
        name, anchor), Graph {

    /**
     * This is our current graph context. This is used for rendering.
     */
    private lateinit var context: ImNodesContext

    /**
     * This is our current graph context. This is used for rendering.
     */
    /**
     * This should render the content of the tool window.
     */
    override fun renderContent() {
        ImNodes.editorContextSet(context)
        ImNodes.beginNodeEditor()
        renderChildren()
        ImNodes.endNodeEditor()
    }


    /**
     * This is used to setup our contenxt
     */
    override fun addedTo(parent: Element) {
        context = ImNodes.editorContextCreate()
    }

    /**
     * This frees our context
     */
    override fun removedFrom(parent: Element) =
        ImNodes.editorContextFree(context)


}