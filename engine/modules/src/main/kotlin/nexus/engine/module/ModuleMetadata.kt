package nexus.engine.module

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import nexus.engine.module.naming.Name
import nexus.engine.module.naming.Version
import nexus.engine.module.naming.i18n.I18nMap
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.cast


/**
 * Information describing a module.
 *
 * @author Immortius
 */
class ModuleMetadata {
    private val extensions: MutableMap<String, Any> = Maps.newHashMap()
    /**
     * @return The identifier of the module
     */
    /**
     * Sets the identifier of the module
     *
     * @param id The new identifier
     */
    var id: Name = Name.Empty
    /**
     * @return The version of the module
     */
    /**
     * Sets the version of the module
     *
     * @param version The new version
     */
    var version: Version = Version.Default

    var dependencies: MutableList<DependencyInfo> = Lists.newArrayList()
        private set
    private var displayName: I18nMap = I18nMap("")
    private var description: I18nMap = I18nMap("")

    /**
     * @return A list of the permissions required by this module, corresponding to permission sets installed in the security manager.
     */
    val requiredPermissions: Set<String> = Sets.newLinkedHashSet()

    constructor() {}
    constructor(id: Name, version: Version) {
        this.id = id
        this.version = version
    }

    /**
     * @return A displayable name of the module
     */
    fun getDisplayName(): I18nMap {
        return displayName
    }

    /**
     * @param displayName The new human-readable name of the module
     */
    fun setDisplayName(displayName: I18nMap) {
        this.displayName = displayName
    }

    /**
     * @return A human readable description of the module
     */
    fun getDescription(): I18nMap {
        return description
    }

    /**
     * @param description The new human-readable description of the module
     */
    fun setDescription(description: I18nMap) {
        this.description = description
    }

    /**
     * @param dependencyId The id of the module to get dependency information on
     * @return The depdendency information for a specific module, or null if no such dependency exists
     */
    fun getDependencyInfo(dependencyId: Name): DependencyInfo? {
        for (dependency in dependencies) {
            if (dependencyId == dependency.getId()) {
                return dependency
            }
        }
        return null
    }

    /**
     * @param extensionId  The identifier of the extension
     * @param expectedType The expected type of the extension
     * @param <T>          The expected type of the extension
     * @return The extension object, or null if it is missing or of an incompatible type
    </T> */
    fun <T : Any> getExtension(extensionId: String, expectedType: KClass<T>): T? {
        val extension = extensions[extensionId]
        return if (expectedType.isInstance(extension)) {
            expectedType.cast(extension)
        } else null
    }

    /**
     * Sets the value of an extension
     *
     * @param extensionId The identifier of the extension
     * @param extension   The extension object
     * @throws IllegalArgumentException if extensionId is a ReservedIds
     */
    fun setExtension(extensionId: String, extension: Any) {
        Preconditions.checkArgument(!RESERVED_IDS.contains(extensionId),
            "Reserved id '$extensionId' cannot be used to identify an extension")
        extensions[extensionId] = extension
    }

    override fun hashCode(): Int =
        Objects.hash(id, version, displayName, description, requiredPermissions, dependencies, extensions)


    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is ModuleMetadata) {
            val other = other
            return (id == other.id
                    && version == other.version
                    && displayName == other.displayName
                    && description == other.description
                    && requiredPermissions == other.requiredPermissions
                    && dependencies == other.dependencies
                    && extensions == other.extensions)
        }
        return false
    }

    companion object {
        /*
     * Constants for the names of each of the core metadata attributes.
     */
        const val ID = "id"
        const val VERSION = "version"
        const val DISPLAY_NAME = "displayName"
        const val DESCRIPTION = "description"
        const val DEPENDENCIES = "dependencies"
        const val REQUIRED_PERMISSIONS = "requiredPermissions"

        /**
         * The set of reserved ids that cannot be used by extensions.
         */
        val RESERVED_IDS: Set<String> =
            ImmutableSet.of(ID, VERSION, DISPLAY_NAME, DESCRIPTION, DEPENDENCIES, REQUIRED_PERMISSIONS)
    }
}
