package marx.engine.layer

import marx.engine.*
import marx.engine.events.*
import marx.engine.events.Events.App.Timestep
import marx.engine.render.*
import marx.engine.scene.*
import kotlin.reflect.*

/*
 * A layer is like a chunk of methods and variables that will be rendered to screen. They are boxed sections that render
 * specific things
 */

abstract class Layer<API : RenderAPI>(val app: Application<*>, private val rendererType: KClass<API>, val name: String = "layer") {
    val renderAPI: API get() = Renderer(rendererType)
    val scene: RenderScene get() = renderAPI.scene
    open fun onAttach() = Unit
    open fun onDetach() = Unit
    abstract fun onUpdate(update: Timestep)
    open fun onEvent(event: Event){}
}