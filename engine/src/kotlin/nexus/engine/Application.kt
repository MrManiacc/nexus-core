package nexus.engine

//import marx.assets.fs.VirtualFileSystem

import dorkbox.messageBus.MessageBus
import dorkbox.messageBus.annotations.Subscribe
import nexus.engine.camera.CameraController
import nexus.engine.events.Event
import nexus.engine.events.Events
import nexus.engine.events.Events.App.Initialized
import nexus.engine.events.Events.App.Timestep
import nexus.engine.events.Events.Window
import nexus.engine.events.Events.Window.Resize
import nexus.engine.events.IBus
import nexus.engine.events.IEvent
import nexus.engine.glfw.IWindow
import nexus.engine.input.IInput
import nexus.engine.layer.LayerStack
import nexus.engine.render.RenderAPI
import nexus.engine.render.framebuffer.Framebuffer
import nexus.engine.scene.RenderScene

/**
 * This is the main entry for the marx engine. It
 */
abstract class Application<API : RenderAPI>() : IBus, LayerStack {
    abstract val eventbus: MessageBus
    abstract val window: IWindow
    abstract val input: IInput
    abstract var isRunning: Boolean
    abstract var gameTime: Double
    abstract var startTime: Long
    val currentTime: Long get() = System.nanoTime()
    val timestep: Timestep get() = globalTimestamp
    val deltaTime: Float get() = timestep.deltaTime
    abstract var controller: CameraController<API>
    abstract val viewport: Framebuffer

    /*This will get the nexus.engine.render api for the specified [rendererType]**/
    abstract val renderAPI: API


    /*This is the root nexus.engine.scene for the application**/
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
        controller.process(this)
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
    open fun start() {
        subscribe(controller)
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
    open fun destroy() {
        publish(Events.App.Shutdown(this))
        shutdown()
        isRunning = false
    }

    @Subscribe
    open fun destroy(event: Window.Destroy) = renderAPI.dispose()

    override fun shutdown() = eventbus.shutdown()

    companion object {
        private val globalTimestamp: Timestep = Timestep(0.1f, 1.0f)
        lateinit var instance: Application<*>
    }


}

