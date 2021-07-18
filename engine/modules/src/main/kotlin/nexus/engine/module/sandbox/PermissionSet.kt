package nexus.engine.module.sandbox

import com.google.common.base.Preconditions
import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import com.google.common.collect.Sets
import com.google.common.reflect.Reflection
import mu.KotlinLogging
import java.security.Permission
import kotlin.reflect.KClass


/**
 * A permission set is a group of Permissions and class access that can be granted together to a module.
 *
 * @author Immortius
 */
@Suppress("UnstableApiUsage") class PermissionSet : PermissionProvider {
    private val apiClasses: MutableSet<KClass<*>> = Sets.newHashSet()
    private val apiPackages: MutableSet<String> = Sets.newHashSet()
    private val globallyAllowedPermissionsTypes: MutableSet<KClass<out Permission>> = Sets.newHashSet()
    private val globallyAllowedPermissionsInstances: MutableSet<Permission> = Sets.newHashSet()
    private val allowedPermissionsTypes: SetMultimap<KClass<out Permission>, KClass<*>> = HashMultimap.create()
    private val allowedPermissionInstances: SetMultimap<Permission, KClass<*>> = HashMultimap.create()
    private val allowedPackagePermissionsTypes: SetMultimap<KClass<out Permission>, String> = HashMultimap.create()
    private val allowedPackagePermissionInstances: SetMultimap<Permission, String> = HashMultimap.create()

    /**
     * @param type The type to check whether access is permitted to
     * @return Whether access to this type is granted by the permission set
     */
    override fun isPermitted(type: KClass<*>): Boolean {
        return apiClasses.contains(type) || apiPackages.contains(Reflection.getPackageName(type.java))
    }

    /**
     * @param permission The permission to check
     * @param context    The context to check
     * @return Whether the given permission is granted in the given context, by this permission set
     */
    override fun isPermitted(permission: Permission, context: KClass<*>): Boolean {
        return (globallyAllowedPermissionsTypes.contains(permission::class) || globallyAllowedPermissionsInstances.contains(
            permission)
                || allowedPermissionInstances[permission].contains(context)
                || allowedPermissionsTypes[permission::class].contains(context)
                || allowedPackagePermissionInstances[permission].contains(Reflection.getPackageName(context.java))
                || allowedPackagePermissionsTypes[permission::class].contains(Reflection.getPackageName(context.java)))
    }

    /**
     * Registers a global permission that all modules are granted
     *
     * @param permission The class of permission to grant
     */
    fun grantPermission(permission: KClass<out Permission>) {
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        globallyAllowedPermissionsTypes.add(permission)
        logger.debug("Globally granted permission '{}'", permission)
    }

    /**
     * Registers a global permission that all modules are granted
     *
     * @param permission The permission to grant
     */
    fun grantPermission(permission: Permission) {
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        globallyAllowedPermissionsInstances.add(permission)
        logger.debug("Globally granted permission '{}'", permission)
    }

    /**
     * Registers a permission that modules are granted when working (directly or indirectly) through the given apiType
     *
     * @param apiType    The type that requires the permission
     * @param permission The class of permission to grant
     */
    fun grantPermission(apiType: KClass<*>, permission: KClass<out Permission>) {
        Preconditions.checkNotNull(apiType)
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        allowedPermissionsTypes.put(permission, apiType)
        logger.debug("Granted permission '{}' to '{}'", permission, apiType)
    }

    /**
     * Registers a permission that modules are granted when working (directly or indirectly) through the given apiType
     *
     * @param apiType    The type that requires the permission
     * @param permission The permission to grant
     */
    fun grantPermission(apiType: KClass<*>, permission: Permission) {
        Preconditions.checkNotNull(apiType)
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        allowedPermissionInstances.put(permission, apiType)
        logger.debug("Granted permission '{}' to '{}'", permission, apiType)
    }

    /**
     * Registers a permission that modules are granted when working (directly or indirectly) through the given package
     *
     * @param packageName The package that requires the permission
     * @param permission  The class of permission to grant
     */
    fun grantPermission(packageName: String, permission: KClass<out Permission>) {
        Preconditions.checkNotNull(packageName)
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        allowedPackagePermissionsTypes.put(permission, packageName)
        logger.debug("Granted permission '{}' to '{}.*'", permission, packageName)
    }

    /**
     * Registers a permission that modules are granted when working (directly or indirectly) through the given package
     *
     * @param packageName The package that requires the permission
     * @param permission  The permission to grant
     */
    fun grantPermission(packageName: String, permission: Permission) {
        Preconditions.checkNotNull(packageName)
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        allowedPackagePermissionInstances.put(permission, packageName)
        logger.debug("Granted permission '{}' to '{}.*'", permission, packageName)
    }

