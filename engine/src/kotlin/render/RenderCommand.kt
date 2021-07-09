package marx.engine.render

interface RenderCommand {

    /*
Allows for viewport resizing
     */
    fun viewport(size: Pair<Int, Int>, pos: Pair<Int, Int>) = Unit

    /*
Clear the screen with the given color
     */
    fun clear(color: FloatArray? = floatArrayOf(0.1f, 0.1f, 0.1f, 1f), clearFlags: ClearFlags = ClearFlags.COLOR) = Unit

    /*
Swap the given buffers of the graphics context
     */
    fun swap() = Unit

    /*
Poll the input for the graphics context
     */
    fun poll() = Unit

    /*
   This clear flags for clearing the screen
     */
    enum class ClearFlags {
        COLOR, DEPTH, COLOR_DEPTH
    }
}