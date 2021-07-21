package nexus.engine.scene

import com.artemis.BaseSystem
import com.artemis.Entity
import kotlin.reflect.KClass

/**
 * This represents a virtual scene. It is a wrapper around an ecs world
 */
interface Scene {
    /**
     * This will hopefully create an register the given base system
     */
    fun <T : BaseSystem> register(kClass: KClass<T>): T

    /**
     * This should create a new entity for us within this scene.
     * The [tage] is required and should be mapped to a Tag component.
     */
    fun create(tag: String): Entity

    /**
     * This allows us to get the system of the given type [T]
     */
    fun <T : BaseSystem> getSystem(klass: KClass<T>): T

    /**
     * This should inject the given class with the world's injection method
     */
    fun inject(any: Any)


    /**
     * This will update the ecs world
     */
    fun process(delta: Float)


}