package nexus.engine.module.naming

import com.google.common.base.Preconditions
import java.util.*
import java.util.function.Predicate


/**
 * A range of versions from a lower-bound (inclusive) to an upper-bound (exclusive).
 *
 * @author Immortius
 */
class VersionRange(
    val lowerBound: Version,
    val upperBound: Version,
) : Predicate<Version> {

    /**
     * @param version The version to check
     * @return Whether version falls within the range
     */
    operator fun contains(version: Version): Boolean {
        return version >= lowerBound && version.compareTo(upperBound.getSnapshot()) < 0
    }

    override fun test(version: Version): Boolean {
        return contains(version)
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is VersionRange) {
            return lowerBound == other.lowerBound && upperBound == other.upperBound
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(lowerBound, upperBound)
    }

    override fun toString(): String {
        return "[$lowerBound,$upperBound)"
    }

    /**
     * lowerBound must be less than or equal to upperBound
     *
     * @param lowerBound The lower bound of the range. Cannot be null
     * @param upperBound The upper bound of the range. Cannot be null
     */
    init {
        Preconditions.checkNotNull<Any>(lowerBound)
        Preconditions.checkNotNull<Any>(upperBound)
        Preconditions.checkArgument(lowerBound.compareTo(upperBound) < 0, "upperBound must be greater than lowerBound")
    }
}
