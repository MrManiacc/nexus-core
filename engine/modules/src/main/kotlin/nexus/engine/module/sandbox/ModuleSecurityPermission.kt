package nexus.engine.module.sandbox

import java.security.BasicPermission

/**
 * For permissions relating to Module security.
 *
 * @see java.security.BasicPermission
 *
 * @see java.security.Permission
 *
 * @see java.security.Permissions
 *
 * @see java.security.PermissionCollection
 *
 * @see java.lang.SecurityManager
 */
class ModuleSecurityPermission : BasicPermission {
    constructor(name: String) : super(name) {}
    constructor(name: String, actions: String) : super(name, actions) {}

    companion object {
        /**
         * This permission allows permissions to be granted and revoked to the module sandbox
         */
        const val UPDATE_ALLOWED_PERMISSIONS = "updateAllowedPermission"

        /**
         * This permission allows the classes and packages available to the module sandbox to be updated
         */
        const val UPDATE_API_CLASSES = "updateAPIClasses"
    }
}
