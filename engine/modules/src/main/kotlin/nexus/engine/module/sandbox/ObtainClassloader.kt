package nexus.engine.module.sandbox

import java.security.PrivilegedAction

/**
 * PrivilegedAction for obtaining the ClassLoader of a type.
 *
 * @author Immortius
 */
class ObtainClassloader(private val type: Class<*>) : PrivilegedAction<ClassLoader> {
    override fun run(): ClassLoader {
        return type.classLoader
    }
}
