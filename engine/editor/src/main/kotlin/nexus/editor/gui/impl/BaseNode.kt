package nexus.editor.gui.impl

import nexus.editor.gui.Node
import nexus.editor.gui.internal.ID
import nexus.editor.gui.internal.WindowFlag

/**
 * This is the implementation of the actual base node.
 */
abstract class BaseNode(id: String, flags: Array<out WindowFlag> = emptyArray()) : Node {
    /**
     * This should be created from the enum values for the flags
     */
    override val flags: Int = WindowFlag.combine(flags)

    /**
     * This is used as a name and id
     */
    override val nameId: ID = ID(id)

    override fun toString(): String {
        return "${this.javaClass.simpleName}(name=$nameId)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseNode

        if (nameId != other.nameId) return false

        return true
    }

    override fun hashCode(): Int {
        return nameId.hashCode()
    }


}