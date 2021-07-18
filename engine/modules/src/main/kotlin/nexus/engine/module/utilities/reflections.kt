package nexus.engine.module.utilities

import com.googlecode.gentyref.GenericTypeReflector
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName


inline fun <reified T : Any> classNameOf(): String = T::class.qualifiedName ?: T::class.jvmName

/**
 * This will create a new list of strings from the [first] and [more]
 */
inline fun <reified T: Any> combineToList(first: T, vararg more: T): List<T> =
    more.toMutableList().also { it.add(0, first) }


/**
 * This will create a new list of strings from the [first] and [more]
 */
inline fun <reified T: Any> combineToSet(first: T, vararg more: T): Set<T> =
    more.toMutableList().also { it.add(0, first) }.toSet()


/**
 * This is used to easily create an optional value of a nullable type
 */
inline fun <reified T : Any> opt(value: T?): Optional<T> =
    Optional.ofNullable(value)


fun getTypeParameterBinding(target: Type, index: Int): Optional<Type> {
    return getClassOfType(target)?.let { getTypeParameterBindingForInheritedClass(target, it, index) }
        ?: throw IllegalArgumentException("Unsupported type: $target")
}

fun <T : Any> getTypeParameterBindingForInheritedClass(
    target: Type,
    superClass: KClass<T>,
    index: Int,
): Optional<Type> {
    return if (superClass.typeParameters.isEmpty()) {
        throw IllegalArgumentException("Class '$superClass' is not parameterized")
    } else {
        val classOfType = getClassOfType(target)
        if (classOfType == null) {
            throw IllegalArgumentException("Unsupported type: $target")
        } else if (!superClass.java.isAssignableFrom(classOfType.java)) {
            throw IllegalArgumentException("Class '$target' does not implement '$superClass'")
        } else {
            val type: Type = GenericTypeReflector.getExactSuperType(target, superClass.java)
            if (type is ParameterizedType) {
                val paramType = type.actualTypeArguments[index]
                if (paramType is Class<*> || paramType is ParameterizedType) {
                    return Optional.of(paramType)
                }
            }
            Optional.empty()
        }
    }
}

fun getClassOfType(type: Type): KClass<*>? {
    return if (type is Class<*>) {
        type.kotlin
    } else if (type is ParameterizedType) {
        (type.rawType as Class<*>).kotlin
    } else {
        if (type is WildcardType) {
            val upperBounds = type.upperBounds
            if (upperBounds.size == 1) {
                return getClassOfType(upperBounds[0])
            }
        }
        null
    }
}