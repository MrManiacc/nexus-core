package marx.engine.utils

/**
 * Provides utilities for strings
 */
object StringUtils {
    fun Number.format(digits: Int) = "%.${digits}f".format(this)
}