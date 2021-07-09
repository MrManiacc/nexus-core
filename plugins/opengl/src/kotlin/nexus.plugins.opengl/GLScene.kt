package nexus.plugins.opengl

import marx.engine.math.*
import marx.engine.render.*
import marx.engine.camera.*
import marx.engine.scene.*
import kotlin.reflect.*

/**
 * This controls an editor's scene.
 */
class GLScene<API : RenderAPI>(
    private val apiType: KClass<API>
) : RenderScene {
    override val renderAPI: RenderAPI get() = Renderer(apiType)
    override lateinit var camera: Camera<*>

    /*This will start a new scene*/
    override fun beginScene(camera: Camera<*>) = Unit

    /*This method should be overloaded for all of the various types of things we can submit*/
    override fun submit(array: VertexArray) {
        array.bind()
        renderAPI.drawIndexed(array)
        array.unbind()
    }

    /*This method should be overloaded for all of the various types of things we can submit*/
    override fun submit(
        array: VertexArray,
        shader: Shader,
        transform: Transform,
    ) {
        shader.bind()
        array.bind()
        shader.uploadMat4("u_ViewProjection", camera.viewProjection)
        shader.uploadMat4("u_ModelMatrix", transform.matrix)
        renderAPI.drawIndexed(array)
        array.unbind()
        shader.unbind()
    }

    /*This method should be overloaded for all of the various types of things we can submit*/
    override fun submit(
        array: VertexArray,
        shaderIn: Shader,
        transformIn: Transform,
        preDraw: RenderScene.(Shader, Transform) -> Unit,
        postDraw: RenderScene.(Shader, Transform) -> Unit
    ) {
        shaderIn.bind()
        array.bind()
        shaderIn.uploadMat4("u_ViewProjection", camera.viewProjection)
        preDraw(shaderIn, transformIn)
        renderAPI.drawIndexed(array)
        postDraw(shaderIn, transformIn)
        array.unbind()
        shaderIn.unbind()
    }

    /*This clears our all of objects or entities on the scene*/
    override fun flush() = Unit

    /*NumberEnds the current scene, renders all of the submitted meshes*/
    override fun endScene() {
        //TODO: render the scene here using a render-queue/render-command system.
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GLScene<*>) return false

        if (apiType != other.apiType) return false

        return true
    }

    override fun hashCode(): Int {
        return apiType.hashCode()
    }


}
