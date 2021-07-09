package marx.engine

import dorkbox.messageBus.*
import dorkbox.messageBus.annotations.*
//import marx.assets.fs.VirtualFileSystem
import marx.engine.events.*
import marx.engine.events.Events.App.Initialized
import marx.engine.events.Events.App.Timestep
import marx.engine.events.Events.Window
import marx.engine.events.Events.Window.Resize
import marx.engine.input.*
import marx.engine.layer.*
import marx.engine.nexus.plugins.glfw.IWindow
import marx.engine.render.*
import marx.engine.scene.*
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

/**
 * This is the main entry for the marx engine. It
 */
abstract class Application<API : RenderAPI>(assetPath: Path) : IBus, LayerStack {
    abstract val eventbus: MessageBus
    abstract val window: IWindow
    abstract val input: IInput
    abstract var isRunning: Boolean
    abstract var gameTime: Double
    abstract var startTime: Long
    val currentTime: Long get() = System.nanoTime()
//    @ExperimentalPathApi
//    protected val vsf: VirtualFileSystem = VirtualFileSystem.of(assetPath)

    /*This will get the render api for the specified [rendererType]**/
    abstract val renderAPI: API


    /*This is the root scene for the application**/
    abstract val scene: RenderScene

    override fun subscribe(listener: Any) = eventbus.subscribe(listener)


    override fun <T : IEvent> publish(event: T) {
        if (event is Event)
            for (layerId in size - 1 downTo 0) {
                val layer = layers[layerId]
                layer.onEvent(event)
                if (event.isHandled) return
            }
        eventbus.publish(event)
    }

    override fun <T : IEvent> publishAsync(event: T) {
        if (event is Event)
            for (layerId in size - 1 downTo 0) {
                val layer = layers[layerId]
                layer.onEvent(event)
                if (event.isHandled) return
            }
        eventbus.publishAsync(event)
    }

    /**
     * Called upon updating of the game
     */
    open fun onUpdate(event: Timestep) {
        for (layerId in size - 1 downTo 0) {
            val layer = layers[layerId]
            layer.onUpdate(event)
        }
        renderAPI.command.swap()
        renderAPI.command.poll()
    }

    @Subscribe
    fun onResize(event: Resize) =
        renderAPI.command.viewport(event.width to event.height, 0 to 0)

    /**
     * This is called upon the start of the application
     */
    @ExperimentalPathApi

    open fun start() {
        instance = this
//        vsf.refresh(true)
        subscribe(input)
        instance = this
        isRunning = true
        publish(Initialized(this))
        startTime = currentTime
        renderAPI.init()
        update()
        destroy()
    }

    /**
     * This is the main update loop.
     */
    open fun update() {
        while (isRunning && !window.shouldClose) {
            renderAPI.command.clear(floatArrayOf(0.1f, 0.1f, 0.1f))
            val now = currentTime
            val delta = (now - startTime) / 1E9
            gameTime += delta
            startTime = now
            timestep.gameTime = gameTime.toFloat()
            timestep.deltaTime = delta.toFloat()
            onUpdate(timestep) //Called before the global event
            publish(timestep)
        }
    }

    /**
     * This posts the shutdown event and then procendes to shutdown the main application
     */
    @ExperimentalPathApi

    open fun destroy() {
        publish(Events.App.Shutdown(this))
        shutdown()
        isRunning = false
    }

    @Subscribe
    open fun destroy(event: Window.Destroy) = renderAPI.dispose()

    override fun shutdown() = eventbus.shutdown()

    companion object {
        private val timestep: Timestep = Timestep(0.1f, 1.0f)
        lateinit var instance: Application<*>
    }


}

