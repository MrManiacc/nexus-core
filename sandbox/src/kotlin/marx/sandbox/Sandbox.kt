package marx.sandbox

import com.google.common.collect.*
import dorkbox.messageBus.*
import dorkbox.messageBus.annotations.*
import marx.engine.*
import marx.engine.events.Events.Input.KeyPress
import marx.engine.events.Events.Window
import marx.engine.events.Events.Window.Initialized
import marx.engine.input.*
import marx.engine.layer.*
import marx.engine.render.*
import marx.engine.camera.*
import marx.engine.nexus.plugins.glfw.IWindow
import marx.engine.scene.*
import marx.sandbox.layer.*
import mu.*
import nexus.engine.editor.camera.OrthographicCamera
import nexus.engine.editor.layer.LayerImGui
import nexus.engine.editor.wrapper.DebugRenderAPI
import nexus.plugins.glfw.GlfwInput
import nexus.plugins.glfw.GlfwWindow
import nexus.plugins.opengl.GLRenderAPI
import nexus.plugins.opengl.GLScene
import org.lwjgl.glfw.GLFW.*
import org.slf4j.*
import java.nio.file.Path
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
    val debugScene: RenderScene = GLScene(DebugRenderAPI::class)
    val gameCamera: Camera<OrthographicCamera> get() = editorCamera //TODO make game camera
    override val scene: RenderScene = GLScene(GLRenderAPI::class)

    /*==================Render==================**/
    override val renderAPI: GLRenderAPI = GLRenderAPI(window, scene).apply(Renderer::register)
    val debugAPI: DebugRenderAPI = DebugRenderAPI(window, debugScene).apply(Renderer::register)
    private val editorLayer: Layer<DebugRenderAPI> = LayerImGui(this)
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

    /* This maps the layer's accordingly*/
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

