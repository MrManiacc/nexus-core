package nexus.engine.layer

import nexus.engine.Application
import nexus.engine.events.Events.App.Timestep
import nexus.engine.render.RenderAPI
import nexus.engine.scene.Scene

/**
 * This is a default layer that allows for a scene to be evaluated.
 */
abstract class SceneLayer<API : RenderAPI>(
    app: Application<API>, val scene: Scene,
) : Layer<API>(app, app.renderAPI::class, "nexus/engine/scene") {

    protected open fun preUpdate(delta: Timestep) = Unit
    protected open fun update(delta: Timestep) = Unit
    protected open fun postUpdate(delta: Timestep) = Unit

    /**
     * This allows us to render the scene
     */
    override fun onUpdate(update: Timestep) {
        preUpdate(update)
        update(update)
        scene.process(update.deltaTime)
        postUpdate(update)
    }
}