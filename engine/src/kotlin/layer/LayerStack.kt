package marx.engine.layer

import org.slf4j.*

interface LayerStack : Collection<Layer<*>> {
    val layers: MutableList<Layer<*>>
    var insertIndex: Int
    val log: Logger

    //Pushes layers to the front/top of the list
    fun pushLayer(layer: Layer<*>) {
        if (layers.contains(layer)) {
            log.warn("Attempted to add layer: $layer twice, make sure to pop it first!")
            return
        }
        if (insertIndex + 1 > size)
            insertIndex = size
        layers.add(insertIndex++, layer).also { layer.onAttach() }
            .also { log.info("pushed layer ${layer.name} to index ${layers.indexOf(layer)}") }
    }

    //Pushes layers to the back, as an overlay
    fun pushOverlay(layer: Layer<*>) {
        if (layers.contains(layer)) {
            log.warn("Attempted to add overlay: $layer twice, make sure to pop it first!")
            return
        }
        val index = if (layers.isEmpty()) 0 else layers.size - 1

        layers.add(index, layer).also { layer.onAttach() }
            .also { log.info("pushed overlay layer ${layer.name} to index ${layers.indexOf(layer)}") }
    }

    /*
   This will find the layer's index and remove it,
then decrement the [insertIndex]
     */
    fun popLayer(layer: Layer<*>) {
        val index = layers.indexOf(layer)
        if (index != layers.size && index >= 0) {
            layers.removeAt(index).onDetach()
            log.info("popped layer ${layer.name} from index $index")
            insertIndex--
        }
    }

    /*
Removes the last layer
     */
    fun popOverlay(layer: Layer<*>) {
        val index = layers.indexOf(layer)
        if (index != layers.size && index >= 0) {
            layers.removeAt(layers.indexOf(layer)).also { layer.onDetach() }
                .also {
                    log.info("popped overlay layer ${layer.name} from index ${layers.size}")
                }
        }
    }

    /*
Returns the size of the collection.
     */
    override val size: Int
        get() = layers.size

    /*
Checks if the specified element is contained in this collection.
     */
    override fun contains(element: Layer<*>): Boolean = layers.contains(element)

    /*
Checks if all elements in the specified collection are contained in this collection.
     */
    override fun containsAll(elements: Collection<Layer<*>>): Boolean = layers.containsAll(elements)

    /*
Returns `true` if the collection is empty (contains no elements), `false` otherwise.
     */
    override fun isEmpty(): Boolean = layers.isEmpty()

    override fun iterator(): Iterator<Layer<*>> = layers.iterator()
}