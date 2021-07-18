package nexus.engine.module.sandbox

import java.security.Permission
import kotlin.reflect.KClass


/**
 * Provides checks for what classes and permissions are permitted to a module/ModuleClassLoader.
 */
interface PermissionProvider {
    /**
     * @param type The class to check
     * @return Whether access to the given class is permitted
     */
    fun isPermitted(type: KClass<*>): Boolean

    /**
     * @param permission The permission to check
     * @param context    The type invoking the permission check
     * @return Whether access to the given permission is permitted
     */
    fun isPermitted(permission: Permission, context: KClass<*>): Boolean
}
