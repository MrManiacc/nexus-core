package nexus.engine.module.sandbox

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import nexus.engine.module.Module
import org.slf4j.LoggerFactory
import java.security.Permission
import java.util.function.Predicate
import kotlin.reflect.KClass

/**
 * A permission provider factory that gives each module permissions based on a number of permission sets, where each module has access to the
 * default set of permissions and can request additional permission sets.
 *
 * @author Immortius
 * @see org.terasology.gestalt.module.sandbox.PermissionSet
 */
class StandardPermissionProviderFactory : PermissionProviderFactory {
    private val permissionSets: MutableMap<String, PermissionSet> = Maps.newHashMap()

    /**
     * @return The base permission set, which all modules are granted
     */
    val basePermissionSet: PermissionSet
        get() = getPermissionSet(BASE_PERMISSION_SET)!!

    /**
     * @param name The name of the permission set
     * @return The permission set with the given name, or null
     */
    fun getPermissionSet(name: String): PermissionSet? {
        return permissionSets[name]
    }

    /**
     * Adds or replaces a permission set that modules can request
     *
     * @param name          The name to give the permission set
     * @param permissionSet A permission set to associate with the given name
     */
    fun addPermissionSet(name: String, permissionSet: PermissionSet) {
        permissionSets[name] = permissionSet
    }

    /**
     * @param module The module to create a permission provider for.
     * @param classpathModuleClasses A predicate that determines what classes on the classpath belong to the module
     * @return A permission provider suitable for the given module
     */
    override fun createPermissionProviderFor(
        module: Module,
        classpathModuleClasses: Predicate<KClass<*>>,
    ): PermissionProvider {
        val grantedPermissionSets: MutableList<PermissionProvider> = Lists.newArrayList()
        basePermissionSet.let { grantedPermissionSets.add(it) }
        for (permissionSetId in module.requiredPermissions) {
            val set = getPermissionSet(permissionSetId)
            if (set != null) {
                grantedPermissionSets.add(set)
            } else {
                logger.warn("Module '{}' requires unknown permission '{}'", module, permissionSetId)
            }
        }
        grantedPermissionSets.add(PredicatePermissionProvider(classpathModuleClasses))
        return SetUnionPermissionProvider(grantedPermissionSets)
    }

    companion object {
        const val BASE_PERMISSION_SET = ""
        private val logger = LoggerFactory.getLogger(StandardPermissionProviderFactory::class.java)
    }

    init {
        permissionSets[BASE_PERMISSION_SET] = PermissionSet()
    }

    /**
     * A PermissionProvider that determines permitted classes via a predicate
     */
    class PredicatePermissionProvider(private val predicate: Predicate<KClass<*>>) :
        PermissionProvider {
        override fun isPermitted(type: KClass<*>): Boolean {
            return predicate.test(type)
        }

        /**
         * @param permission The permission to check
         * @param context    The type invoking the permission check
         * @return Whether access to the given permission is permitted
         */
        override fun isPermitted(permission: Permission, context: KClass<*>): Boolean {
            return false
        }

    }


    /**
     * This permission provider is based on a union of [PermissionProvider]. As long as one PermissionProvider in the provider has the permission it is granted.
     *
     * @author Immortius
     */
    class SetUnionPermissionProvider(permissionProviders: Iterable<PermissionProvider>) :
        PermissionProvider {

        private val permissionSets: ImmutableList<PermissionProvider> = ImmutableList.copyOf(permissionProviders)

        override fun isPermitted(type: KClass<*>): Boolean {
            for (set in permissionSets) {
                if (set.isPermitted(type)) {
                    return true
                }
            }
            return false
        }

        override fun isPermitted(permission: Permission, context: KClass<*>): Boolean {
            for (set in permissionSets) {
                if (set.isPermitted(permission, context))
                    return true
            }
            return false
        }

    }


}
