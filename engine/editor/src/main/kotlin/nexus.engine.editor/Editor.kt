package nexus.engine.editor

import com.google.common.collect.Lists
import dorkbox.messageBus.MessageBus
import dorkbox.messageBus.annotations.Subscribe
import mu.KotlinLogging
import nexus.engine.Application
import nexus.engine.camera.CameraController
import nexus.engine.editor.camera.OrthoCamera
import nexus.engine.editor.camera.OrthoController
import nexus.engine.editor.layer.LayerEditor
import nexus.engine.editor.layer.LayerViewport
import nexus.engine.editor.wrapper.DebugRenderAPI
import nexus.engine.events.Events
import nexus.engine.glfw.IWindow
import nexus.engine.input.IInput
import nexus.engine.layer.Layer
import nexus.engine.render.Renderer
import nexus.engine.render.framebuffer.Framebuffer
import nexus.engine.render.framebuffer.FramebufferFormat
import nexus.engine.render.framebuffer.FramebufferSpecification
import nexus.engine.scene.RenderScene
import nexus.plugins.glfw.GlfwInput
import nexus.plugins.glfw.GlfwWindow
import nexus.plugins.opengl.GLFramebuffer
import nexus.plugins.opengl.GLRenderAPI
import nexus.plugins.opengl.GLScene
import org.slf4j.Logger


/**
 * This is the front on the engine. Its the sandbox/the application used for testing the engine/libraries.
 */
object Editor : Application<DebugRenderAPI>() {
    override val log: Logger = KotlinLogging.logger { }
    override val eventbus: MessageBus = MessageBus(4)
    override val window: IWindow = GlfwWindow(title = "Sandbox, nexus.plugins.glfw", app = this)
    override val input: IInput = GlfwInput(window)
    override var isRunning: Boolean = false
    override var gameTime: Double = 0.0
    override var startTime: Long = System.currentTimeMillis()
    override val layers: MutableList<Layer<*>> = Lists.newArrayList()
    override var insertIndex: Int = 0

    override val viewport: Framebuffer = GLFramebuffer(
        FramebufferSpecification(
            1280, 720, format = FramebufferFormat(
                FramebufferFormat.Attachment.DepthBuffer, FramebufferFormat.Attachment.ColorImage
            )
        )
    )

    /*==================Scene==================**/
    override var controller: CameraController<DebugRenderAPI> =
        OrthoController(OrthoCamera(-1.6f, 1.6f, -0.9f, 0.9f, 1.0f))
    private val debugScene: RenderScene = GLScene(DebugRenderAPI::class)
    override val scene: RenderScene = GLScene(GLRenderAPI::class)

    /*==================Render==================**/
    override val renderAPI: DebugRenderAPI = DebugRenderAPI(window, debugScene).apply(Renderer::register)
    private val editorLayer: Layer<DebugRenderAPI> = LayerEditor(this)
    private val viewportLayer: Layer<DebugRenderAPI> = LayerViewport(this, DebugRenderAPI::class)

    /*We must subscribe anything important here. In the future any entity systems number would be subscribed here*/
    init {
        subscribe(controller)
        subscribe(viewport)
        subscribe(renderAPI)
        subscribe(window)
        subscribe(this)
    }

    /*This is used to initialized our layers*/
    @Subscribe
    fun onGLInitialized(event: Events.Window.Initialized) {
        pushLayer(editorLayer)
        pushLayer(viewportLayer)
    }


}

