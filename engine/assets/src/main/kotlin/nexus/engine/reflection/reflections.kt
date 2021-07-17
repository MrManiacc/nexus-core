package nexus.engine.reflection

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
fun combineToList(first: String, vararg more: String): List<String> =
    more.toMutableList().also { it.add(0, first) }

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


/**
 * This will scan the class path for annotated asstet types
 */
//    fun scanForAutoRegistrable(): List<ScannedAssetTypeInfo> {
//        val results = scanPackages("nexus")
//        val output = ArrayList<ScannedAssetTypeInfo>()
//        for (cls in results.getClassesWithAnnotation(classNameOf<RegisterAssetType>())) {
//            if (cls.implementsInterface(classNameOf<Asset<*>>())) {
//                val info = cls.getAnnotationInfo(classNameOf<RegisterAssetType>())
//                val values = info.parameterValues
//                val extension = values.getValue("extension") as Array<String>
//                val factory = values.getValue("factory") as AnnotationClassRef
//                output.add(ScannedAssetTypeInfo(cls.loadClass(Asset::class.java, true).kotlin,
//                    factory.loadClass(true).kotlin as KClass<out AssetFactory<*, *>>, extension))
//                logger.info("Found annotated class: ${cls.toStringWithSimpleNames()}")
//            }
//        }
//        return output
//    }

//
//    /**
//     * This will scan the given packages on the class path
//     */
//    fun scanPackages(vararg pkg: String): ScanResult = ClassGraph().enableAllInfo().acceptClasses(*pkg).scan()

//
//    data class ScannedAssetTypeInfo(
//        val assetClass: KClass<out Asset<*>>,
//        val factoryClass: KClass<out AssetFactory<*, *>>,
//        val extensions: Array<String>,
//    ) {
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (javaClass != other?.javaClass) return false
//
//            other as ScannedAssetTypeInfo
//
//            if (assetClass != other.assetClass) return false
//            if (factoryClass != other.factoryClass) return false
//            if (!extensions.contentEquals(other.extensions)) return false
//
//            return true
//        }
//
//        override fun hashCode(): Int {
//            var result = assetClass.hashCode()
//            result = 31 * result + factoryClass.hashCode()
//            result = 31 * result + extensions.contentHashCode()
//            return result
//        }
//    }
