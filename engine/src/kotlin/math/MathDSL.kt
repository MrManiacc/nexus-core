package marx.engine.math

import marx.engine.comps.AxisAngle.*
import marx.engine.comps.Position.*
import marx.engine.comps.Rotation.*
import marx.engine.comps.Scale.*
import marx.engine.utils.MathUtils.radians
import org.joml.*

/**
 * This stores all of the domain specific language related things for joml classes.
 * That means it's purposes is to provide extensions to joml classes
 */
object MathDSL {

    object Conversions {
        /*Converts the vector in to radians*/
        val IVec<*>.rads2: Vec2
            get() = Vec2(this[X, 0f].radians, this[Y, 0f].radians)

        /*Converts the vector in to radians*/
        val IVec<*>.rads3: Vec3
            get() = Vec3(this[X, 0f].radians, this[Y, 0f].radians, this[Z, 0f].radians)

        /*Converts the vector in to radians*/
        val IVec<*>.rads4: Vec4
            get() = Vec4(this[X, 0f].radians, this[Y, 0f].radians, this[Z, 0f].radians, this[W, 0f].radians)

        /*Only a transform is a transform. No direct conversions from vectors*/
        val IVec<*>.isTransform: Boolean get() = this is Transform

        /*True if this vector2f*/
        val IVec<*>.isVec2: Boolean
            get() = this is Vector2fc ||
                    (this[X] != null && this[Y] != null)

        /*True if this rotational vector for rotation*/
        val IVec<*>.isRot2: Boolean
            get() = (this[Pitch] != null && this[Yaw] != null)

        /*True if this vector3f*/
        val IVec<*>.isVec3: Boolean
            get() = this is Vector3fc ||
                    (this[X] != null && this[Y] != null && this[Z] != null)

        /*True if this rotational vector for rotation*/
        val IVec<*>.isRot3: Boolean
            get() = (this[Pitch] != null && this[Yaw] != null && this[Yaw] != null)

        /*True if this vector4f*/
        val IVec<*>.isVec4: Boolean
            get() = this is Vector4fc ||
                    (this[X] != null && this[Y] != null && this[Z] != null && this[W] != null)

        /*True if this Angle4 ([AxisAngle4f])*/
        val IVec<*>.isAngle4: Boolean
            get() = this is AxisAngle4f ||
                    (this[X] != null && this[Y] != null && this[Z] != null && this[W] != null)

        val IVec<*>.vec2: Vec2
            get() = if (this is Vec2) this
            else Vec2(this[X, 0f], this[Y, 0f])

        val IVec<*>.rot2: Vec2
            get() = if (this is Vec2) this
            else Vec2(this[Pitch, this[X, 0f]], this[Yaw, this[Y, 0f]])

        val IVec<*>.array2: FloatArray
            get() {
                return floatArrayOf(
                    this[X, this[Pitch, this[Width, 0f]]],
                    this[Y, this[Yaw, this[Height, 0f]]]
                )
            }

        val IVec<*>.vec3: Vec3
            get() = if (this is Vec3) this
            else Vec3(this[X, 0f], this[Y, 0f], this[Z, 0f])

        val IVec<*>.array3: FloatArray
            get() {
                return floatArrayOf(
                    this[X, this[Pitch, this[Width, 0f]]],
                    this[Y, this[Yaw, this[Height, 0f]]],
                    this[Z, this[Roll, this[Depth, 0f]]]
                )
            }

        val IVec<*>.rot3: Vec3
            get() = if (this is Vec3) this
            else Vec3(this[Pitch, this[X, 0f]], this[Yaw, this[Y, 0f]], this[Roll, this[Z, 0f]])

        val IVec<*>.scale: Vec3
            get() = if (this is Vec3) this
            else Vec3(this[Width, this[X, 0f]], this[Height, this[Y, 0f]], this[Depth, this[Z, 0f]])

        val IVec<*>.vec4: Vec4
            get() = if (this is Vec4) this
            else Vec4(this[X, 0f], this[Y, 0f], this[Z, 0f], this[X, 0f])

        val IVec<*>.array4: FloatArray
            get() {
                return floatArrayOf(
                    this[X, this[Pitch, this[Width, 0f]]],
                    this[Y, this[Yaw, this[Height, 0f]]],
                    this[Z, this[Roll, this[Depth, 0f]]],
                    this[W, 0f]
                )
            }

