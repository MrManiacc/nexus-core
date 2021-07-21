package nexus.engine.scene.components

import com.artemis.Component
import com.artemis.annotations.DelayedComponentRemoval
import java.util.*

/**
 * This should allow for easy tracking of entities within a given scene
 */
@DelayedComponentRemoval
data class CTag(val tag: String) : Component() {
    constructor() : this("$EmptyTagText##${UUID.randomUUID()}")

    companion object {
        const val EmptyTagText = "Empty"
    }

}