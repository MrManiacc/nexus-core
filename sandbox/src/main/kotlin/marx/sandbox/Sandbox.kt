package marx.sandbox

import com.google.common.collect.Lists
import dorkbox.messageBus.MessageBus
import dorkbox.messageBus.annotations.Subscribe
import marx.sandbox.layer.LayerDebug
import marx.sandbox.layer.LayerSimulate
import mu.KotlinLogging
import nexus.editor.camera.OrthoCamera
import nexus.editor.camera.OrthoController
import nexus.editor.layer.LayerEditor
import nexus.editor.wrapper.DebugRenderAPI
import nexus.engine.Application
import nexus.engine.camera.CameraController
import nexus.engine.events.Events.Input.KeyPress
import nexus.engine.events.Events.Window.Initialized
import nexus.engine.input.IInput
import nexus.engine.layer.Layer
import nexus.engine.render.RenderScene
import nexus.engine.render.Renderer
import nexus.engine.render.framebuffer.Framebuffer
import nexus.engine.render.framebuffer.FramebufferFormat
import nexus.engine.render.framebuffer.FramebufferSpecification
import nexus.engine.scene.Scene
import nexus.engine.scene.internal.ArtemisScene
import nexus.engine.window.IWindow
import nexus.plugins.glfw.GlfwInput
import nexus.plugins.glfw.GlfwWindow
import nexus.plugins.opengl.GLFramebuffer
import nexus.plugins.opengl.GLRenderAPI
import nexus.plugins.opengl.GLScene
import org.lwjgl.glfw.GLFW.*
import org.slf4j.Logger
import kotlin.io.path.ExperimentalPathApi

/**
 * This is the front on the engine. Its the sandbox/the application used for testing the engine/libraries.
 */
@ExperimentalPathApi
object Sandbox : Application<GLRenderAPI>() {
    override val log: Logger = KotlinLogging.logger { }
    override val eventbus: MessageBus = MessageBus(4)
    override val window: IWindow = GlfwWindow(title = "Sandbox, nexus.plugins.glfw", app = this)
    override val input: IInput = GlfwInput(window)
    override var isRunning: Boolean = false
    override var gameTime: Double = 0.0
    override var startTime: Long = System.currentTimeMillis()
    override val layers: MutableList<Layer<*>> = Lists.newArrayList()
    override var insertIndex: Int = 0

    /*==================Scene==================**/
    val debugScene: RenderScene = GLScene(DebugRenderAPI::class)
    override val renderScene: RenderScene = GLScene(GLRenderAPI::class)
    override val viewport: Framebuffer = GLFramebuffer(
        FramebufferSpecification(
            1280, 720, format = FramebufferFormat(
                FramebufferFormat.Attachment.DepthBuffer, FramebufferFormat.Attachment.ColorImage
            )
        )
    )

    /*==================Render==================**/
    override val renderAPI: GLRenderAPI = GLRenderAPI(window, renderScene).apply(Renderer::register)
    private val debugAPI: DebugRenderAPI = DebugRenderAPI(window, debugScene).apply(Renderer::register)
    private val editorLayer: Layer<DebugRenderAPI> = LayerEditor(this)
    private val debugLayer: Layer<DebugRenderAPI> = LayerDebug(this)
    private val simulateLayer: Layer<GLRenderAPI> = LayerSimulate(this)
    override var controller: CameraController<GLRenderAPI> =
        OrthoController(OrthoCamera(-1.6f, 1.6f, -0.9f, 0.9f, 1.0f))

    /*We must subscribe anything important here. In the future any entity systems number would be subscribed here*/
    init {
        subscribe(editorLayer)
        subscribe(debugAPI)
        subscribe(window)
        subscribe(this)
    }

    /*This is used to initialized our layers*/
    @Subscribe
    fun onGLInitialized(event: Initialized) {
        pushLayer(debugLayer)
    }

    /* This maps the nexus.engine.layer's accordingly*/
    @Subscribe
    fun onKeyPressed(event: KeyPress) {
        when (event.key) {
            GLFW_KEY_KP_0 -> {
                layers.clear()
                pushLayer(editorLayer)
            }
            GLFW_KEY_KP_1 -> {
                layers.clear()
                pushLayer(debugLayer)
            }
            GLFW_KEY_KP_2 -> {
                popLayer(simulateLayer)
                popOverlay(debugLayer)
                popOverlay(editorLayer)
                pushLayer(simulateLayer)
            }
            GLFW_KEY_KP_3 -> {
                popLayer(simulateLayer)
                popOverlay(debugLayer)
                popOverlay(editorLayer)
                pushOverlay(editorLayer)
            }
        }
    }


    /* Called upon the window closing, we pass on the destroy event the various APIS*/
//    @Subscribe
    override fun destroy() {
        super.destroy()
        debugAPI.dispose()
        renderAPI.dispose()
    }

    /**
     * This is used to render the scene
     */
    override var scene: Scene = ArtemisScene()


}

