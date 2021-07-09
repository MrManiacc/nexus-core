package marx.engine.math

import marx.engine.comps.Position.*
import org.joml.*
import java.nio.*
import java.text.*

/*
 * This is our implementation of a vector4f. Its based upon joml's implementation
 */
class Vec2 : Vector2f, IVec<Vec2> {
    constructor() : super()
    constructor(d: Number) : super(d.toFloat())
    constructor(v: Vector2fc) : super(v)
    constructor(v: Vector2ic) : super(v)
    constructor(xy: FloatArray) : super(xy)
    constructor(
        x: Number,
        y: Number
    ) : super(x.toFloat(), y.toFloat())
    constructor(buffer: ByteBuffer) : super(buffer)
    constructor(
        index: Int,
        buffer: ByteBuffer
    ) : super(index, buffer)
    constructor(buffer: FloatBuffer) : super(buffer)
    constructor(
        index: Int,
        buffer: FloatBuffer
    ) : super(index, buffer)

    /*This should divide add vector [V] from this vector [SELF]*/
    override fun <V : IVec<*>> plus(other: V): Vec2 =
        Vec2(this.x + other[X, 0f], this.y + other[Y, 0f])

    /*This should divide subtract vector [V] from this vector [SELF]*/
    override fun <V : IVec<*>> minus(other: V): Vec2 =
        Vec2(this.x - other[X, 0f], this.y - other[Y, 0f])

    /*This should divide this vector [SELF] by the other vector [V]*/
    override fun <V : IVec<*>> div(other: V): Vec2 =
        Vec2(this.x / other[X, 1f], this.y / other[Y, 1f])

    /*should multiple this vector [SELF] by the other vector [V]*/
    override fun <V : IVec<*>> times(other: V): Vec2 =
        Vec2(this.x * other[X, 1f], this.y * other[Y, 1f])

    /*This should set the float at the given index*/
    override operator fun set(
        component: Comp,
        value: Float
    ) {
        when (component.idx) {
            0 -> this.x = value
            1 -> this.y = value
        }
    }

    /* This should get the component*/
    override operator fun get(component: Comp): Float? =
        when (component.idx) {
            0 -> x
            1 -> y
            else -> null
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is Vector2fc) return false
        return equals(other.x(), other.y())
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + java.lang.Float.floatToIntBits(x)
        result = prime * result + java.lang.Float.floatToIntBits(y)
        return result
    }

    override fun toString(): String = super.toString(DecimalFormat.getInstance())

}