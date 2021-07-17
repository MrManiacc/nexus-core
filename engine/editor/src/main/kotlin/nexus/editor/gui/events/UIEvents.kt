package nexus.editor.gui.events

import nexus.editor.gui.Element
import nexus.engine.events.Event

object UIEvents {

    /**
     * This is called when the focus of a given node is switched
     */
    data class NodeFocusSwitched(val focused: Element) : Event()
}