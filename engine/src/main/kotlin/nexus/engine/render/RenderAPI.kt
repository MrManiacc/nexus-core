package nexus.engine.render

import nexus.engine.camera.Camera
import nexus.engine.math.Transform
import nexus.engine.render.framebuffer.Framebuffer
import nexus.engine.render.framebuffer.FramebufferSpecification

/**
 * This is the core of all of the rendering done throughout the engine.
 * It's purpose is to provide a platform agnostic rendering subset of tools.
 */
abstract class RenderAPI(
    val command: RenderCommand,
    val scene: RenderScene
) {

    init {
        this.register()
    }

    abstract fun init()

    /*NumberDraws the given vertex array instanced, meaning we can nexus.engine.render many of these statically.*/
    abstract fun drawIndexed(array: VertexArray)

    /**
     * This should create/return a frame buffer for the givne specification.
     */
    abstract fun framebuffer(specification: FramebufferSpecification): Framebuffer


    /*This will register the api with the [Renderer]*/
    open fun register() =
        Renderer.set(this::class, this)


    /*Called upon unloading of the given renderAPI **/
    open fun dispose() {}

    companion object {
        /*Provides and a "null-safe" api. This will be the defaulted to renderApi **/
        val Null: RenderAPI = object : RenderAPI(object : RenderCommand {
            override fun blending(enabled: Boolean) = Unit
        }, object : RenderScene {
            val renderAPI: RenderAPI
                get() = Renderer()
            var camera: Camera<*> = Camera.Null()

            /*This will start a new nexus.engine.scene*/
            override fun beginScene(camera: Camera<*>) = Unit
            override fun submit(array: VertexArray) = Unit

            /*This method should be overloaded for all of the various types of things we can submit*/
            override fun submit(
                array: VertexArray,
                shader: Shader,
                transform: Transform
            ) {

            }

            /*This wil send some arrbitrary data off to the */
            override fun submit(
                array: VertexArray,
                shaderIn: Shader,
                transformIn: Transform,
                preDraw: RenderScene.(Shader, Transform) -> Unit,
                postDraw: RenderScene.(Shader, Transform) -> Unit
            ) {
                TODO("Not yet implemented")
            }

            /*This method should be overloaded for all of the various types of things we flush*/
            override fun flush() = Unit
            override fun endScene() = Unit
        }) {
            override fun init() = Unit
            override fun register() = Unit
            override fun drawIndexed(array: VertexArray) = Unit

            /**
             * This should create/return a frame buffer for the givne specification.
             */
            override fun framebuffer(specification: FramebufferSpecification): Framebuffer {
                TODO("Not yet implemented")
            }
        }
    }

}