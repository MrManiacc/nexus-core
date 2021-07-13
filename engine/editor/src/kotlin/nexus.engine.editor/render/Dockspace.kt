package nexus.engine.editor.render

class Dockspace(name: String, descriptor: descriptorBlock) : ElementContainer(name, descriptor) {
    /**
     * This is the internal method that will do the actual rendering
     */
    override fun process() {
        preDockspace()
        super.process()
        postDockspace()
    }


    private fun preDockspace() {

    }


    private fun postDockspace() {

    }
}