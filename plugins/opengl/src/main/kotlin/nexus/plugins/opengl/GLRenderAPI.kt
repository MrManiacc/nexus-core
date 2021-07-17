package nexus.plugins.opengl

import nexus.engine.window.IWindow
import nexus.engine.render.Buffer.IndexBuffer
import nexus.engine.render.RenderAPI
import nexus.engine.render.RenderScene
import nexus.engine.render.VertexArray
import nexus.engine.render.framebuffer.Framebuffer
import nexus.engine.render.framebuffer.FramebufferSpecification
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil

/*
 * This is the backend nexus.engine.render context for opengl rendering related functionality
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

    /**
     * Draws the given vertex array instanced, meaning we can nexus.engine.render many of these statically.
     */
    override fun drawIndexed(array: VertexArray) {
        glDrawElements(GL_TRIANGLES, array[IndexBuffer::class].indices.size, GL_UNSIGNED_INT, MemoryUtil.NULL)
    }

    /**
     * This should create/return a frame buffer for the givne specification.
     */
    override fun framebuffer(specification: FramebufferSpecification): Framebuffer {
        return GLFramebuffer(specification)
    }

}