package nexus.engine.module.sandbox

import org.slf4j.LoggerFactory
import java.security.Permission
import java.util.function.Predicate
import kotlin.reflect.KClass


/**
 * This provider factory wraps another factory. Whenever the other factory would deny access, this factory logs an error and grants permission.
 *
 * This is intended to allow code being developed to run regardless of permission issues, so that required permissions can be gathered.
 *
 * @author Immortius
 */
class WarnOnlyProviderFactory(
    /**
     * @param wrappedFactory Another permission factory to wrap.
     */
    private val wrappedFactory: PermissionProviderFactory,
) :
    PermissionProviderFactory {
    /**
     * @param module The module to create a permission provider for.
     * @param classpathModuleClasses A predicate that determines what classes on the classpath belong to the module
     * @return A permission provider suitable for the given module
     */
    override fun createPermissionProviderFor(
        module: nexus.engine.module.Module,
        classpathModuleClasses: Predicate<KClass<*>>,
    ): PermissionProvider {
        return object : PermissionProvider {
            private val wrapped = wrappedFactory.createPermissionProviderFor(module, classpathModuleClasses)

            override fun isPermitted(type: KClass<*>): Boolean {
                if (!wrapped.isPermitted(type)) {
                    logger.error("Use of non-permitted class '{}' detected by module '{}': this should be fixed for production use",
                        type.toString(),
                        module)
                }
                return true
            }

            /**
             * @param permission The permission to check
             * @param context    The type invoking the permission check
             * @return Whether access to the given permission is permitted
             */
            override fun isPermitted(permission: Permission, context: KClass<*>): Boolean {
                if (!wrapped.isPermitted(permission, context)) {
                    logger.error("Non-permitted permission '{}' required by module '{}', class '{}': this should be fixed for production use",
                        permission,
                        module,
                        context)
                }
                return true
            }


        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(WarnOnlyProviderFactory::class.java)
    }

}
