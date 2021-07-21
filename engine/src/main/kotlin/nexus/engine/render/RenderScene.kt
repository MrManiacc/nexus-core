package nexus.engine.render

import nexus.engine.camera.Camera
import nexus.engine.camera.CameraController
import nexus.engine.math.Transform

/**
 * This allows us to nexus.engine.render a nexus.engine.scene. It is platform agnostic
 */
interface RenderScene {


    /*Creates a new nexus.engine.scene of the */
    fun sceneOf(
        cameraController: CameraController<*>,
        body: RenderScene.() -> Unit,
    ) {
        beginScene(cameraController.camera)
        body()
        endScene()
    }

    /*This will start a new nexus.engine.scene*/
    fun beginScene(camera: Camera<*>)

    /*This method should be overloaded for all of the various types of things we can submit*/
    fun submit(array: VertexArray)

    /*This method should be overloaded for all of the various types of things we can submit*/
    fun submit(
        array: VertexArray,
        shader: Shader,
        transform: Transform,
    )

    /*This method should be overloaded for all of the various types of things we can submit*/
    fun submit(
        array: VertexArray,
        shaderIn: Shader,
        transformIn: Transform,
        draw: RenderScene.(Shader, Transform) -> Unit = { shader, transform ->
            shader.uploadMat4(
                "u_ModelMatrix",
                transform.matrix
            )
        },
    ) = submit(array, shaderIn, transformIn, draw) { _, _ -> }

    /*This method should be overloaded for all of the various types of things we can submit*/
    fun submit(
        array: VertexArray,
        shaderIn: Shader,
        transformIn: Transform,
        preDraw: RenderScene.(Shader, Transform) -> Unit,
        postDraw: RenderScene.(Shader, Transform) -> Unit = { _, _ -> },
    )

    /*This clears our all of objects or entities on the nexus.engine.scene*/
    fun flush()

    /*Ends the current nexus.engine.scene, renders all of the submitted meshes*/
    fun endScene()


}
