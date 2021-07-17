package nexus.engine.assets

import mu.KotlinLogging
import java.lang.reflect.Modifier
import javax.annotation.Nullable


/**
 * DisposalHook holds the action to occur when an asset is disposed. This is handled outside of the asset class itself to allow disposal to occur after the asset has been
 * garbage collected via a disposed reference queue mechanism in AssetType.
 */
class DisposalHook {
    @Volatile private var resource: DisposableResource? = null

    /**
     * This disposes of our assets's resources
     */
    @Synchronized fun dispose() {
        resource?.close()
        resource = null
    }

    /**
     * This updates the disposable
     */
    fun setDisposableResource(@Nullable resource: DisposableResource?) {
        if (resource != null) {
            val actionType: Class<out DisposableResource?> = resource.javaClass
            if ((actionType.isLocalClass || actionType.isAnonymousClass || actionType.isMemberClass)
                && !Modifier.isStatic(actionType.modifiers)
            ) {
                logger.warn("Non-static anonymous or member class should not be registered as the disposable resource - this will block garbage collection enqueuing for disposal")
            }
        }
        this.resource = resource
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
