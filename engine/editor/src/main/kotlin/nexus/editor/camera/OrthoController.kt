package nexus.editor.camera

import dorkbox.messageBus.annotations.Subscribe
import mu.KotlinLogging
import nexus.editor.api.core.panels.ViewportPanel
import nexus.editor.api.events.UIEvents
import nexus.engine.camera.Camera
import nexus.engine.camera.CameraController
import nexus.engine.events.Events
import nexus.engine.events.Events.App.Timestep
import nexus.engine.input.IInput
import nexus.engine.render.RenderAPI
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import java.lang.Float.max

/**
 * This class manages the 2d camera movement for the orthographic camera.
 */
class OrthoController<API : RenderAPI>(camera: OrthoCamera) : CameraController<API>(camera) {
    private val log: Logger = KotlinLogging.logger { }
    private var zoomLevel: Float = 5f
    private var aspect: Float = 1920f / 1080f
    private var processInput = false

    /**
     * This is used to update the camera. It's pass the render scene which contains the camera for the given scene.
     * This interface allows for explict camera controllers for certain scenes. This means we could have a
     * orthographic camera controller for say a 2d side view of the scene that could be rendered out to a framebuffer
     * the to an imgui panel. [dt] is the current delta time of the engine. this is used to for movement that isn't
     * bound to the current fps. The [input] is used for mapping the input of the camera
     */
    override fun update(dt: Timestep, input: IInput): Camera<*> = with(input) {
        val cam = super.update(dt, input)
        if (processInput) {
            with(cam) {
                if (isKeyDown(GLFW.GLFW_KEY_D))
                    x(moveSpeed * dt.deltaTime)
                if (isKeyDown(GLFW.GLFW_KEY_A))
                    x(-moveSpeed * dt.deltaTime)
                if (isKeyDown(GLFW.GLFW_KEY_E))
                    roll(-lookSpeed * dt.deltaTime)
                if (isKeyDown(GLFW.GLFW_KEY_Q))
                    roll(lookSpeed * dt.deltaTime)
                if (isKeyDown(GLFW.GLFW_KEY_W))
                    y(-moveSpeed * dt.deltaTime)
                if (isKeyDown(GLFW.GLFW_KEY_S))
                    y(moveSpeed * dt.deltaTime)
            }
        }
        return cam
    }

    @Subscribe
    fun onWindowFocus(event: UIEvents.NodeFocusSwitched) {
        this.processInput = event.focused is ViewportPanel<*>
    }

    /*This allows us to recompute our projection matrix every time our window resizes*/
    @Subscribe
    fun onScroll(event: Events.Input.MouseScroll) = with(this.camera) {
        if (processInput) {
            zoomLevel -= event.yOffset * 0.25f
            zoomLevel = max(zoomLevel, 0.25f)
            updateCamera(aspect, zoomLevel)
            log.info("Zoom camera for new zoom level: $zoomLevel, event: $event")
        }
    }



    /*This allows us to recompute our projection matrix every time our window resizes*/
    @Subscribe
    fun onResize(event: Events.Camera.Resize) = with(this.camera) {
        if (this is OrthoCamera) {
            aspect = event.width.toFloat() / event.height.toFloat()
            updateCamera(aspect, zoomLevel)
            log.info("Resized camera for new aspect: $aspect, event: $event")
        }
    }

    /**
     * Updates the cameras projection
     */
    private fun updateCamera(aspect: Float, zoom: Float) = with(this.camera) {
        projectionMatrix = projectionMatrix.identity().ortho(
            -aspect * zoom,
            aspect * zoom,
            -zoom,
            zoom,
            1.0f,
            -1.0f
        )
    }


}