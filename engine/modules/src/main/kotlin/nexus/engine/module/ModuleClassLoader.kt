package nexus.engine.module

import nexus.engine.module.naming.Name
import nexus.engine.module.sandbox.PermissionProvider
import java.io.IOException


/**
 * Interface for ModuleClassLoader. This allows for different base class loaders (for example
 * on jre and android).
 */
interface ModuleClassLoader {
    /**
     * @return The id of the module producing this class loader
     */
    val moduleId: Name

    /**
     * @return The class loader itself
     */
    val classLoader: ClassLoader
        get() = if (this !is ClassLoader) error("expected this to me instance of cloass loader!") else this

    /**
     * Closes this class loader (when relevant)
     *
     * @throws IOException If an exception occurs closing the classloader
     */
    @Throws(IOException::class) fun close()

    /**
     * @return The PermissionProvider determining what classes from this module
     * are allowed to do and access
     */
    val permissionProvider: PermissionProvider
}
