package nexus.engine.module.sandbox

import nexus.engine.module.Module
import java.util.function.Predicate
import kotlin.reflect.KClass

/**
 * Interface for factories that produce permission providers for modules.
 *
 * @author Immortius
 */
interface PermissionProviderFactory {
    /**
     * @param module The module to create a permission provider for.
     * @param classpathModuleClasses A predicate that determines what classes on the classpath belong to the module
     * @return A permission provider suitable for the given module
     */
    fun createPermissionProviderFor(module: Module, classpathModuleClasses: Predicate<KClass<*>>): PermissionProvider
}
