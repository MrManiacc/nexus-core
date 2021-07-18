package nexus.engine.module.naming

import com.google.common.base.Preconditions
import java.util.*



/**
 * A name is a normalised string used as an identifier. Primarily this means it is case insensitive.
 *
 *
 * The original case-sensitive name is retained and available for display purposes, since it may use camel casing for readability.
 *
 *
 * This class is immutable.
 *
 *
 * 
 */
class Name(name: String) : Comparable<Name> {

    private val originalName: String
    private val normalisedName: String

    /**
     * @return Whether this name is empty (equivalent to an empty string)
     */
    val isEmpty: Boolean
        get() = normalisedName.isEmpty()

    /**
     * @return The Name in lowercase consistent with Name equality (so two names that are equal will have the same lowercase)
     */
    @Deprecated("""This is scheduled for removal in upcoming versions.
      Use {@code toString} or {@code displayName} instead.
      Note that a Name should not be transformed to a String for further processing.""") fun toLowerCase(): String {
        return normalisedName
    }

    /**
     * @return The Name in uppercase consistent with Name equality (so two names that are equal will have the same uppercase)
     */
    @Deprecated("""This is scheduled for removal in upcoming versions.
      Use {@code toString} or {@code displayName} instead.
      Note that a Name should not be transformed to a String for further processing.""") fun toUpperCase(): String {
        return originalName.toUpperCase(Locale.ENGLISH)
    }

    /**
     * This is used for sorting alphabetically
     */
    override operator fun compareTo(other: Name): Int =
        other.normalisedName.compareTo(normalisedName)


    /**
     * normalises the string and compares it to the normalisedName version used for @[Name]
     * @param other string to compare against
     * @return string comparision with normalisedName version 0 if match and unmatched with anything else
     */
    operator fun compareTo(other: String): Int {
        return normalisedName.compareTo(other.toLowerCase(Locale.ENGLISH))
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Name) {
            return normalisedName == obj.normalisedName
        }
        return false
    }

    /**
     * We want the hash to simply use the normalized name
     */
    override fun hashCode(): Int = normalisedName.hashCode()


    /**
     * @return The Name as string for display purposes
     */
    override fun toString(): String = originalName


    /**
     * This is used for an easy way of getting the actual value of this name
     */
    operator fun invoke(): String = toString()


    companion object {
        /**
         * The Name equivalent of an empty String
         */
        val Empty = Name("")
    }

    init {
        Preconditions.checkNotNull(name)
        originalName = name
        normalisedName = name.lowercase()
    }


}
