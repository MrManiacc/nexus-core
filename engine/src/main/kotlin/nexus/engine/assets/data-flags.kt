package nexus.engine.assets

import kotlin.reflect.KClass

interface AssetDataFlags {
    //This allows us to supply specific flags to the mesh when loading.
    val flags: MutableMap<KClass<*>, MutableMap<String, Any>>

    /**
     * This allows you to set a flag of pretty much anything to the given value
     */
    fun set(name: String, value: Any) {
        val map = flags.getOrPut(value::class) { HashMap() }
        map[name] = value
    }

    /**
     * This will get the flag of the given type/name or null
     */
    fun <T : Any> get(type: KClass<T>, name: String): T? {
        val flag = flags[type]?.get(name) ?: return null
        if (!type.isInstance(flag)) return null
        return flag as T
    }

}

/**
 * This will get the flag of the given type/name or null, using inlines
 */
inline operator fun <reified T : Any> AssetDataFlags.get(name: String): T? = get(T::class, name)