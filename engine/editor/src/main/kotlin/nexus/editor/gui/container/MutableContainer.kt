package nexus.editor.gui.container

import nexus.editor.gui.Node
import nexus.editor.gui.internal.ID
import kotlin.reflect.KClass

/**
 * This wraps around a container to allow for mutability meaning we can add and remove nodes
 */
interface MutableContainer : Container {
    /**
     * This will add this given child element, if [forceReplace] is true, we will make sure that
     * this element is appended
     */
    fun add(element: Node, forceReplace: Boolean = false): Boolean

    /**
     * This is called upon any element being added
     */
    fun onAdd(element: Node) = element.addedTo(this)

    /**
     * This is called upon any element being removed
     */
    fun onRemove(element: Node) = element.removedFrom(this)

    /**
     * This will add all of the given elements
     */
    fun addAll(vararg elements: Node, forceReplace: Boolean): Boolean =
        addAll(elements.toList(), forceReplace)


    /**
     * This will add all of the given elements. This will return true if all of the elements were added
     * successfully. This is done via the use of the [add] method
     */
    fun addAll(elements: Collection<Node>, forceReplace: Boolean): Boolean {
        var passed = true
        for (element in elements) {
            if (!add(element, forceReplace)) {
                passed = false
            }
        }
        return passed
    }


    /**
     * This removes the node of the given element id
     */
    fun remove(element: ID): Node?


    /**
     * This will remove all of the nodes of the given [type]
     */
    fun <T : Node> removeAll(type: KClass<T>): Collection<T>

}