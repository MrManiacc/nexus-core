package nexus.engine.module.sandbox

import com.google.common.collect.ImmutableList
import javassist.ClassPool
import javassist.CtClass
import mu.KotlinLogging
import nexus.engine.module.Module
import nexus.engine.module.ModuleClassLoader
import nexus.engine.module.naming.Name
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction

/**
 * A classloader to use when loading modules. This classloader ties into the sandboxing of modules by:
 * <ul>
 * <li>Acting as an indicator that a class belongs to a module - any class whose classloader is an instance of ModuleClassLoader comes from a module.</li>
 * <li>Restricting the classes visible to modules to those belonging those the module module has access to - as determined by the PermissionProvider.
 * Accessing any other class outside of the modules results in a ClassNotFoundException</li>
 * </ul>
 * <p>
 * Additionally, the ModuleClassLoader provides hooks for any injection that needs to be done to module classes as they are loaded, via javassist.
 * </p>
 *
 * @author Immortius
 */

class JavaModuleClassloader(
    /**
     * The name of the module this classloader belongs to
     */
    override val moduleId: Name,
    /**
     * The security manager that sandboxes classes
     */
    override val permissionProvider: PermissionProvider,
    /**
     * The urls where teh module classes can be found
     */
    urls: Array<URL>,
    /**
     * There parent class loader. Where the API classes can be found.
     */
    parent: ClassLoader,
    /**
     * A collection of byte code injectors to pass all loaded module code through.
     * This isn't required
     */
    injectors: Iterable<BytecodeInjector> = emptyList(),
) : URLClassLoader(urls, parent), ModuleClassLoader {
    private val injectors: List<BytecodeInjector> = ImmutableList.copyOf(injectors)
    private val pool: ClassPool?

    init {
        if (this.injectors.isNotEmpty()) {
            pool = ClassPool(ClassPool.getDefault())
            for (url in urls) {
                try {
                    logger.debug("Module path: {}", url.toURI())
                    pool.appendClassPath(File(url.toURI()).toString())
                } catch (e: Exception) {
                    logger.error("Failed to process module url: {}", url)
                }
            }
        } else pool = null
    }

    /**
     * @return The non-ModuleClassLoader that the module classloader chain is based on
     */
    private fun getBaseClassLoader(): ClassLoader {
        return if (parent is JavaModuleClassloader) {
            (parent as JavaModuleClassloader).getBaseClassLoader()
        } else parent
    }

    @Throws(ClassNotFoundException::class) override fun loadClass(name: String, resolve: Boolean): Class<*>? {
        val clazz: Class<*> = try {
            getBaseClassLoader().loadClass(name)
        } catch (e: ClassNotFoundException) {
            super.loadClass(name, resolve)
        }

        val parentLoader: ClassLoader = AccessController.doPrivileged(ObtainClassloader(clazz))
        return if (parentLoader !== this && parentLoader !is ModuleClassLoader) {
            if (permissionProvider.isPermitted(clazz.kotlin)) {
                clazz
            } else {
                logger.error("Denied access to class (not allowed with this module's permissions): {}",
                    name)
                null
            }
        } else clazz
    }

    /**
     * this override the default url class laoder's find class to provide us with runtime class defintions via
     * javaassist
     */
    @Throws(ClassNotFoundException::class) override fun findClass(name: String): Class<*>? {
        return if (name.startsWith("java.")) {
            null
        } else try {
            if (pool != null) {
                AccessController.doPrivileged(PrivilegedExceptionAction {
                    val cc: CtClass = pool.get(name)
                    for (injector in injectors) {
                        injector.inject(cc)
                    }
                    val b = cc.toBytecode()
                    defineClass(name, b, 0, b.size)
                } as PrivilegedExceptionAction<Class<*>>)
            } else {
                super.findClass(name)
            }
        } catch (e: PrivilegedActionException) {
            throw ClassNotFoundException("Failed to find or load class $name", e.cause)
        }
    }


    companion object {
        private val logger = KotlinLogging.logger { }

        /**
         * This will create a new instance of our
         */
        fun create(module: Module, parent: ClassLoader, permissionProvider: PermissionProvider): ModuleClassLoader {
            val urls = module.classpaths.map {
                try {
                    it.toURI().toURL()
                } catch (ex: MalformedURLException) {
                    logger.error { "Malformed url for file: $it" }
                    null
                }
            }.filterNotNull().toTypedArray()
            return JavaModuleClassloader(module.id,
                urls = urls,
                parent = parent,
                permissionProvider = permissionProvider)
        }
    }
}