package nexus.plugins.opengl

import nexus.engine.glfw.IWindow
import nexus.engine.render.RenderCommand
import nexus.engine.render.RenderCommand.ClearFlags
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

    override fun blending(enabled: Boolean) {
        if (enabled) {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        } else {
            glDisable(GL_BLEND)
        }
    }

    /*
Swap the given buffers of the graphics context
     */
    override fun swap() {
        window.swapBuffers()
    }

    /*
Poll the nexus.engine.input for the graphics context
     */
    override fun poll() {
        window.pollInput()
    }
}