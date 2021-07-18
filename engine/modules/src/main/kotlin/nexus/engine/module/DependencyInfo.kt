package nexus.engine.module

import nexus.engine.module.naming.Name
import nexus.engine.module.naming.Version
import nexus.engine.module.naming.VersionRange
import nexus.engine.module.naming.version
import java.util.*
import java.util.function.Predicate


/**
 * Describes a dependency on a module. Dependencies apply to a range of versions - anything from the min version (inclusive) to the max version (exclusive) are supported.
 *
 * @author Immortius
 */
class DependencyInfo {
    private var id: Name = Name("")
    private var minVersion: Version = version(1, 0, 0)
    private var maxVersion: Version? = null
    /**
     * An optional dependency does not need to be present for a module with the dependency to be used. If it is present, it must fall within
     * the allowed version range.
     *
     * @return Whether this dependency is optional
     */
    /**
     * Sets whether the dependency is optional
     *
     * @param optional Whether this dependency should be optional
     */
    var isOptional = false

    constructor() {}
    constructor(other: DependencyInfo) {
        id = other.id
        minVersion = other.minVersion
        maxVersion = other.maxVersion
        isOptional = other.isOptional
    }

    /**
     * @return The id of the module
     */
    fun getId(): Name {
        return id
    }

    /**
     * Sets the id of the module
     *
     * @param id The id of the module
     */
    fun setId(id: Name) {
        this.id = id
    }

    /**
     * @return The minimum supported version
     */
    fun getMinVersion(): Version {
        return minVersion
    }

    /**
     * The minimum supported version.
     *
     * @param value The minimum version
     */
    fun setMinVersion(value: Version) {
        minVersion = value
    }

    /**
     * The first unsupported version. If not explicitly specified, it is the next major version from minVersion.
     *
     * @return The maximum supported version (exclusive).
     */
    fun getMaxVersion(): Version {
        return if (maxVersion == null) {
            if (minVersion.major == 0) {
                minVersion.coreVersion.nextMinorVersion
            } else minVersion.coreVersion.nextMajorVersion
        } else maxVersion!!
    }

    /**
     * The upperbound of supported versions (exclusive)
     *
     * @param value The new upperbound
     */
    fun setMaxVersion(value: Version?) {
        maxVersion = value
    }

    /**
     * Returns a predicate that yields true when applied to version that is within the version range described by this dependency information
     */
    fun versionPredicate(): Predicate<Version> {
        return VersionRange(getMinVersion(), getMaxVersion())
    }

    override fun toString(): String {
        return java.lang.String.format("DependencyInfo [id=%s, minVersion=%s, maxVersion=%s, optional=%s]",
            id, minVersion, maxVersion, isOptional)
    }

    override fun hashCode(): Int {
        return Objects.hash(id, minVersion, maxVersion, isOptional)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj is DependencyInfo) {
            val other = obj
            return (id == other.id
                    && minVersion == other.minVersion
                    && maxVersion == other.maxVersion
                    && isOptional == other.isOptional)
        }
        return false
    }
}
