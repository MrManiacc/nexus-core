package nexus.engine.editor.camera

import dorkbox.messageBus.annotations.*
import nexus.engine.events.*
import nexus.engine.camera.*
import nexus.engine.utils.MathUtils.radians
import org.joml.*

/**
 * Provides a 2d nexus.engine.camera projections
 */
class OrthographicCamera(
    left: Float,
    right: Float,
    bottom: Float,
    top: Float,
    var size: Float = 5f
) : Camera<OrthographicCamera>() {

    override var projectionMatrix: Matrix4f = Matrix4f().ortho(left, right, bottom, top, -1.0f, 1.0f)

    /*The player's view matrix**/
    override val viewMatrix: Matrix4f
        get() = viewBuffer.identity()
            .rotateZ(rotation.z.radians)
            .translate(position)
            .invert()

    /*This allows us to recompute our projection matrix every time our window resizes*/
    @Subscribe fun onResize(event: Events.Window.Resize) {
        val aspect = event.width.toFloat() / event.height.toFloat()
        projectionMatrix = projectionMatrix.identity().ortho(
            -aspect * size,
            aspect * size,
            -size,
            size,
            1.0f,
            -1.0f
        )
    }


}