package nexus.engine.resource

import com.google.common.base.Strings
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This is used for validity checking over urns
 */
fun isUrnValid(urn: String): Boolean = ResourceUrn.URN_REGEX.matches(urn)

/**
 * This method is used for creating new urn instances
 */
fun urn(group: Name, resource: Name, fragmentName: Name, instance: Boolean): ResourceUrn =
    ResourceUrn(group, resource, fragmentName, instance)


/**
 * This method is used for creating new urn instances
 */
fun urn(urn: String): ResourceUrn {
    val match: Matcher = ResourceUrn.URN_REGEX.toPattern().matcher(urn)
    if (match.matches()) {
        val groupName = name(match.group(1))
        val resourceName = name(match.group(2))
        val fragmentName = if (!Strings.isNullOrEmpty(match.group(3)))
            name(match.group(3))
        else Name.Empty
        val instance = !Strings.isNullOrEmpty(match.group(4))
        return urn(groupName, resourceName, fragmentName, instance)
    } else {
        throw InvalidUrnException("Invalid Urn: '$urn'")
    }
}


/**
 * This should be used for creating actual resource references.
 */
fun resourceUrn(group: Name, resource: Name): ResourceUrn =
    urn(group, resource, Name.Empty, false)

/**
 * This should be used for specifying a fragmented urn
 */
fun fragmentUrn(group: Name, resource: Name, fragmentName: Name): ResourceUrn =
    urn(group, resource, fragmentName, false)

/**
 * This is used for creating instances of urns
 */
fun instanceUrn(group: Name, resource: Name, fragmentName: Name): ResourceUrn =
    urn(group, resource, fragmentName, true)

/**
 * This is used for creating instances of urns
 */
fun instanceUrn(group: Name, resource: Name): ResourceUrn =
    urn(group, resource, Name.Empty, true)


/**
 * A ResourceUrn is a urn of the structure "{typeName}:{resourceName}[#{fragmentName}][!instance]".
 *
 *  * groupName is the name of the group containing or owning the resource
 *  * resourceName is the name of the resource
 *  * fragmentName is an optional identifier for a sub-part of the resource
 *  * an instance urn indicates a resource that is am independant copy of a resource identified by the rest of the urn
 *
 * ResourceUrn is immutable and comparable.
 *
 * @author Immortius
 */
data class ResourceUrn(
    /**
     * @return The group name part of the urn. This identifies the group that the resource belongs to.
     */
    val groupName: Name,

    /**
     * @return The resource name part of the urn. This identifies the resource itself.
     */
    val resourceName: Name,

    /**
     * @return The fragment name part of the urn. This identifies a sub-part of the resource.
     */
    val fragmentName: Name,

    /**
     * @return Whether this urn identifies an independent copy of the resource
     */
    val isInstance: Boolean,
) : Comparable<ResourceUrn> {
    /**
     * @return the fully qualified named, that was created matching the [Pattern] of the urn syntax
     */
    val qualifiedName: String

    /**
     * @return true if this is a fragment of a resource
     */
    val isFragment: Boolean get() = !this.fragmentName.isEmpty

    /**
     * @return The root of the ResourceUrn, without the fragment name or instance marker.
     */
    val rootUrn: ResourceUrn
        get() = if (fragmentName.isEmpty && !isInstance) {
            this
        } else ResourceUrn(groupName, resourceName, Name.Empty, false)

    /**
     * @return If this urn is an instance, returns the urn without the instance marker. Otherwise this urn.
     */
    val parentUrn: ResourceUrn
        get() {
            return if (isInstance) {
                ResourceUrn(groupName, resourceName, fragmentName, false)
            } else {
                this
            }
        }

    /**
     * @return This instance urn version of this urn. If this urn is already an instance, this urn is returned.
     */
    val instanceUrn: ResourceUrn
        get() {
            return if (!isInstance) {
                ResourceUrn(groupName, resourceName, fragmentName, true)
            } else {
                this
            }
        }

    /**
     * This is used for an easy way of getting the actual value of this urn
     */
    operator fun invoke(): String = qualifiedName

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override operator fun compareTo(other: ResourceUrn): Int {
        var result = groupName.compareTo(other.groupName)
        if (result == 0)
            result = resourceName.compareTo(other.resourceName)
        if (result == 0)
            result = fragmentName.compareTo(other.fragmentName)
        if (result == 0)
            if (isInstance && !other.isInstance) result = 1
            else if (!isInstance && other.isInstance) result = -1
        return result
    }

    /**
     * This is used to generate our property string using a string builder
     */
    override fun toString(): String = this()

    /**
     * This code simply generates the fully qualified named at the creation of the object
     */
    init {
        val stringBuilder = StringBuilder()
        stringBuilder.append(groupName)
        stringBuilder.append(RESOURCE_SEPARATOR)
        stringBuilder.append(resourceName)
        if (!fragmentName.isEmpty) {
            stringBuilder.append(ResourceUrn.FRAGMENT_SEPARATOR)
            stringBuilder.append(fragmentName)
        }
        if (isInstance) stringBuilder.append(ResourceUrn.INSTANCE_INDICATOR)
        this.qualifiedName = stringBuilder.toString()
    }

    /**
     * This stores our static fields for the resource urn
     */
    companion object {
        internal const val RESOURCE_SEPARATOR = ":"
        internal const val FRAGMENT_SEPARATOR = "#"
        internal const val INSTANCE_INDICATOR = "!instance"
        val URN_REGEX = "([^:]+):([^#!]+)(?:#([^!]+))?(!instance)?".toRegex()


        fun isValid(string: String): Boolean {
            return URN_REGEX.matches(string)
        }

    }

}


