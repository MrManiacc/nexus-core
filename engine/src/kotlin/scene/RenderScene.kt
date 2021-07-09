package marx.engine.scene

import marx.engine.math.*
import marx.engine.render.*
import marx.engine.camera.*

/**
 * This allows us to render a scene. It is platform agnostic
 */
interface RenderScene {
    val renderAPI: RenderAPI
    var camera: Camera<*>

    /*Creates a new scene of the */
    fun sceneOf(
        camera: Camera<*>,
        body: RenderScene.() -> Unit
    ) {
        this.camera = camera
        beginScene(camera)
        body()
        endScene()
    }

    /*This will start a new scene*/
    fun beginScene(camera: Camera<*>)

    /*This method should be overloaded for all of the various types of things we can submit*/
    fun submit(array: VertexArray)

    /*This method should be overloaded for all of the various types of things we can submit*/
    fun submit(
        array: VertexArray,
        shader: Shader,
        transform: Transform
    )

    /*This method should be overloaded for all of the various types of things we can submit*/
    fun submit(
        array: VertexArray,
        shaderIn: Shader,
        transformIn: Transform,
        preDraw: RenderScene.(Shader, Transform) -> Unit = { shader, transform ->
            shader.uploadMat4(
                "u_ModelMatrix",
                transform.matrix
            )
        },
        postDraw: RenderScene.(Shader, Transform) -> Unit = { _, _ -> }
    )

    /*This clears our all of objects or entities on the scene*/
    fun flush()

    /*Ends the current scene, renders all of the submitted meshes*/
    fun endScene()


}
