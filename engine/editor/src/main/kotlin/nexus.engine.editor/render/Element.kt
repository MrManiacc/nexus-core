package nexus.engine.editor.render

fun interface Element {

    /**
     * This is the internal method that will do the actual rendering
     */
    fun process()
}