package nexus.engine.scene.components

import com.artemis.Component
import nexus.engine.math.Transform
import nexus.engine.math.Vec3

/**
 * This component is applied to anything that should be represented in a 3d scene
 */
data class CTransform(val transform: Transform) : Component() {
    constructor(transform: CTransform) : this(Transform(transform.transform))

    /**
     * This is used to create an instance of transform
     */
    constructor() : this(Transform(Vec3(0, 0, 0), Vec3(0, 0, 0), Vec3(1, 1, 1)))

    /**
     * This allows us to accecss our transform directly via a () call on the variable instance
     */
    operator

    fun invoke(): Transform = this.transform
}