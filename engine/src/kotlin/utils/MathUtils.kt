package marx.engine.utils

object MathUtils {

    fun Number.orEquals(vararg numbers: Number): Int {
        var out = this.toInt()
        numbers.forEach {
            out = out or it.toInt()
        }
        return out
    }

    val Number.radians: Float get() = Math.toRadians(this.toDouble()).toFloat()

    val Number.degrees: Float get() = Math.toDegrees(this.toDouble()).toFloat()

}
