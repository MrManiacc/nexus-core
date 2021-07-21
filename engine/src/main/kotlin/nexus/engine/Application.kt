package nexus.engine

//import marx.assets.fs.VirtualFileSystem

import com.artemis.BaseSystem
import dorkbox.messageBus.MessageBus
import nexus.engine.assets.Assets
import nexus.engine.camera.CameraController
import nexus.engine.events.Event
import nexus.engine.events.Events
import nexus.engine.events.Events.App.Initialized
import nexus.engine.events.Events.App.Timestep
import nexus.engine.events.IBus
import nexus.engine.events.IEvent
import nexus.engine.input.IInput
import nexus.engine.layer.LayerStack
import nexus.engine.render.RenderAPI
import nexus.engine.render.RenderScene
import nexus.engine.render.framebuffer.Framebuffer
import nexus.engine.scene.Scene
import nexus.engine.window.IWindow

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
    abstract val renderScene: RenderScene

    /**
     * This is used to render the scene
     */
    abstract var scene: Scene
        protected set


    /**
     * This is used to register our core systems privately
     */
    private fun initialize() {}

    /**
     * This builds modules, assets, and everything, file/path/resource related
     */
    protected open fun buildAssets() {
        val scannedTypes = Assets.typeManager.collect()
        scannedTypes.forEach {
            Assets.typeManager.addAssetType(it)
            log.info("Registered asset type: ${it.assetClass.simpleName}")
        }
    }

    /**
     * This is called upon the start of the application
     */
    open fun start() {
        buildAssets()
        initialize()
        subscribe(controller)
        instance = this
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
     * Called upon updating of the game
     */
    open fun onUpdate(event: Timestep) {
        scene.process(event.deltaTime)
        controller.process(this)
        for (layerId in size - 1 downTo 0) {
            val layer = layers[layerId]
            layer.onUpdate(event)
        }
        renderAPI.command.swap()
        renderAPI.command.poll()
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


    override fun shutdown() = eventbus.shutdown()

    companion object {
        private val globalTimestamp: Timestep = Timestep(0.1f, 1.0f)
        lateinit var instance: Application<*>

        /**
         * Provides static access to the message bus, TODO move stuff like this into some context/registry system
         */
        val BaseSystem.app: Application<*> get() = instance


    }


}

