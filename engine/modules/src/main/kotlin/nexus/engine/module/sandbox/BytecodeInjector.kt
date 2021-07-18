package nexus.engine.module.sandbox

import javassist.CtClass

/**
 * An interface for classes that inject or modify byte code in classes during the load process.
 *
 * @author Immortius
 */
interface BytecodeInjector {
    
    /**
     * @param cc The class being loaded.
     */
    fun inject(cc: CtClass)
}