    /**
     * Adds a class to the API - this allows it to be used directly from a module context.
     *
     * @param clazz The class to add to the API.
     */
    fun addAPIClass(clazz: KClass<*>) {
        Preconditions.checkNotNull(clazz)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_API_CLASSES)
        }
        apiClasses.add(clazz)
        logger.debug("Added API class '{}'", clazz)
    }

    /**
     * Adds a package to the API - this allows any class from the package to be used directly from a module context.
     *
     * @param packageName The package to add to the API
     */
    fun addAPIPackage(packageName: String) {
        Preconditions.checkNotNull(packageName)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        apiPackages.add(packageName)
        logger.debug("Added API classes '{}.*'", packageName)
    }

    /**
     * Removes a permission that has previously been globally allowed
     *
     * @param permission The permission to revoke
     * @return Whether the permission was previously globally allowed
     */
    fun revokePermission(permission: KClass<out Permission>): Boolean {
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        logger.debug("Revoking global permission '{}'", permission)
        return globallyAllowedPermissionsTypes.remove(permission)
    }

    /**
     * Removes a permission that has previously been globally allowed
     *
     * @param permission The permission to revoke
     * @return Whether the permission was previously globally allowed
     */
    fun revokePermission(permission: Permission): Boolean {
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        logger.debug("Revoking global permission '{}'", permission)
        return globallyAllowedPermissionsInstances.remove(permission)
    }

    /**
     * Remove a permission that has previously been granted to calls passing through a given class
     * WARNING: Does not revoke permissions granted at a package level
     *
     * @param apiType    The api class to revoke the permission from
     * @param permission The permission to revoke
     * @return whether the permission had previously been granted to the given class.
     */
    fun revokePermission(apiType: KClass<*>, permission: Class<out Permission?>): Boolean {
        Preconditions.checkNotNull(apiType)
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        logger.debug("Revoking permission '{}' from '{}'", permission, apiType)
        return allowedPermissionsTypes.remove(permission, apiType)
    }

    /**
     * Remove a permission that has previously been granted to calls passing through a given class
     * WARNING: Does not revoke permissions granted at a package level
     *
     * @param apiType    The api class to revoke the permission from
     * @param permission The permission to revoke
     * @return whether the permission had previously been granted to the given class.
     */
    fun revokePermission(apiType: KClass<*>, permission: Permission): Boolean {
        Preconditions.checkNotNull(apiType)
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        logger.debug("Revoking permission '{}' from '{}'", permission, apiType)
        return allowedPermissionInstances.remove(permission, apiType)
    }

    /**
     * Remove a permission that has previously been granted to a calls passing through a given package.
     * This will also revoke permissions granted at a class level, for classes within the package
     *
     * @param packageName The package to revoke the permission from
     * @param permission  The class of permission to revoke
     */
    fun revokePermission(packageName: String, permission: KClass<out Permission>) {
        Preconditions.checkNotNull(packageName)
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        logger.debug("Revoking permission '{}' from '{}.*'", permission, packageName)
        allowedPackagePermissionsTypes.remove(permission, packageName)
        val iterator = allowedPermissionsTypes[permission].iterator()
        while (iterator.hasNext()) {
            val clazz = iterator.next()
            if (packageName == Reflection.getPackageName(clazz.java)) {
                iterator.remove()
            }
        }
    }

    /**
     * Remove a permission that has previously been granted to a calls passing through a given package.
     * This will also revoke permissions granted at a class level, for classes within the package
     *
     * @param packageName The package to revoke the permission from
     * @param permission  The permission to revoke
     */
    fun revokePermission(packageName: String, permission: Permission) {
        Preconditions.checkNotNull(packageName)
        Preconditions.checkNotNull(permission)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        logger.debug("Revoking permission '{}' from '{}.*'", permission, packageName)
        allowedPackagePermissionInstances.remove(permission, packageName)
        val iterator = allowedPermissionInstances[permission].iterator()
        while (iterator.hasNext()) {
            val clazz = iterator.next()
            if (packageName == Reflection.getPackageName(clazz.java)) {
                iterator.remove()
            }
        }
    }

    /**
     * Removes a specific class from the list of API classes.
     * WARNING: This does not revoke access if granted at the package level.
     *
     * @param clazz The class to remove from the API
     * @return Whether the class was perviously an API class
     */
    fun revokeAPIClass(clazz: KClass<*>): Boolean {
        Preconditions.checkNotNull(clazz)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_API_CLASSES)
        }
        logger.debug("Removing from API '{}'", clazz)
        return apiClasses.remove(clazz)
    }

    /**
     * Removes a package and all contained classes from the list of API classes and packages.
     *
     * @param packageName The package to remove from the API
     */
    fun revokeAPIPackage(packageName: String) {
        Preconditions.checkNotNull(packageName)
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(ModuleSecurityManager.UPDATE_ALLOWED_PERMISSIONS)
        }
        logger.debug("Removing from API '{}.*'", packageName)
        apiPackages.remove(packageName)
        val iterator = apiClasses.iterator()
        while (iterator.hasNext()) {
            val clazz = iterator.next()
            if (packageName == Reflection.getPackageName(clazz.java)) {
                iterator.remove()
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
