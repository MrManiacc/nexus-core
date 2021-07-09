package nexus.plugins.opengl

import marx.engine.nexus.plugins.glfw.IWindow
import marx.engine.render.*
import marx.engine.render.RenderCommand.*
import org.lwjgl.opengl.GL11.*

/*
 * This is the openGL implementation of raw draw [RenderCommand]
 */
class GLRenderCommand(val window: IWindow) : RenderCommand {
    /*
Allows for viewport resizing
     */
    override fun viewport(size: Pair<Int, Int>, pos: Pair<Int, Int>) {
        glViewport(pos.first, pos.second, size.first, size.second)
    }

    /*
Clear the screen with the given color
     */
    override fun clear(color: FloatArray?, clearFlags: ClearFlags) {
        when (clearFlags) {
            ClearFlags.COLOR -> {
                glClear(GL_COLOR_BUFFER_BIT)
            }
            ClearFlags.DEPTH -> {
                glClear(GL_DEPTH_BUFFER_BIT)
            }
            ClearFlags.COLOR_DEPTH -> {
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            }
        }
        if (color != null)
            if (color.size == 4) glClearColor(color[0], color[1], color[2], color[3])
            else if (color.size == 3) glClearColor(color[0], color[1], color[2], 1.0f)
    }

    /*
Swap the given buffers of the graphics context
     */
    override fun swap() {
        window.swapBuffers()
    }

    /*
Poll the input for the graphics context
     */
    override fun poll() {
        window.pollInput()
    }
}