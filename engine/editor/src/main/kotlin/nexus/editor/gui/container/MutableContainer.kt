package nexus.editor.gui.container

import nexus.editor.gui.Element
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
    fun add(element: Element, forceReplace: Boolean = false): MutableContainer

    /**
     * This is called upon any element being added
     */
    fun onAdd(element: Element) = element.addedTo(this)

    /**
     * This is called upon any element being removed
     */
    fun onRemove(element: Element) = element.removedFrom(this)

    /**
     * This will add all of the given elements
     */
    fun addAll(vararg elements: Element, forceReplace: Boolean = true): MutableContainer =
        addAll(elements.toList(), forceReplace)


    /**
     * This will add all of the given elements. This will return true if all of the elements were added
     * successfully. This is done via the use of the [add] method
     */
    fun addAll(elements: Collection<Element>, forceReplace: Boolean): MutableContainer {
        var passed = true
        for (element in elements) {
            add(element, forceReplace)
        }
        return this
    }


    /**
     * This removes the node of the given element id
     */
    fun remove(element: ID): Element?


    /**
     * This will remove all of the nodes of the given [type]
     */
    fun <T : Element> removeAll(type: KClass<T>): Collection<T>

}