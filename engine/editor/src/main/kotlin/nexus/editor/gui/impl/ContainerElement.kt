package nexus.editor.gui.impl


import mu.KotlinLogging
import nexus.editor.gui.Element
import nexus.editor.gui.container.Container
import nexus.editor.gui.container.MutableContainer
import nexus.editor.gui.internal.ID
import nexus.editor.gui.internal.WindowFlag
import org.slf4j.Logger
import kotlin.reflect.KClass

/**
 * This implements a base type of node that can
 */
abstract class ContainerElement(id: String, flags: Array<out WindowFlag>) : BaseElement(id, flags), MutableContainer,
    Iterable<Element> {
    protected val children: MutableMap<ID, Element> = HashMap()
    private val logger: Logger = KotlinLogging.logger { }

    /**
     * This should reutn the total number of ui nodesl
     */
    override val count: Int
        get() = children.size

    /**
     * This will add this given child element, if [forceReplace] is true, we will make sure that
     * this element is appended. This will return true if the element has been added
     */
    override fun add(element: Element, forceReplace: Boolean): ContainerElement {
        if (children.containsKey(element.nameId) && !forceReplace) {
            logger.warn("Failed to add element: ${element.nameId} due to duplication of id's for child of type: ${children[element.nameId]!!::class.simpleName}. (your node type is: ${element.javaClass.simpleName})")
            return this
        }
        children[element.nameId] = element
        onAdd(element)
        logger.debug("Successfully added element named '${element.nameId}' with node type '${element.javaClass.simpleName}'")
        return this
    }


    /**
     * This removes the node of the given element id
     */
    override fun remove(id: ID): Element? {
        val element = children.remove(id)
        if (element != null) {
            onRemove(element)
        }
        return element
    }

    /**
     * This will remove all of the nodes of the given [type]
     */
    override fun <T : Element> removeAll(type: KClass<T>): Collection<T> {
        val iterator = this.iterator()
        val removed = ArrayList<T>()
        while (iterator.hasNext()) {
            val node: Element = iterator.next()
            if (type.isInstance(node)) {
                iterator.remove()
                onRemove(node)
                removed.add(node as T)
            }
        }
        return removed
    }

    /**
     * This attempts to get the given child element of the given [name]. does not search through childrne.
     *  this is O(N) where N = number of namedChildren
     */
    override fun <T : Element> find(name: String, type: KClass<T>): T? {
        return filter { type.isInstance(it) && it.nameId.id == name }.firstOrNull() as T?
    }

    /**
     * This should return all of the nodes that are instances of the given [type] and return them as a
     * collection.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Element> find(type: KClass<T>): List<T> {
        return filter { type.isInstance(it) }.map { it as T }  //This is fine to cast to as it's internally an arraylist
    }

    /**
     * This attempts to find all nodes of the given [type] recursively, it does this finding all
     * children of type [Container] and iterating them, only after checking it's own nodes first.
     */
    override fun <T : Element> findRecursive(type: KClass<T>): List<T> {
        val result = ArrayList<T>()
        for (child in children.values) {
            if (type.isInstance(child)) {
                result.add(child as T)
                logger.debug("Found node of type '${type.simpleName}' named '${child.nameId}")
            }
            if (child is Container)
                result.addAll(child.findRecursive(type))
        }
        return result
    }


    /**
     * This attempts to get the given child element of the given [name]. This will search through children.
     *  this is O(N^2) where N = number of namedChildren and it's square because it must search through N'th
     *  level children.
     */
    override fun <T : Element> findRecursive(name: String, type: KClass<T>): T? {
        for (child in children.values) {
            if (type.isInstance(child)) {
                if (child.nameId.id == name) {
                    logger.debug("Found node of type '${type.simpleName}' named '${child.nameId}")
                    return child as T
                }
            }
            if (child is Container) {
                return child.findRecursive(name, type) ?: continue
            }

        }
        return null
    }

    /**
     * This is used to check if we have a code of the given type. This is non recursive
     */
    override fun <T : Element> contains(type: KClass<T>): Boolean {
        return any { type.isInstance(it) }
    }

    /**
     * This is used to check if we have a node of the given name
     */
    override fun containsName(name: String): Boolean {
        return any { it.nameId.id == name }
    }

    /**
     * This is used to determine if we have a node assignable of the given [type]
     * and with the name id of the given [name]
     */
    override fun <T : Element> contains(type: KClass<T>, name: String): Boolean {
        return this.any { type.isInstance(it) && it.nameId.id == name }
    }

    /**
     * This is used to check if we have a node of the given name. It will also check all children to see if they
     * carry any nodes with the given [type]
     */
    override fun <T : Element> containsRecursive(type: KClass<T>): Boolean {
        if (contains(type)) return true
        for (container in find(Container::class))
            if (container.containsRecursive(type)) return true
        return false
    }

    /**
     * This is used to check if we have a node of the given name. It will also check all children to see if they
     * carry any nodes with the given [name]
     */
    override fun containsNameRecursive(name: String): Boolean {
        if (containsName(name)) return true
        for (container in find(Container::class))
            if (container.containsNameRecursive(name)) return true
        return false
    }

    /**
     * This is used to determine if we have a node assignable of the given [type]
     * and with the name id of the given [name].
     */
    override fun <T : Element> containsRecursive(type: KClass<T>, name: String): Boolean {
        if (containsRecursive(type, name)) return true
        for (container in find(Container::class))
            if (container.containsRecursive(type, name)) return true
        return false
    }

    /**
     * This will simply call our render children
     */
    override fun render() = renderChildren()

    /**
     * This is the internal method that will do the actual rendering
     */
    override fun renderChildren() {
        for (child in children.values)
            if (child.batchRender)
                child.render()
    }

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator(): MutableIterator<Element> = children.values.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContainerElement

        if (children != other.children) return false
        if (nameId != other.nameId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = children.hashCode()
        result = 31 * result + nameId.hashCode()
        return result
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}(children=$children, name=$nameId)"
    }


}