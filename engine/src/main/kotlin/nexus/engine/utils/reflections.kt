package nexus.engine.utils

import com.artemis.AspectSubscriptionManager
import com.artemis.BaseSystem
import com.artemis.SystemInvocationStrategy
import com.artemis.utils.Bag
import mu.KotlinLogging
import org.slf4j.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * This must be public
 */
private val logger: Logger = KotlinLogging.logger("nexus.engine.utils.reflections")

/**
 * This returns the value of the field of the name [name]. THis is used in the case of fields that are private
 * or protected or finial.
 */
fun <T : Any> Any.fieldEvaluation(name: String, type: KClass<T>): T? {
    var errorMessage: String? = null
    try {
        val field = this::class.java.getDeclaredField(name)
        field.trySetAccessible()
        field.isAccessible = true
        val value = field.get(this)
        if (type.isInstance(value)) return value as T
    } catch (exception: Exception) {
        errorMessage = exception.localizedMessage
    }
    logger.error("Error, failed to find field with name $name in class ${this.javaClass.name}${
        if (errorMessage != null) ", also received exception message: $errorMessage." else "."
    }")
    return null
}


/**
 * This returns the value of the field of the name [name]. THis is used in the case of fields that are private
 * or protected or finial.
 */
inline fun <reified T : Any> Any.fieldEvaluation(name: String): T? = fieldEvaluation(name, T::class)

/**
 * This returns the value of the field of the name [name]. THis is used in the case of fields that are private
 * or protected or finial.
 */
fun <T : Any> Any.methodInvocation(name: String, type: KClass<T>, vararg parameters: Any): T? {
    val types: Array<Class<*>> = parameters.map { it::class.java }.toTypedArray()
    var errorMessage: String? = null
    try {
        val method = this::class.java.getDeclaredMethod(name, *types)
        method.trySetAccessible()
        method.isAccessible = true
        val value = method.invoke(this, *parameters)
        if (type.isInstance(value)) return value as T
    } catch (exception: Exception) {
        errorMessage = exception.localizedMessage
    }
    logger.error("Error, failed to find field with name $name in class ${this.javaClass.name}${
        if (errorMessage != null) ", also received exception message: $errorMessage." else "."
    }")
    return null
}


/**
 * This returns the value of the field of the name [name]. THis is used in the case of fields that are private
 * or protected or finial.
 */
inline fun <reified T : Any> Any.methodInvocation(name: String, vararg parameters: Any): T? =
    methodInvocation(name, T::class, *parameters)

///**
// * This will create a new instance of the given class the given parameters
// */
//inline fun <reified T : Any> KClass<T>.new(vararg parameter: Any): T {
//    val types: Array<KClass<*>> = parameter.map { it::class }.toTypedArray()
//    for (ctor in constructors) {
////        ctor.parameters
////        ctor.cal
//
//    }
//}
//
///**
// * This should be used to find the correct constructor for the given values
// */
//inline fun <reified T : Any> KClass<T>.findConstructor(vararg parameter: Any): KFunction<T> {
//    if (parameter.isEmpty()) return this.primaryConstructor
//        ?: error("Failed to find primrary constructor in class ${this.simpleName}????")
//    val paramsIn = parameter.map { it::class }.toTypedArray()
//    for (ctor in constructors) {
//        val params = ctor.parameters
//        if (params.size == parameter.size) {
//
//        }
//    }
//}


/**
 * This can be used for a delagation of hidden or protected value.
 * Use this to get the private or protected field values.
 */
class NamedDelegate(val propertyName: String) {
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T {
        if (thisRef == null) error("ExposedDelagation must be done from witing a class instance. No kotlin objects allowed")
        return thisRef.fieldEvaluation(propertyName)
            ?: error("Failed to find exportable property or name ${propertyName} and type ${T::class.simpleName}")
    }
}


/**
 * This can be used for a delagation of hidden or protected value.
 * Use this to get the private or protected field values.
 */
object PropertyDelegate {
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T {
        val name = if (property.hasAnnotation<PropertyName>())
            property.findAnnotation<PropertyName>()!!.name
        else
            property.name
        if (thisRef == null) error("ExposedDelagation must be done from witing a class instance. No kotlin objects allowed")
        return thisRef.fieldEvaluation(name)
            ?: error("Failed to find exportable property or name ${name} and type ${T::class.simpleName}")
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class PropertyName(val name: String)


/**
 * This allows for extensions/exposing of various things
 */
interface WorldExtender {
    /**
     * This should be used to expose the bag of systems using [NamedDelegate]
     */
    val systems: Bag<BaseSystem>

    /**
     * This should get the asm from the class using [NamedDelegate]
     */
    val subsManagager: AspectSubscriptionManager


    /**
     * This is used to configure our
     */
    val invokeStragegy: SystemInvocationStrategy


}