package marx.engine.events

import marx.engine.*
import marx.engine.nexus.plugins.glfw.IWindow
import marx.engine.render.*
import marx.engine.render.Shader.*
import marx.engine.utils.StringUtils.format
import marx.engine.render.Shader as RenderShader

/*
 * This stores all of our engine level abstract events.
 */
object Events {

    /**
     * Used for asset related actvities
     */
    object Asset {

    }

    /*
Used for imgui related events
     */
    class Shader {
        data class Compiled(
            val shader: RenderShader,
            val result: CompileResult
        ) : Event()
    }

    /*
Used for imgui related events
     */
    class Gui {
        class ViewportOverlay : Event()
        class PropertiesOverlay : Event()
    }

    /*
   This stores all of our window events
     */
    object Window {
        data class Initialized(val window: IWindow) : Event()

        data class Resize(
            val window: IWindow,
            var width: Int,
            var height: Int
        ) : Event()

        data class Destroy(val window: IWindow) : Event()
    }

    /*
   This stores all of our app lifecycle events
     */
    object App {
        /*
       This is used to pass the current timestep.
         */
        data class Timestep(
            var deltaTime: Float,
            var gameTime: Float
        ) : Event() {
            /*We are in seconds, to see the milliseconds, we must multiple by 1000 (1000 ms in 1 second)**/
            val milliseconds: Float get() = deltaTime * 1000f

            override fun toString(): String =
                "Timestep(time= ${gameTime.format(1)}, delta=${deltaTime.format(3)}, ms=${milliseconds.format(3)})"

        }

        data class Initialized(val app: Application<*>) : Event()

        data class Shutdown(val app: Application<*>) : Event()
    }

    object Input {
        data class KeyEvent(
            val window: IWindow,
            var key: Int,
            var scancode: Int,
            var action: Int,
            var mods: Int
        ) : Event()

        data class KeyRelease(
            val window: IWindow,
            var key: Int,
            var mods: Int
        ) : Event()

        data class KeyPress(
            val window: IWindow,
            var key: Int,
            var mods: Int
        ) : Event()

        data class KeyRepeat(
            val window: IWindow,
            var key: Int,
            var mods: Int
        ) : Event()

        data class MouseEvent(
            val window: IWindow,
            var button: Int,
            var action: Int,
            var mods: Int
        ) : Event()

        data class MousePress(
            val window: IWindow,
            var button: Int,
            var mods: Int
        ) : Event()

        data class MouseRelease(
            val window: IWindow,
            var button: Int,
            var mods: Int
        ) : Event()

        data class MouseRepeat(
            val window: IWindow,
            var button: Int,
            var mods: Int
        ) : Event()

        data class MouseScroll(
            val window: IWindow,
            var xOffset: Float,
            var yOffset: Float
        ) : Event()

        data class MouseMove(
            val window: IWindow,
            var x: Float,
            var y: Float
        ) : Event()
    }


}

abstract class Event : IEvent {
    var isHandled: Boolean = false
        private set

    fun handled() = true.also { isHandled = it }
}

interface IEvent