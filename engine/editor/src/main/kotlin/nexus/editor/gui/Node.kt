package nexus.editor.gui

import nexus.editor.gui.impl.NodeEmpty
import nexus.editor.gui.internal.ID

/**
 * This is the root of all ui elements, it represents the relationship between nodes allowing for children
 */
interface Node {
    /**
     * This is used as a name and id
     */
    val nameId: ID


    val name: String get() = nameId.name

    val id: String get() = nameId.id

    val displayName: String get() = nameId.windowID


    /**
     * This method actually is used to render the nodes
     */
    fun render()


    /**
     * This used to save use a render call. we simply check to see if this class is an instance of the [NodeEmpty] object
     */
    val isEmpty: Boolean get() = this is NodeEmpty


    /**
     * This should be created from the enum values for the flags
     */
    val flags: Int

    /**
     * This is called upon being added to the given [parent]
     */
    fun addedTo(parent: Node) {}

    /**
     * This is called upon a ui element being removed from the parent
     */
    fun removedFrom(parent: Node) {}

}