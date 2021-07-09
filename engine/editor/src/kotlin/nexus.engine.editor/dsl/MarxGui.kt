package nexus.engine.editor.dsl

import imgui.*
import marx.engine.math.*
import marx.engine.math.MathDSL.Extensions.from
import marx.engine.camera.*

object MarxGui {
    private val position: FloatArray = FloatArray(3)
    private val rotation: FloatArray = FloatArray(3)
    private val scale: FloatArray = FloatArray(3)
    private val moveSpeed: FloatArray = FloatArray(1)
    private val lookSpeed: FloatArray = FloatArray(1)

    /**
     * This will render out the given transform using the [name].
     * Returns true if we updated the value
     */
    fun transform(
        name: String,
        transform: Transform,
        step: Float = 0.01f
    ): Boolean {

        position from transform.position
        rotation from transform.rotation
        scale from transform.scale
        var updated = false
        if (ImGui.dragFloat3("pos##$name", position, step)) {
            transform.position.set(position)
            updated = true
        }
        if (ImGui.dragFloat3("rot##$name", rotation, step)) {
            transform.rotation.set(rotation)
            updated = true
        }
        if (ImGui.dragFloat3("scale##$name", scale, step)) {
            transform.scale.set(scale)
            updated = true
        }
        return updated
    }

    /**
     * This will render out the given transform using the [name].
     * Returns true if we updated the value
     */
    fun camera(
        name: String,
        transform: Camera<*>,
        step: Float = 0.125f
    ): Boolean {
        position from transform.position
        rotation from transform.rotation
        moveSpeed from transform.moveSpeed * 100
        lookSpeed from transform.lookSpeed

        var updated = false
        if (ImGui.dragFloat3("pos##$name", position, step)) {
            transform.position.set(position)
            updated = true
        }
        if (ImGui.dragFloat3("rot##$name", rotation, step, 0f, 360f)) {
            transform.rotation.set(rotation)
            updated = true
        }
        if (ImGui.dragFloat("move speed##$name", moveSpeed, step, 0f, 1000f)) {
            transform.moveSpeed = moveSpeed[0] / 100.0f
            updated = true
        }
        if (ImGui.dragFloat("look speed##$name", lookSpeed, step, 0f, 1000f)) {
            transform.lookSpeed = lookSpeed[0]
            updated = true
        }
        return updated
    }


}

