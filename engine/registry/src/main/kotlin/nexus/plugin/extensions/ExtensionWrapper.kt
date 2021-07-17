package nexus.plugin.extensions

import mu.KotlinLogging
import org.slf4j.Logger
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.createInstance

/**
 * This wraps around an extension interface allowing for overriding of original interfaces
 */
class ExtensionWrapper {
    private val extensions: MutableCollection<Any> = ArrayList()
    private val interfaces: MutableCollection<KClass<*>> = ArrayList()
    private val log: Logger = KotlinLogging.logger { }

    /**
     * This should be used only to add classes that implement the [extensionInterface].
     */
    fun add(instance: Any) {
        if (instance is Class<*>) {
            add(instance.kotlin)
            return
        }
        if (instance is KClass<*>) {
            if (instance.isAbstract) {
                interfaces.add(instance)
                log.debug("Added a new interface: ${instance.qualifiedName}")
            } else if (instance.isFinal) {
                add(instance.createInstance())
                return
            }
        } else {
            val kClass = instance::class
            if (kClass.isFinal) {
                if (kClass.allSuperclasses.filter {
                        interfaces.contains(it)
                    }.isNotEmpty()) {
                    extensions.add(instance)
                    log.debug("Added new extension implementation to extension wrapper with class name: ${kClass.simpleName}")
                } else {
                    log.warn("Failed to find interface that is registered to this extension wrapper ${kClass.simpleName}")
                }
            }
        }
    }

    /**
     * This will filter out any interfaces and instances of the passed type
     */
    fun <T : Any> list(type: KClass<T>): Collection<T> {
        return extensions.filterIsInstance(type.java)
            .plus(interfaces.filterIsInstance(type.java))
    }

    /**
     * This will get a collection of extensions that are of type T
     */
    inline fun <reified T : Any> list(): Collection<T> = list(T::class)

    /**
     * This will get a collection of extensions that are of type T
     */
    inline fun <reified T : Any> forEach(block: T.() -> Unit) {
        list(T::class).forEach(block)
    }

    fun <T : Any> first(type: KClass<T>): T? {
        return list(type).firstOrNull()
    }

    /**
     * This will get a collection of extensions that are of type T
     */
    inline fun <reified T : Any> first(): T? = first(T::class)


}