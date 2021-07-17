package nexus.editor.gui.container

import nexus.editor.gui.Element
import kotlin.reflect.KClass

interface Container : Element {

    /**
     * This attempts to get the given child element of the given [name]. does not search through childrne.
     *  this is O(N) where N = number of namedChildren
     */
    fun <T : Element> find(name: String, type: KClass<T>): T?

    /**
     * This should return all of the nodes that are instances of the given [type] and return them as a
     * collection.
     */
    fun <T : Element> find(type: KClass<T>): List<T>

    /**
     * This attempts to find all nodes of the given [type] recursively, it does this finding all
     * children of type [Container] and iterating them, only after checking it's own nodes first.
     */
    fun <T : Element> findRecursive(type: KClass<T>): List<T>

    /**
     * This attempts to get the given child element of the given [name]. This will search through children.
     *  this is O(N^2) where N = number of namedChildren and it's square because it must search through N'th
     *  level children.
     */
    fun <T : Element> findRecursive(name: String, type: KClass<T>): T?

    /**
     * This is used to check if we have a code of the given type. This is non recursive
     */
    fun <T : Element> contains(type: KClass<T>): Boolean

    /**
     * This is used to check if we have a node of the given name. It will also check all children to see if they
     * carry any nodes with the given [type]
     */
    fun <T : Element> containsRecursive(type: KClass<T>): Boolean

    /**
     * This is used to check if we have a node of the given name
     */
    fun containsName(name: String): Boolean

    /**
     * This is used to check if we have a node of the given name. It will also check all children to see if they
     * carry any nodes with the given [name]
     */
    fun containsNameRecursive(name: String): Boolean

    /**
     * This is used to determine if we have a node assignable of the given [type]
     * and with the name id of the given [name]
     */
    fun <T : Element> contains(type: KClass<T>, name: String): Boolean

    /**
     * This is used to determine if we have a node assignable of the given [type]
     * and with the name id of the given [name].
     */
    fun <T : Element> containsRecursive(type: KClass<T>, name: String): Boolean

    /**
     * This should reutn the total number of ui nodesl
     */
    val count: Int

    /**
     * This should be true only when count is 0
     */
    override val isEmpty: Boolean get() = count <= 0

    /**
     * This should render all of the child nodes.
     */
    fun renderChildren()
}