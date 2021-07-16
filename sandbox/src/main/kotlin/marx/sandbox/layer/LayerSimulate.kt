package marx.sandbox.layer

import mu.KotlinLogging
import nexus.engine.Application
import nexus.engine.events.Event
import nexus.engine.events.Events.App.Timestep
import nexus.engine.events.Events.Input.KeyPress
import nexus.engine.events.Events.Shader.Compiled
import nexus.engine.layer.Layer
import nexus.engine.render.Buffer.VertexBuffer.DataType.Float3
import nexus.engine.render.VertexArray
import nexus.plugins.opengl.GLBuffer
import nexus.plugins.opengl.GLRenderAPI
import nexus.plugins.opengl.GLShader
import nexus.plugins.opengl.GLVertexArray
import nexus.plugins.opengl.data.Shaders
import org.lwjgl.glfw.GLFW.GLFW_KEY_R
import org.slf4j.Logger

/*
 * This nexus.engine.layer is used for debugging purposes
 */
class LayerSimulate(app: Application<*>) :
    Layer<GLRenderAPI>(app, GLRenderAPI::class, "simulation-nexus.engine.layer") {
    private val log: Logger = KotlinLogging.logger { }
    private val shader = GLShader(app)

    /*This creates a quad of size 0.5**/
    val quadVAO: VertexArray = GLVertexArray().apply {
        this += GLBuffer.GLVertexBuffer(
            floatArrayOf(
                0.25f, 0.5f, 0.0f,  // top right
                0.25f, -0.5f, 0.0f,  // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                -0.5f, 0.5f, 0.0f // top left
            ), Float3, 0
        )

        this += GLBuffer.GLIndexBuffer(
            intArrayOf(
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
            )
        )
    }

    /*This creates a quad of size 0.5**/
    val triangleVAO: VertexArray = GLVertexArray().apply {
        this += GLBuffer.GLVertexBuffer(
            floatArrayOf(
                -0.33f, -0.5f, 0.0f,  // bottom left
                0.0f, 0.33f, 0.0f, // top left
                0.33f, -0.5f, 0.0f,  // bottom right
            ), Float3, 0
        )

        this += GLBuffer.GLIndexBuffer(
            intArrayOf(
                0, 1, 2,   // first triangle
            )
        )
    }

    /*
   This is called upon the nexus.engine.layer being presented.
     */
    override fun onAttach() {
        renderAPI.init()
        quadVAO.create()
        triangleVAO.create()
        if (shader.compile(Shaders.flatShader())) log.warn("Successfully compiled shader: ${shader::class.qualifiedName}")
    }

    /*
   This will draw every frame
     */
    override fun onUpdate(update: Timestep) {
        shader.bind()
        scene.submit(triangleVAO)
        shader.unbind()
    }

    override fun onEvent(event: Event) {
        if (event is Compiled)
            if (event.result.isValid)
                log.info("Successfully compiled '${event.result.type.name}' shader: ${event.result.message}")
            else
                log.error("Failed to compile '${event.result.type.name}' shader: ${event.result.message}")
        else if (event is KeyPress) {
            if (event.key == GLFW_KEY_R) { //Reload the shader
                shader.destroy()
                shader.compile(Shaders.flatShader())
                log.info("Reloaded shader: $shader")
            }
        }
    }

    override fun onDetach() {
        shader.destroy()
        triangleVAO.dispose()
        quadVAO.dispose()
    }


}