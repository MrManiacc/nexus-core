package nexus.engine.module.naming

import com.github.zafarkhaja.semver.ParseException
import com.google.common.base.Strings
import nexus.engine.module.ex.InvalidUrnException
import nexus.engine.module.ex.VersionParseException
import java.util.regex.Matcher
import com.github.zafarkhaja.semver.Version as VersionSem


/*======================================*
*                                       *
*              ResourceUrns             *
*                                       *
*=======================================*/

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
 * @return the string that was paresed into the correct [ResourceUrn.URN_REGEX]
 */
val String.toUrn: ResourceUrn get() = urn(this)

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


/*======================================*
*               Nameing                 *
*=======================================*/


/**
 * This is used to create a new name
 */
fun name(name: String): Name = Name(name)

/**
 * This is used for easy creation of names via a string extension
 */
val String.toName: Name get() = name(this)


/*======================================*
*               Version                 *
*=======================================*/


/**
 * This creates the version from the [version] string input. If the string ends with -SNAPSHOT then we mark the version
 * as a snapshot and remove the -SNAPSHOT flag fro mthe version
 * @throws ParseException if the given version input is invalid
 */
@Throws(ParseException::class) fun version(version: String): Version {
    try {
        var snapshot = false
        val ver: VersionSem = VersionSem.valueOf(if (version.endsWith("-SNAPSHOT")) {
            snapshot = true
            version.substringBeforeLast("-SNAPSHOT")
        } else version)
        return Version(ver, snapshot)
    } catch (e: ParseException) {
        throw VersionParseException("Invalid version '$version' - must be of the form MAJOR.minor.patch")
    }
}

/**
 * This creates the version from the [version] string input.
 * @throws ParseException if the given version input is invalid
 */
@Throws(ParseException::class) fun version(
    major: Int = 1,
    minor: Int = 0,
    patch: Int = 0,
    snapshot: Boolean = false,
): Version {
    require(!(major < 0 || minor < 0 || patch < 0)) { "Illegal version $major.$minor.$patch - all version parts must be positive" }
    return Version(VersionSem.forIntegers(major, minor, patch), snapshot)
}

/**
 * This creates a snapshot version from the [version] string input.
 * @throws ParseException if the given version input is invalid
 */
@Throws(ParseException::class) fun snapshot(major: Int = 1, minor: Int = 0, patch: Int = 0): Version =
    version(major, minor, patch, true)
