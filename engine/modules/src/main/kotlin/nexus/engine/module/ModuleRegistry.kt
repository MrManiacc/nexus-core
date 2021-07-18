package nexus.engine.module

import nexus.engine.module.naming.Name
import nexus.engine.module.naming.Version


/**
 * ModuleRegistry is a specialised collection of modules with lookup support for latest versions, specific version or all versions of a particular module.
 *
 *
 * For add operations, if a module already exists in the registry it will not be added (similar to a set)
 *
 *
 * @author Immortius
 */
interface ModuleRegistry : MutableCollection<Module> {
    /**
     * @param id The name of the modules to return
     * @return A list of all versions of the module with the given id
     */
    fun getModuleVersions(id: Name): Collection<Module>

    /**
     * @return A complete collection of all available module names
     */
    val moduleIds: Set<Any>

    /**
     * @param id The name of the module to return
     * @return The most recent version of the desired module, or null if there is no matching module
     */
    fun getLatestModuleVersion(id: Name): Module

    /**
     * @param id         The name of the module to return
     * @param minVersion The lower bound (inclusive) on the version desired
     * @param maxVersion The upper bound (exclusive) on the version desired
     * @return The most recent version of the desired module within the bounds, or null if there is no matching module
     */
    fun getLatestModuleVersion(id: Name, minVersion: Version, maxVersion: Version): Module

    /**
     * Retrieves a specific module
     *
     * @param moduleId The name of the module
     * @param version  The version of the module
     * @return The module, or null if it doesn't exist in the registry
     */
    fun getModule(moduleId: Name, version: Version): Module
}
