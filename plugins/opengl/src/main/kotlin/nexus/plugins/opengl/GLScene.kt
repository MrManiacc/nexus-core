package nexus.plugins.opengl

import nexus.engine.camera.Camera
import nexus.engine.math.Transform
import nexus.engine.render.*
import kotlin.reflect.KClass

/**
 * This controls an editor's nexus.engine.scene.
 */
class GLScene<API : RenderAPI>(
    private val apiType: KClass<API>,
) : RenderScene {
    val renderAPI: RenderAPI get() = Renderer(apiType)
    private lateinit var camera: Camera<*>

    /*This will start a new nexus.engine.scene*/
    override fun beginScene(camera: Camera<*>) {
        this.camera = camera
    }

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
        postDraw: RenderScene.(Shader, Transform) -> Unit,
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


    /*NumberEnds the current nexus.engine.scene, renders all of the submitted meshes*/
    override fun endScene() {
        //TODO: nexus.engine.render the nexus.engine.scene here using a nexus.engine.render-queue/nexus.engine.render-command system.
    }

    /*This clears our all of objects or entities on the nexus.engine.scene*/
    override fun flush() = Unit

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
