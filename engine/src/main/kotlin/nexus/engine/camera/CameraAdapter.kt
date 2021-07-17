package nexus.engine.camera

import org.joml.Matrix4f

/**
 * this can be used to create an anonymous camera
 */
class CameraAdapter : Camera<CameraAdapter>() {
    override var projectionMatrix: Matrix4f = Matrix4f()
    override val viewMatrix: Matrix4f = Matrix4f()
}