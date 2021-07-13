package nexus.engine.editor.camera

import nexus.engine.camera.Camera
import nexus.engine.utils.MathUtils.radians
import org.joml.Matrix4f

/**
 * Provides a 2d nexus.engine.camera projections
 */
class OrthoCamera(
    left: Float,
    right: Float,
    bottom: Float,
    top: Float,
    var size: Float = 5f
) : Camera<OrthoCamera>() {

    override var projectionMatrix: Matrix4f = Matrix4f().ortho(left, right, bottom, top, -1.0f, 1.0f)

    /*The player's view matrix**/
    override val viewMatrix: Matrix4f
        get() = viewBuffer.identity()
            .rotateZ(rotation.z.radians)
            .translate(position)
            .invert()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrthoCamera

        if (size != other.size) return false
        if (projectionMatrix != other.projectionMatrix) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + projectionMatrix.hashCode()
        return result
    }

    override fun toString(): String {
        return "OrthoCamera(size=$size, projectionMatrix=$projectionMatrix)"
    }


}