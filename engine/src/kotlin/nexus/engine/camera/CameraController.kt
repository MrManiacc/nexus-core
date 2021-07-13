@file:Suppress("UNCHECKED_CAST")

package nexus.engine.camera

import mu.KotlinLogging
import nexus.engine.Application
import nexus.engine.events.Events.App.Timestep
import nexus.engine.input.IInput
import nexus.engine.render.RenderAPI
import org.slf4j.Logger

/**
 * This interface is responsible for handling the camera's movement
 */
abstract class CameraController<API : RenderAPI>(target: Camera<*>) {
    private val logger: Logger = KotlinLogging.logger { }
    var camera: Camera<*> = target
        protected set

    /**
     * This is used to update the camera. It's pass the render scene which contains the camera for the given scene.
     * This interface allows for explict camera controllers for certain scenes. This means we could have a
     * orthographic camera controller for say a 2d side view of the scene that could be rendered out to a framebuffer
     * the to an imgui panel. [dt] is the current delta time of the engine. this is used to for movement that isn't
     * bound to the current fps. The [input] is used for mapping the input of the camera
     */
    protected open fun update(dt: Timestep, input: IInput): Camera<*> {
        return this.camera
    }

    /**
     * This will invoke our update method, first making sure to check that we are attempting to process to right camera.
     */
    fun process(app: Application<API>) = with(app) {
        val result = update(this.timestep, this.input)
        if (camera != result) {
            camera = result
            logger.debug("Updated camera to: $camera")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CameraController<*>

        if (camera != other.camera) return false

        return true
    }

    override fun hashCode(): Int {
        return camera.hashCode()
    }

    override fun toString(): String {
        return "CameraController(camera=$camera)"
    }


}