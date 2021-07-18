package nexus.engine.module.naming

import com.github.zafarkhaja.semver.Version as SemverVersion


/**
 * Wrapper for a semantic version string - for version numbers of the form MAJOR.minor.patch(-SNAPSHOT). Allows the individual
 * elements to be retrieved, and for comparison between versions.
 *
 * @author Immortius
 */
class Version internal constructor(versionIn: SemverVersion, snapshot: Boolean) : Comparable<Version> {
    /**
     * @return the [versionIn] or a snapshot instance if [isSnapshot] is true
     */
    val semver: SemverVersion = if (snapshot) versionIn.setPreReleaseVersion(SNAPSHOT) else versionIn

    val major: Int
        get() = semver.majorVersion
    val minor: Int
        get() = semver.minorVersion
    val patch: Int
        get() = semver.patchVersion

    /**
     * @return Whether this version is a snapshot (work in progress)
     */
    val isSnapshot: Boolean
        get() = semver.preReleaseVersion.isNotEmpty()

    /**
     * @return A new version of this version as a snapshot
     */
    fun getSnapshot(): Version = Version(semver, true)

    val nextMajorVersion: Version
        get() = Version(semver.incrementMajorVersion(), false)

    val nextMinorVersion: Version
        get() = Version(semver.incrementMinorVersion(), false)

    val nextPatchVersion: Version
        get() = Version(semver.incrementPatchVersion(), false)

    /**
     * Returns the version core according to semver, i.e. the purely numerical version without any pre-release or build metadata info
     */
    val coreVersion: Version
        get() = version(semver.normalVersion)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is Version) {
            return semver.equals(other.semver)
        }
        return false
    }

    override fun hashCode(): Int = semver.hashCode()

    override fun toString(): String = semver.toString()

    override operator fun compareTo(other: Version) = semver.compareTo(other.semver)

    companion object {
        /**
         * A default version of 1.0.0
         */
        val Default = version(1, 0, 0)
        private const val SNAPSHOT = "SNAPSHOT"
    }
}
