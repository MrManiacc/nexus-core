package nexus.engine.scene.internal

import com.artemis.*
import com.artemis.utils.Bag
import mu.KotlinLogging
import nexus.engine.scene.Scene
import nexus.engine.utils.NamedDelegate
import nexus.engine.utils.PropertyDelegate
import nexus.engine.utils.PropertyName
import nexus.engine.utils.WorldExtender
import org.slf4j.Logger
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * This is a scene that exposes many of the artemis internal mechanics via reflections
 */
class ArtemisScene(configuration: WorldConfiguration = WorldConfiguration()) : World(configuration), WorldExtender,
    Scene {
    private val logger: Logger = KotlinLogging.logger { }

    /**
     * This should be used to expose the bag of systems using [NamedDelegate]
     */
    @PropertyName("systemsBag")
    override val systems: Bag<BaseSystem> by PropertyDelegate

    /**
     * This should get the asm from the class using [NamedDelegate]
     */
    @PropertyName("asm")
    override val subsManagager: AspectSubscriptionManager by PropertyDelegate

    /**
     * This is used to configure our invocations
     */
    @PropertyName("invocationStrategy")
    override val invokeStragegy: SystemInvocationStrategy by PropertyDelegate

    /**
     * This will hopefully create an register the given base system
     */
    override fun <T : BaseSystem> register(kClass: KClass<T>): T {
        val instance = kClass.createInstance()
        systems.add(instance)
        logger.info("registered system instance: ${kClass.simpleName}")
        return instance
    }

    /**
     * TODO: custom injection here
     */
    override fun inject(target: Any) {
        super.inject(target)
    }

    /**
     * This should create a new entity for us within this scene.
     * The [tage] is required and should be mapped to a Tag component.
     */
    override fun create(tag: String): Entity {
        return createEntity()
    }

    /**
     * This allows us to get the system of the given type [T]
     */
    override fun <T : BaseSystem> getSystem(klass: KClass<T>): T {
        return getSystem(klass.java)
    }

    /**
     * This routes up to the base level scene to allow for accessing of the scene with inline reified stuff
     */
    inline fun <reified T : BaseSystem> Scene.getSystem(): T = getSystem(T::class)

    /**
     * This will update the ecs world
     */
    override fun process(delta: Float) {
        super.delta = delta
        super.process()
    }

}