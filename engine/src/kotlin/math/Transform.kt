package marx.engine.math

import marx.engine.math.MathDSL.Conversions.rads3

import marx.engine.comps.Position.*
import marx.engine.comps.Rotation.*
import marx.engine.comps.Scale.*

import org.joml.*

/**
 * This is applied to game objects for rendering. This allows translating, rotating, and scaling in a 3d scene.
 */
open class Transform(
    val position: Vec3,
    val rotation: Vec3,
    val scale: Vec3
) : IVec<Transform> {
    var matrix: Matrix4f = Matrix4f()
        private set

    /*This is a dynamic variable that is updated per the position, rotation and scale*/
    private fun updateMatrix() {
        matrix.identity()
            .translate(position)
            .rotateXYZ(rotation.rads3)
            .scale(scale)
    }

    /*This should divide add vector [OTHER] from this vector [SELF]*/
    override fun <OTHER : IVec<*>> plus(other: OTHER): Transform {
        this.position.add(other[X, 0f], other[Y, 0f], other[Z, 0f])
        updateMatrix()
        return this
    }

    /*This should divide subtract vector [OTHER] from this vector [SELF]*/
    override fun <OTHER : IVec<*>> minus(other: OTHER): Transform {
        this.position.sub(other[X, 0f], other[Y, 0f], other[Z, 0f])
        updateMatrix()
        return this
    }

    /*This should divide this vector [SELF] by the other vector [OTHER]*/
    override fun <OTHER : IVec<*>> div(other: OTHER): Transform {
        this.position.div(other[X, 1f], other[Y, 1f], other[Z, 1f])
        updateMatrix()
        return this
    }

    /*This should multiple this vector [SELF] by the other vector [OTHER]*/
    override fun <OTHER : IVec<*>> times(other: OTHER): Transform {
        this.position.mul(other[X, 1f], other[Y, 1f], other[Z, 1f])
        updateMatrix()
        return this
    }

    infix fun scale(other: Number): Transform {
        this.scale.set(other.toFloat())
        updateMatrix()
        return this
    }

    infix fun rotate(other: Number): Transform {
        this.rotation.set(other.toFloat())
        updateMatrix()
        return this
    }

    infix fun position(other: Number): Transform {
        this.position.set(other.toFloat())
        updateMatrix()
        return this
    }

    infix fun <OTHER : IVec<*>> scale(other: OTHER): Transform {
        this.scale.set(other[Width, this.scale.x], other[Height, this.scale.y], other[Depth, this.scale.z])
        updateMatrix()
        return this
    }

    infix fun <OTHER : IVec<*>> rotate(other: OTHER): Transform {
        this.rotation.set(other[Pitch, rotation.x], other[Yaw, rotation.x], other[Roll, rotation.x])
        updateMatrix()
        return this
    }

    infix fun <OTHER : IVec<*>> position(other: OTHER): Transform {
        this.position.set(other[X, position.x], other[Y, position.y], other[Z, position.z])
        updateMatrix()
        return this
    }

    infix fun <OTHER : IVec<*>> translate(other: OTHER): Transform {
        this.position.add(other[X, position.x], other[Y, position.y], other[Z, position.z])
        updateMatrix()
        return this
    }

    /*This should set the float at the given index*/
    override fun set(
        component: Comp,
        value: Float
    ) {
        when (component) {
            X -> this.position.x = value
            Y -> this.position.y = value
            Z -> this.position.z = value
            Yaw -> this.rotation.x = value
            Pitch -> this.rotation.y = value
            Roll -> this.rotation.z = value
            Width -> this.scale.x = value
            Height -> this.scale.y = value
            Depth -> this.scale.z = value
        }
        updateMatrix()
    }

    /*This should get the*/
    override fun get(component: Comp): Float? =
        when (component) {
            X -> this.position.x
            Y -> this.position.y
            Z -> this.position.z
            Yaw -> this.rotation.x
            Pitch -> this.rotation.y
            Roll -> this.rotation.z
            Width -> this.scale.x
            Height -> this.scale.y
            Depth -> this.scale.z
            else -> null
        }

    infix fun setX(other: Number): Transform {
        this.position.x = other.toFloat()
        updateMatrix()
        return this
    }

    infix fun setY(other: Number): Transform {
        this.position.y = other.toFloat()
        updateMatrix()
        return this
    }

    infix fun setZ(other: Number): Transform {
        this.position.z = other.toFloat()
        updateMatrix()
        return this
    }

    infix fun x(other: Number): Transform {
        this.position.x += other.toFloat()
        updateMatrix()
        return this
    }

    infix fun y(other: Number): Transform {
        this.position.y += other.toFloat()
        updateMatrix()
        return this
    }

    infix fun z(other: Number): Transform {
        this.position.z += other.toFloat()
        updateMatrix()
        return this
    }

    infix fun setPitch(other: Number): Transform {
        this.rotation.x = other.toFloat()
        updateMatrix()

        return this
    }

    infix fun setYaw(other: Number): Transform {
        this.rotation.y = other.toFloat()
        updateMatrix()
        return this
    }

    infix fun setRoll(other: Number): Transform {
        this.rotation.z = other.toFloat()
        updateMatrix()
        return this

    }

    infix fun pitch(other: Number): Transform {
        this.rotation.x += other.toFloat()
        updateMatrix()
        return this
    }

    infix fun yaw(other: Number): Transform {
        this.rotation.y += other.toFloat()
        updateMatrix()
        return this
    }

    infix fun roll(other: Number): Transform {
        this.rotation.z += other.toFloat()
        updateMatrix()
        return this
    }

}