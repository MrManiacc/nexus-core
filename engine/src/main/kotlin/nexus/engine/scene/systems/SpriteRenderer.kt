package nexus.engine.scene.systems

import com.artemis.Aspect
import com.artemis.Entity
import com.artemis.systems.EntityProcessingSystem
import nexus.engine.scene.components.CSpriteRenderer
import nexus.engine.scene.components.CTransform

/**
 * This allows us to iterate over the
 */
class SpriteRenderer : EntityProcessingSystem(
    Aspect.all(CTransform::class.java, CSpriteRenderer::class.java)
) {

    /**
     * Process a entity this system is interested in.
     * @param e
     * the entity to process
     */
    override fun process(e: Entity) {

    }
}