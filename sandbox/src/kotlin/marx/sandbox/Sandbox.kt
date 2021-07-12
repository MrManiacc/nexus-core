package marx.sandbox

import com.google.common.collect.Lists
import dorkbox.messageBus.MessageBus
import dorkbox.messageBus.annotations.Subscribe
import marx.sandbox.layer.LayerDebug
import marx.sandbox.layer.LayerSimulate
import mu.KotlinLogging
import nexus.engine.Application
import nexus.engine.camera.Camera
import nexus.engine.editor.camera.OrthographicCamera
import nexus.engine.editor.layer.LayerEditor
import nexus.engine.editor.wrapper.DebugRenderAPI
import nexus.engine.events.Events.Input.KeyPress
import nexus.engine.events.Events.Window
import nexus.engine.events.Events.Window.Initialized
import nexus.engine.glfw.IWindow
import nexus.engine.input.IInput
import nexus.engine.layer.Layer
import nexus.engine.render.Renderer
import nexus.engine.scene.RenderScene
import nexus.plugins.glfw.GlfwInput
import nexus.plugins.glfw.GlfwWindow
import nexus.plugins.opengl.GLRenderAPI
import nexus.plugins.opengl.GLScene
import org.lwjgl.glfw.GLFW.*
import org.slf4j.Logger
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

/**
 * This is the front on the engine. Its the sandbox/the application used for testing the engine/libraries.
 */
@ExperimentalPathApi
object Sandbox : Application<GLRenderAPI>(Path(".")) {
    override val log: Logger = KotlinLogging.logger { }
    override val eventbus: MessageBus = MessageBus(4)
    override val window: IWindow = GlfwWindow(title = "Sandbox, glfw", app = this)
    override val input: IInput = GlfwInput(window)
    override var isRunning: Boolean = false
    override var gameTime: Double = 0.0
    override var startTime: Long = System.currentTimeMillis()
    override val layers: MutableList<Layer<*>> = Lists.newArrayList()
    override var insertIndex: Int = 0

    /*==================Scene==================**/
    val editorCamera: Camera<OrthographicCamera> = OrthographicCamera(-1.6f, 1.6f, -0.9f, 0.9f, 1.0f)
    private val debugScene: RenderScene = GLScene(DebugRenderAPI::class)
    private val gameCamera: Camera<OrthographicCamera> get() = editorCamera //TODO make game nexus.engine.camera
    override val scene: RenderScene = GLScene(GLRenderAPI::class)

    /*==================Render==================**/
    override val renderAPI: GLRenderAPI = GLRenderAPI(window, scene).apply(Renderer::register)
    private val debugAPI: DebugRenderAPI = DebugRenderAPI(window, debugScene).apply(Renderer::register)
    private val editorLayer: Layer<DebugRenderAPI> = LayerEditor(this)
    private val debugLayer: Layer<DebugRenderAPI> = LayerDebug(this)
    private val simulateLayer: Layer<GLRenderAPI> = LayerSimulate(this)

    /*We must subscribe anything important here. In the future any entity systems number would be subscribed here*/
    init {
        subscribe(editorCamera)
        subscribe(gameCamera)
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
            }
            GLFW_KEY_KP_1 -> {
                popLayer(simulateLayer)
                popOverlay(debugLayer)
                popOverlay(editorLayer)
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
    @Subscribe
    override fun destroy(event: Window.Destroy) {
        super.destroy(event)
        debugAPI.dispose()
        renderAPI.dispose()
    }

}

