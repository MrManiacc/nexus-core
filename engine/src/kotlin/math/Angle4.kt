package marx.engine.math

import marx.engine.comps.AxisAngle.*
import marx.engine.comps.Position.*
import marx.engine.comps.Rotation.*
import org.joml.*

/*
 * This represents a rotation like interface.
 */
class Angle4 : AxisAngle4f, IVec<Angle4> {
    constructor() : super()
    constructor(a: AxisAngle4f?) : super(a)
    constructor(q: Quaternionfc?) : super(q)
    constructor(
        angle: Float,
        x: Float,
        y: Float,
        z: Float
    ) : super(angle, x, y, z)

    constructor(
        angle: Float,
        v: Vector3fc?
    ) : super(angle, v)

    /*This should divide add vector [OTHER] from this vector [SELF]*/
    override fun <OTHER : IVec<*>> plus(other: OTHER): Angle4 {
        this.x += other[AxisX, other[X, 0f]]
        this.y += other[AxisY, other[Y, 0f]]
        this.z += other[AxisZ, other[Z, 0f]]
        this.angle += other[Angle, other[W, 0f]]
        return this
    }

    /*This should divide subtract vector [OTHER] from this vector [SELF]*/
    override fun <OTHER : IVec<*>> minus(other: OTHER): Angle4 {
        this.x -= other[AxisX, other[X, 0f]]
        this.y -= other[AxisY, other[Y, 0f]]
        this.z -= other[AxisZ, other[Z, 0f]]
        this.angle -= other[Angle, other[W, 0f]]
        return this
    }

    /*This should divide this vector [SELF] by the other vector [OTHER]*/
    override fun <OTHER : IVec<*>> div(other: OTHER): Angle4 {
        this.x /= other[AxisX, other[X, 1f]]
        this.y /= other[AxisY, other[Y, 1f]]
        this.z /= other[AxisZ, other[Z, 1f]]
        this.angle /= other[Angle, other[W, 1f]]
        return this
    }

    /*This should multiple this vector [SELF] by the other vector [OTHER]*/
    override fun <OTHER : IVec<*>> times(other: OTHER): Angle4 {
        this.x += other[AxisX, other[X, 0f]]
        this.y += other[AxisY, other[Y, 0f]]
        this.z += other[AxisZ, other[Z, 0f]]
        this.angle += other[Angle, other[W, 0f]]
        return this
    }

    /*This should set the float at the given index*/
    override fun set(
        component: Comp,
        value: Float
    ) {
        when (component) {
            AxisX, X, Pitch -> this.x = value
            AxisY, Y, Yaw -> this.y = value
            AxisZ, Z, Roll -> this.z = value
            Angle -> this.angle = value
        }
    }

    /*This should get the angle components*/
    override fun get(component: Comp): Float? {
        return when (component) {
            AxisX, X, Pitch -> this.x
            AxisY, Y, Yaw -> this.y
            AxisZ, Z, Roll -> this.z
            Angle -> this.angle
            else -> null
        }
    }
}