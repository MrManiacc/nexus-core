package nexus.plugins.opengl

import marx.engine.nexus.plugins.glfw.IWindow
import marx.engine.render.*
import marx.engine.render.Buffer.*
import marx.engine.scene.*
import org.lwjgl.glfw.*
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.*

/*
 * This is the backend render context for opengl rendering related functionality
 */
open class GLRenderAPI(val window: IWindow, scene: RenderScene) : RenderAPI(GLRenderCommand(window), scene) {
    /*
Initialize the given graphics context
     */
    override fun init() {
        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window.handle)
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()
    }

    /*
Draws the given vertex array instanced, meaning we can render many of these statically.
     */
    override fun drawIndexed(array: VertexArray) {
        glDrawElements(GL_TRIANGLES, array[IndexBuffer::class].indices.size, GL_UNSIGNED_INT, MemoryUtil.NULL)
    }

}