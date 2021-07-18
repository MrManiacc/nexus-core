package nexus.engine.module.sandbox

import nexus.engine.module.ModuleClassLoader
import java.security.Permission
import kotlin.reflect.KClass

/**
 * <p>ModuleSecurityManager enforces permission access for modules.</p>
 * <p>The actual permissions are determined by the PermissionProvider associated with the module</p>
 * <p>
 * When checking permissions, only the stack down to the calling module (if any) is considered. This means that a module cannot exploit a package with higher
 * permissions.
 * </p>
 * <p>
 * AccessController.doPrivileged() is fully supported by this system, so non-module code can use this to avoid needing to be explicitly registered as allowing a permission
 * to modules using it, if the code is intended to run at the engine's security level.
 *
 * @see ModuleClassLoader
 */
class ModuleSecurityManager : SecurityManager() {
    private val calculatingPermission: ThreadLocal<Boolean> = ThreadLocal()

    override fun checkPermission(perm: Permission) {
        if (checkModuleDeniedAccess(perm))
            super.checkPermission(perm)
    }

    override fun checkPermission(perm: Permission, context: Any?) {
        if (checkModuleDeniedAccess(perm))
            super.checkPermission(perm)
    }

    /**
     * Checks whether a permission is allowed under the current module context.
     *
     * @param perm The permission under question
     * @return Whether the permission is denied
     */
    private fun checkModuleDeniedAccess(perm: Permission): Boolean {
        if (calculatingPermission.get() != null) {
            return false
        }
        calculatingPermission.set(true)
        try {
            val stack = classContext
            for (i in stack.indices) {
                val owningLoader = stack[i].classLoader
                if (owningLoader != null && owningLoader is ModuleClassLoader) {
                    return !checkAPIPermissionsFor(perm,
                        i,
                        stack.map { it.kotlin }.toTypedArray(),
                        (owningLoader as ModuleClassLoader).permissionProvider)
                }
            }
        } finally {
            calculatingPermission.set(null)
        }
        return true
    }

    /**
     * Checks the stack down to the first module to see if the given permission is allowed to be triggered from a module context.
     *
     * @param permission  The permission being checked
     * @param moduleDepth The depth of the first module class
     * @param stack       The classes involved in the current stack
     * @return Whether the permission has been granted to any of the API classes involved.
     */
    private fun checkAPIPermissionsFor(
        permission: Permission,
        moduleDepth: Int,
        stack: Array<KClass<*>>,
        permissionProvider: PermissionProvider,
    ): Boolean {
        for (i in moduleDepth - 1 downTo 0) {
            if (permissionProvider.isPermitted(permission, stack[i])) {
                return true
            }
        }
        return false
    }


    companion object {
        val UPDATE_ALLOWED_PERMISSIONS: Permission =
            ModuleSecurityPermission(ModuleSecurityPermission.UPDATE_ALLOWED_PERMISSIONS)
        val UPDATE_API_CLASSES: Permission = ModuleSecurityPermission(ModuleSecurityPermission.UPDATE_API_CLASSES)
    }

}