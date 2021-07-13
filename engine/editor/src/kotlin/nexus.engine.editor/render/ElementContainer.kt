package nexus.engine.editor.render

import kotlin.reflect.KClass

/**
 * This is an element
 */
abstract class ElementContainer(override val name: String, block: descriptorBlock) : NamedElement {
    protected val namedChildren: MutableMap<String, Element> = HashMap()
    protected val children: MutableList<Element> = ArrayList()
    private val descriptor = ElementDescriptor(this)


    init {
        descriptor.apply(block)
    }

    fun add(element: Element) {
        children.add(element)
    }

    /**
     * This will add this given child element
     */
    fun add(element: NamedElement, forceReplace: Boolean = false): Boolean {
        if (namedChildren.containsKey(element.name) && !forceReplace) return false
        namedChildren[element.name] = element
        return true
    }

    /**
     * This attempts to find the given element of the given [name]. if [recursive] is
     * true, we will search through all of the children for the given name
     */
    fun has(name: String, recursive: Boolean = false): Boolean {
        if (namedChildren.containsKey(name)) return true
        if (!recursive) return false
        for (child in namedChildren.values) {
            if (child is ElementContainer) {
                if (child.has(name, recursive)) return true
            }
        }
        return false
    }

    /**
     * This attempts to get the given child element of the given [name]. does not search through childrne.
     *  this is O(N) where N = number of namedChildren
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Element> find(name: String, type: KClass<T>): T? {
        if (!has(name, false)) return null
        val value = namedChildren[name]
        if (type.isInstance(value))
            return value as T
        return null
    }

    /**
     * This attempts to get the given child element of the given [name]. This will search through children.
     *  this is O(N^2) where N = number of namedChildren and it's square because it must search through N'th
     *  level children.
     */
    fun <T : Element> findRecursive(name: String, type: KClass<T>): T? {
        if (has(name, false)) return find(name, type)
        for (child in namedChildren.values)
            if (child is ElementContainer)
                if (child.has(name, true))
                    return child.findRecursive(name, type) ?: continue
        return null
    }

    /**
     * This is the internal method that will do the actual rendering
     */
    override fun process() {
        children.forEach { it.process() }
        for (child in namedChildren.values) {
            child.process()
        }

    }
}