package nexus.engine.project

import dorkbox.messageBus.MessageBus
import mu.KotlinLogging
import nexus.engine.Application
import nexus.engine.input.IInput
import nexus.engine.layer.Layer
import nexus.engine.render.RenderAPI
import nexus.engine.render.RenderScene
import nexus.engine.render.framebuffer.Framebuffer
import nexus.engine.scene.Scene
import nexus.engine.window.IWindow
import org.slf4j.Logger

/**
 * This adds alot of default values and implementations and unifies the idea of a "project" away from the idea
 * of an "application"
 */
abstract class ProjectAdapter<API : RenderAPI>(
    /** This allow us to specify a custom implementation of a window.TODO: In the future i'd like to add a imgui popup window that implements this interface for compat**/
    override val window: IWindow,
    /**This must be overridden, typically with the glfw input. **/
    override val input: IInput,
    override val viewport: Framebuffer,
    override val renderScene: RenderScene,
    /**
     * This is used to render the scene
     */
    override var scene: Scene,
) : Application<API>() {
    override val eventbus: MessageBus = MessageBus()
    override var isRunning: Boolean = false
    override var gameTime: Double = 0.0
    override var startTime: Long = System.currentTimeMillis()
    override val log: Logger = KotlinLogging.logger { }
    override var insertIndex: Int = 0

    /**The layers stores the renderable contents **/
    override val layers: MutableList<Layer<*>> = ArrayList()



}