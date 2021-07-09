package marx.engine.math

import marx.engine.math.Comp.*

/**
 * This unifies all vectors.
 */
interface IVec<SELF : IVec<SELF>> {

    /*The component's we posses*/
    val components: Array<Comp> get() = arrayOf(None)

    /*This should divide add vector [OTHER] from this vector [SELF]*/
    operator fun <OTHER : IVec<*>> plus(other: OTHER): SELF

    /*This should divide subtract vector [OTHER] from this vector [SELF]*/
    operator fun <OTHER : IVec<*>> minus(other: OTHER): SELF

    /*This should divide this vector [SELF] by the other vector [OTHER]*/
    operator fun <OTHER : IVec<*>> div(other: OTHER): SELF

    /*This should multiple this vector [SELF] by the other vector [OTHER]*/
    operator fun <OTHER : IVec<*>> times(other: OTHER): SELF

    /*This should divide add vector [Number] from this vector [SELF]*/
    operator fun plus(number: Number): SELF = plus(Vec2(number))

    /*This should divide subtract vector [OTHER] from this vector [SELF]*/
    operator fun minus(number: Number): SELF = minus(Vec2(number))

    /*This should divide this vector [SELF] by the other vector [OTHER]*/
    operator fun div(number: Number): SELF = div(Vec2(number))

    /*This should multiple the given vec by the number*/
    operator fun times(number: Number): SELF = times(Vec2(number))

    /*Used to check if we have the component*/
    fun has(component: Comp) = components.contains(component)

    /*This should set the float at the given index*/
    operator fun set(
        component: Comp,
        value: Float
    )

    /*This should get the*/
    operator fun get(component: Comp): Float?

    /*This should get the*/
    operator fun get(
        component: Comp,
        default: Float
    ) = get(component) ?: default


}
