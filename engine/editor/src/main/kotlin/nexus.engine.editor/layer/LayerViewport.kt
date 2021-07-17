package nexus.engine.editor.layer

import dorkbox.messageBus.annotations.Subscribe
import nexus.engine.Application
import nexus.engine.camera.CameraController
import nexus.engine.events.Events
import nexus.engine.layer.Layer
import nexus.engine.math.MathDSL.Extensions.by
import nexus.engine.math.Transform
import nexus.engine.math.Vec2
import nexus.engine.math.Vec3
import nexus.engine.render.Buffer
import nexus.engine.render.RenderAPI
import nexus.engine.assets.texture.TextureData
import nexus.engine.assets.texture.TextureInstance
import nexus.engine.assets.texture.TextureInstanceData
import nexus.plugins.opengl.GLBuffer
import nexus.plugins.opengl.GLShader
import nexus.plugins.opengl.GLTexture2D
import nexus.plugins.opengl.GLVertexArray
import nexus.plugins.opengl.data.Shaders
import org.lwjgl.opengl.GL11
import kotlin.reflect.KClass

/**
 * This allows for rendering of the viewport
 */
class LayerViewport<API : RenderAPI>(app: Application<API>, target: KClass<API>) :
    Layer<API>(app, target, name = "nexus/editor/viewport") {
    private var texture = GLTexture2D().initialize(TextureData("checkerboard.png"))
    private lateinit var textureInstance: TextureInstance
    private val shader = GLShader(app)
    private val transform: Transform = Transform(0f by 0f by 0f, Vec3(), Vec3(1f))
    private val size = Vec2(-1f)
    private val quad = GLVertexArray(
        GLBuffer.GLVertexBuffer(
            floatArrayOf(
                0.5f, 0.5f, 0.0f, // top right
                0.5f, -0.5f, 0.0f, // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                -0.5f, 0.5f, 0.0f// top left
            ), Buffer.VertexBuffer.DataType.Float3, 0
        ),
        GLBuffer.GLVertexBuffer(
            floatArrayOf(
                0f, 0f,
                1f, 0f,
                1f, 1f,
                0f, 1f
            ), Buffer.VertexBuffer.DataType.Float2, 1
        ),
        GLBuffer.GLIndexBuffer(
            intArrayOf(
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
            )
        )
    )


    /**
     * This is called upon the layer being attached to the application
     */
    override fun onAttach() {
        quad.create()
        shader.compile(Shaders.textureShader()) //TODO: make this return a shader instance or look into materials
        textureInstance = texture.instantiate(
            TextureInstanceData(
                0,
                GL11.GL_TEXTURE_MIN_FILTER to GL11.GL_LINEAR,
                GL11.GL_TEXTURE_MAG_FILTER to GL11.GL_NEAREST
            )
        ) as TextureInstance
        app.viewport.invalidate()
    }

    /**
     * This is used for updating the viewport to match imgui window size
     */
    @Subscribe private fun onResize(event: Events.Camera.Resize) {
        size.set(event.width, event.height)
    }

    /**
     * TODO: this should publish some kind of intellij plugin like event system thing where
     * all classes that wish to render right here can via some interfacing system and extension file system like the
     * intellij plugin.xml system
     */
    override fun onUpdate(update: Events.App.Timestep) {
        app.viewport.bind()
        renderAPI.command.clear(floatArrayOf(0.1f, 0.1f, 0.1f))
        render(app.controller)
        renderAPI.command.viewport(size, Vec2.Zero)
        app.viewport.unbind()

    }

    /**
     * This will render the
     */
    private fun render(controller: CameraController<*>) {
        scene.sceneOf(controller) {
            submit(quad, shader, transform) { shader, transform ->
                textureInstance.bind(0)
                shader.uploadTexture("u_Texture", textureInstance)
                shader.uploadMat4(
                    "u_ModelMatrix", transform.matrix
                )
            }
        }
    }
}