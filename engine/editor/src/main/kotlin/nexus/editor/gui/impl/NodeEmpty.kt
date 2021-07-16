package nexus.editor.gui.impl

import nexus.editor.gui.Node
import nexus.editor.gui.internal.ID
import java.util.*

/**
 * This is used for any time you wish to have a deafult node while still being null safe
 */
object NodeEmpty : Node {
    /**
     * This is used as a name and id
     */
    override val nameId: ID = ID("EMPTY##${UUID.randomUUID()}")

    /**
     * This method actually is used to render the nodes
     */
    override fun render() = Unit

    /**
     * This should be created from the enum values for the flags
     */
    override val flags: Int = 0
}