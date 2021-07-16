package nexus.engine.layer

import nexus.engine.*
import nexus.engine.events.*
import nexus.engine.events.Events.App.Timestep
import nexus.engine.render.*
import nexus.engine.scene.*
import kotlin.reflect.*

/*
 * A nexus.engine.layer is like a chunk of methods and variables that will be rendered to screen. They are boxed sections that nexus.engine.render
 * specific things
 */

abstract class Layer<API : RenderAPI>(val app: Application<*>, private val rendererType: KClass<API>, val name: String = "nexus/engine/layer") {
    val renderAPI: API get() = Renderer(rendererType)
    val scene: RenderScene get() = renderAPI.scene
    open fun onAttach() = Unit
    open fun onDetach() = Unit
    abstract fun onUpdate(update: Timestep)
    open fun onEvent(event: Event){}
}