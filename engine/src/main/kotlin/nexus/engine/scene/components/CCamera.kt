package nexus.engine.scene.components

import com.artemis.Component
import nexus.engine.camera.Camera
import nexus.engine.camera.CameraAdapter
import nexus.engine.math.Transform

/**
 * This component is applied to anything that should be represented in a 3d scene
 */
data class CCamera(val camera: Camera<*>) : Component() {

    /**
     * This is required because components require no-arg constructors
     */
    constructor() : this(CameraAdapter())

    /**
     * This allows us to accecss our transform directly via a () call on the variable instance
     */
    operator fun invoke(): Transform = this.camera
}