package nexus.engine.render

import nexus.engine.math.Vec2

interface RenderCommand {

    /*
Allows for viewport resizing
     */
    fun viewport(size: Vec2, pos: Vec2) = Unit

    /*
Clear the screen with the given color
     */
    fun clear(color: FloatArray? = floatArrayOf(0.1f, 0.1f, 0.1f, 1f), clearFlags: ClearFlags = ClearFlags.COLOR) = Unit


    fun blending(enabled: Boolean)


    /*
Swap the given buffers of the graphics context
     */
    fun swap() = Unit

    /*
Poll the nexus.engine.input for the graphics context
     */
    fun poll() = Unit

    /*
   This clear flags for clearing the screen
     */
    enum class ClearFlags {
        COLOR, DEPTH, COLOR_DEPTH
    }
}