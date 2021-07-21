package nexus.engine.scene.managers

import com.artemis.Aspect
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.Entity
import nexus.engine.Application.Companion.app
import nexus.engine.events.Events
import nexus.engine.scene.components.CTag

/**
 * This allwow tracking of entites withing a given system
 */
class TagManager : BaseEntitySystem(Aspect.one(CTag::class.java)) {
    private val trackedTags: MutableMap<Int, CTag> = HashMap()
    private val trackedEntities: MutableMap<CTag, Int> = HashMap()
    private lateinit var tagMap: ComponentMapper<CTag>

    /**
     * <p>Called if entity has gone out of scope of this system, e.g deleted
     * or had one of it's components removed.</p>
     * <p>
     * Important note on accessing components:
     * Using {@link ComponentMapper#get(int)} to retrieve a component is unsafe, unless:
     * - You annotate the component with {@link DelayedComponentRemoval}.
     * - {@link World#isAlwaysDelayComponentRemoval} is enabled to make accessing all components safe,
     * for a small performance hit.
     * <p>
     * {@link ComponentMapper#has(int)} always returns {@code false}, even for DelayedComponentRemoval components.
     *
     * Can trigger for entities that have been destroyed immediately after being created (within a system).
     *
     * @param entityId the entity that was removed from this system
     */
    override fun removed(entityId: Int) {
        val tag = tagMap[entityId]
        if (trackedEntities.containsKey(tag) || trackedTags.containsKey(entityId)) {
            trackedEntities.remove(tag)
            trackedTags.remove(entityId)
            app.publish(Events.Entity.TaggedEntityRemoved(entityId, tag))
        }
    }

    /**
     * Called if entity has come into scope for this system, e.g created or a component was added to it.
     *
     * Triggers right after any system finishes processing. Adding and immediately removing a component
     * does not count as a permanently change and will prevent this method from being called.
     *
     * Not triggered for entities that have been destroyed immediately after being created (within a system).
     *
     * @param entityId the entity that was added to this system
     */
    override fun inserted(entityId: Int) {
        val tag = tagMap[entityId]
        trackedEntities[tag] = entityId
        trackedTags[entityId] = tag
        app.publish(Events.Entity.TaggedEntityAdded(entityId, tag))
    }

    /**
     * This attempts to get the entity from the given tag. if not found it will return [-1]
     */
    fun entityOf(tag: CTag): Int = trackedEntities.getOrDefault(tag, -1)

    /**
     * This attempts to get the entity from the given tag. if not found it will return [-1]
     */
    fun entityOf(tag: String): Int = entityOf(CTag(tag))

    /**
     * This will return null if not found
     */
    fun tagOf(entityId: Int): CTag? = trackedTags[entityId]


    /**
     * This will return null if not found
     */
    fun tagOf(entityId: Entity): CTag? = tagOf(entityId.id)


    override fun checkProcessing(): Boolean = false

    /**
     * Process the system.
     */
    override fun processSystem() = Unit
}