        /*This will generate a new angle from this vector*/
        val IVec<*>.angle4: Angle4
            get() = if (this is Angle4) this
            else Angle4(
                this[AxisX, this[Pitch, this[X, 0f]]],
                this[AxisY, this[Yaw, this[Y, 0f]]],
                this[AxisZ, this[Roll, this[Z, 0f]]],
                this[Angle, this[W, 0f]]
            )

        /*Creates a quaternion from this vector instance*/
        val IVec<*>.quaternion: Quaternionf
            get() = Quaternionf(this.angle4)

        /*Converts the given vector to a transform*/
        val IVec<*>.transform: Transform get() = if (this is Transform) this else Transform(vec3, rot3, scale)
    }

    /*This stores various inline extensions for the different vectors*/
    object Extensions {

        /*Provides an extension for creating a vec2 via 2 numbers*/
        infix fun Number.via(other: Number): Vec2 = Vec2(this, other)

        /*Creates a vec3 from a vec2 with a number*/
        infix fun Vec2.via(other: Number): Vec3 = Vec3(this, other.toFloat())

        /*Creates a vec3 from a vec2 with a number*/
        fun Vec3.via(other: Number): Vec4 = Vec4(this, other.toFloat())

        infix fun Number.by(other: Number): Vec2 = Vec2(this, other)

        /*Creates a vec3 from a vec2 with a number*/
        infix fun Vec2.by(other: Number): Vec3 = Vec3(this, other.toFloat())

        /*Creates a vec3 from a vec2 with a number*/
        infix fun Vec3.by(other: Number): Vec4 = Vec4(this, other.toFloat())

        /*This adds our get refined get method so we can do [IVec<X>]*/
        inline operator fun <reified COMP : Comp> IVec<*>.get(default: () -> Float) = compOr<COMP>(default)

        /*This adds our get refined get method so we can do [IVec<X>]*/
        inline fun <reified COMP : Comp> IVec<*>.comp() = get(COMP::class.objectInstance!!)

        /*This adds our get refined get method so we can do [IVec<X>]*/
        inline fun <reified COMP : Comp> IVec<*>.compOr(default: Float) = compOr<COMP> { default }

        /*This adds our get refined get method so we can do [IVec<X>]*/
        inline fun <reified COMP : Comp> IVec<*>.compOr(default: () -> Float) = comp<COMP>() ?: default()

        /*This wil get a vector fro mthe given float array*/
        val FloatArray.vec2: Vec2
            get() = when (this.size) {
                0 -> Vec2()
                1 -> Vec2(this[0])
                else -> Vec2(this[0], this[1])
            }

        /*This wil get a vector fro mthe given float array*/
        val FloatArray.vec3: Vec3
            get() = when (this.size) {
                0 -> Vec3()
                1 -> Vec3(this[0])
                2 -> Vec3(this[0], this[1], 0f)
                else -> Vec3(this[0], this[1], this[2])
            }

        /*This wil get a vector fro mthe given float array*/
        val FloatArray.vec4: Vec4
            get() = when (this.size) {
                0 -> Vec4()
                1 -> Vec4(this[0])
                2 -> Vec4(this[0], this[1], 0f, 0f)
                3 -> Vec4(this[0], this[1], this[2], 0f)
                else -> Vec4(this[0], this[1], this[2], this[3])
            }

        /*This will update a float array from the given vector*/
        infix fun FloatArray.from(vector: Number) =
            this.fill(vector.toFloat())

        /*This will update a float array from the given vector*/
        infix fun FloatArray.from(vec: IVec<*>) {
            when (this.size) {
                1 -> {
                    this[0] = vec[X, vec[Pitch, vec[Width, 0.0f]]]
                }
                2 -> {
                    this[0] = vec[X, vec[Pitch, vec[Width, 0.0f]]]
                    this[1] = vec[Y, vec[Yaw, vec[Height, 0.0f]]]
                }
                3 -> {
                    this[0] = vec[X, vec[Pitch, vec[Width, 0.0f]]]
                    this[1] = vec[Y, vec[Yaw, vec[Height, 0.0f]]]
                    this[2] = vec[Z, vec[Roll, vec[Depth, 0.0f]]]
                }
                4 -> {
                    this[0] = vec[X, vec[Pitch, vec[Width, 0.0f]]]
                    this[1] = vec[Y, vec[Yaw, vec[Height, 0.0f]]]
                    this[2] = vec[Z, vec[Roll, vec[Depth, 0.0f]]]
                    this[4] = vec[W, 0f]
                }
            }
        }


    }


}