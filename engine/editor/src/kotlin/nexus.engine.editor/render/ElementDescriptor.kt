package nexus.engine.editor.render

import mu.KotlinLogging
import org.slf4j.Logger

class ElementDescriptor(private val container: ElementContainer) {
    private val log: Logger = KotlinLogging.logger { }

    /**
     * This creates a new label
     */
    fun label(text: String) {
        container.add(Text(text))
        log.debug("Added new label: $text")
    }

    /**
     * This creates a new label
     */
    fun label(key: String, value: String) {
        container.add(Label(key, value))
        log.debug("Added new label: $key = $value")
    }


    fun render(element: Element) {
        container.add(element)
    }

}