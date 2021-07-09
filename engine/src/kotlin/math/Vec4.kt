package marx.engine.math

import marx.engine.comps.Position.*
import org.joml.*
import java.nio.*
import java.text.*

/*
 * This is our implementation of a vector4f. Its based upon joml's implementation
 */
class Vec4 : Vector4f, IVec<Vec4> {
    constructor() : super()
    constructor(v: Vector4fc?) : super(v)
    constructor(v: Vector4ic?) : super(v)
    constructor(
        v: Vector3fc,
        w: Float
    ) : super(v, w)

    constructor(
        v: Vector3ic?,
        w: Number
    ) : super(v, w.toFloat())

    constructor(
        v: Vector2fc,
        z: Number,
        w: Number
    ) : super(v, z.toFloat(), w.toFloat())

    constructor(
        v: Vector2ic,
        z: Number,
        w: Number
    ) : super(v, z.toFloat(), w.toFloat())

    constructor(d: Number) : super(d.toFloat())
    constructor(
        x: Number,
        y: Number,
        z: Number,
        w: Number
    ) : super(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())

    constructor(xyzw: FloatArray?) : super(xyzw)
    constructor(buffer: ByteBuffer?) : super(buffer)
    constructor(
        index: Int,
        buffer: ByteBuffer?
    ) : super(index, buffer)

    constructor(buffer: FloatBuffer?) : super(buffer)
    constructor(
        index: Int,
        buffer: FloatBuffer?
    ) : super(index, buffer)

    /*This should divide add vector [V] from this vector [SELF]*/
    override fun <V : IVec<*>> plus(other: V): Vec4 =
        Vec4(this.x + other[X, 0f], this.y + other[Y, 0f], this.z + other[Z, 0f], this.w + other[W, 0f])

    /*This should divide subtract vector [V] from this vector [SELF]*/
    override fun <V : IVec<*>> minus(other: V): Vec4 =
        Vec4(this.x - other[X, 0f], this.y - other[Y, 0f], this.z - other[Z, 0f], this.w - other[W, 0f])

    /*This should divide this vector [SELF] by the other vector [V]*/
    override fun <V : IVec<*>> div(other: V): Vec4 =
        Vec4(this.x / other[X, 1f], this.y / other[Y, 1f], this.z / other[Z, 1f], this.w / other[W, 1f])

    /*This should multiple this vector [SELF] by the other vector [V]*/
    override fun <V : IVec<*>> times(other: V): Vec4 =
        Vec4(this.x * other[X, 1f], this.y * other[Y, 1f], this.z * other[Z, 1f], this.w * other[W, 1f])

    /*This should set the float at the given index*/
    override operator fun set(
        component: Comp,
        value: Float
    ) {
        when (component.idx) {
            0 -> this.x = value
            1 -> this.y = value
            2 -> this.z = value
            3 -> this.w = value
        }
    }

    /*This should get the component value*/
    override operator fun get(component: Comp): Float =
        when (component.idx) {
            0 -> x
            1 -> y
            2 -> z
            3 -> w
            else -> Float.NaN
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is Vector4fc) return false
        return equals(other.x(), other.y(), other.z(), other.w())
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + java.lang.Float.floatToIntBits(x)
        result = prime * result + java.lang.Float.floatToIntBits(y)
        result = prime * result + java.lang.Float.floatToIntBits(z)
        result = prime * result + java.lang.Float.floatToIntBits(w)
        return result
    }

    override fun toString(): String = super.toString(DecimalFormat.getInstance())


}