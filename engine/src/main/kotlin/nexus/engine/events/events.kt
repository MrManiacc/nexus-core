package nexus.engine.events


import nexus.engine.Application
import nexus.engine.math.Vec2
import nexus.engine.render.Shader.CompileResult
import nexus.engine.scene.components.CTag
import nexus.engine.utils.StringUtils.format
import nexus.engine.window.IWindow
import nexus.engine.render.Shader as RenderShader

/*
 * This stores all of our engine level abstract nexus.engine.events.
 */
object Events {

    /**
     * Used for asset related actvities
     */
    object Asset {

    }

    /**
     * Used for entity events
     */
    object Entity {
        data class TaggedEntityRemoved(val entityId: Int, val tag: CTag) : Event()
        data class TaggedEntityAdded(val entityId: Int, val tag: CTag) : Event()
    }


    /*
Used for imgui related nexus.engine.events
     */
    class Shader {
        data class Compiled(
            val shader: RenderShader,
            val result: CompileResult,
        ) : Event()
    }

    /*
Used for imgui related nexus.engine.events
     */


    object Camera {
        data class Resize(var width: Int, var height: Int) : Event()
    }

    /*
   This stores all of our window nexus.engine.events
     */
    object Window {
        data class Initialized(val window: IWindow) : Event()

        data class Resize(
            val window: IWindow,
            var width: Int,
            var height: Int,
            var pos: Vec2 = Vec2(),
        ) : Event()


        data class Destroy(val window: IWindow) : Event()
    }

    /*
   This stores all of our app lifecycle nexus.engine.events
     */
    object App {
        /*
       This is used to pass the current timestep.
         */
        data class Timestep(
            var deltaTime: Float,
            var gameTime: Float,
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
            var mods: Int,
        ) : Event()

        data class KeyRelease(
            val window: IWindow,
            var key: Int,
            var mods: Int,
        ) : Event()

        data class KeyPress(
            val window: IWindow,
            var key: Int,
            var mods: Int,
        ) : Event()

        data class KeyRepeat(
            val window: IWindow,
            var key: Int,
            var mods: Int,
        ) : Event()

        data class MouseEvent(
            val window: IWindow,
            var button: Int,
            var action: Int,
            var mods: Int,
        ) : Event()

        data class MousePress(
            val window: IWindow,
            var button: Int,
            var mods: Int,
        ) : Event()

        data class MouseRelease(
            val window: IWindow,
            var button: Int,
            var mods: Int,
        ) : Event()

        data class MouseRepeat(
            val window: IWindow,
            var button: Int,
            var mods: Int,
        ) : Event()

        data class MouseScroll(
            val window: IWindow,
            var xOffset: Float,
            var yOffset: Float,
        ) : Event()

        data class MouseMove(
            val window: IWindow,
            var x: Float,
            var y: Float,
        ) : Event()
    }


}

abstract class Event : IEvent {
    var isHandled: Boolean = false
        private set

    fun handled() = true.also { isHandled = it }
}

interface IEvent