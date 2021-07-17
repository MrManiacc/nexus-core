package marx.sandbox.layer

import marx.sandbox.Sandbox
import mu.KotlinLogging
import nexus.engine.Application
import nexus.engine.editor.wrapper.DebugRenderAPI
import nexus.engine.events.Event
import nexus.engine.events.Events.App.Timestep
import nexus.engine.events.Events.Input.KeyPress
import nexus.engine.events.Events.Shader.Compiled
import nexus.engine.layer.Layer
import nexus.engine.math.MathDSL.Extensions.by
import nexus.engine.math.MathDSL.Extensions.via
import nexus.engine.math.Transform
import nexus.engine.math.Vec3
import nexus.engine.assets.texture.TextureData
import nexus.engine.assets.texture.TextureInstance
import nexus.engine.assets.texture.TextureInstanceData
import nexus.plugins.opengl.GLShader
import nexus.plugins.opengl.GLTexture2D
import nexus.plugins.opengl.data.Primitives
import nexus.plugins.opengl.data.Shaders
import org.joml.Random
import org.lwjgl.glfw.GLFW.GLFW_KEY_R
import org.lwjgl.opengl.GL11.*
import org.slf4j.Logger
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi

/*This nexus.engine.layer is used for debugging purpose*/
class LayerDebug(app: Application<*>) : Layer<DebugRenderAPI>(app, DebugRenderAPI::class, "debug-nexus.engine.layer") {
    private val log: Logger = KotlinLogging.logger { }
    private val rand: Random = Random(69420)
    private val flatShader = GLShader(app)
    private val editorShader = GLShader(app)
    private val textureShader = GLShader(app)
    private val transform = Transform(Vec3(0f, 0f, 0f), 0f via 0f via 0f, Vec3(1f))
    private val transformBuffer = Transform(Vec3(0f, 0f, 0f), 0f via 0f via 0f, Vec3(1f))

    //=====================Texture testing==========================
    private var texture = GLTexture2D().initialize(TextureData("checkerboard.png"))
    private lateinit var textureInstance: TextureInstance
    private lateinit var textureInstanceLinear: TextureInstance
    //==============================================================

    /*This is called upon the nexus.engine.layer being presented.*/
    override fun onAttach() {
        renderAPI.init()
        Primitives.QuadVAO.create()
        Primitives.TriangleVAO.create()
        if (flatShader.compile(Shaders.flatShader())) log.warn("Successfully compiled simple shader: ${flatShader::class.qualifiedName}")
        if (editorShader.compile(Shaders.simple())) log.warn(
            "Successfully compiled editor shader: ${editorShader::class.qualifiedName}"
        )
        if (textureShader.compile(Shaders.textureShader())) log.warn(
            "Successfully compiled texture shader: ${editorShader::class.qualifiedName}"
        )
        textureInstance = texture.instantiate(
            TextureInstanceData(
                0,
                GL_TEXTURE_MIN_FILTER to GL_LINEAR,
                GL_TEXTURE_MAG_FILTER to GL_NEAREST
            )
        ) as TextureInstance



    }

    /*Draws our debug test nexus.engine.scene*/
    private fun drawScene() {
        scene.sceneOf(Sandbox.controller) {
            textureInstance.bind()
            submit(Primitives.QuadVAO, textureShader, transformBuffer) { shader, transform ->
                shader.uploadTexture("u_Texture", textureInstance)
                shader.uploadMat4(
                    "u_ModelMatrix", transform.matrix
                        .identity()
                        .translate(0f by 0f by 0f)
                        .scale(1f)
                )
            }
        }
    }

    /*This will draw every frame*/
    override fun onUpdate(time: Timestep) {
        drawScene()
        renderAPI.frame { drawGui(time) }
    }


    /*This is called inside the nexus.engine.render frame of imgui. It's an overlay so it should be last.*/
    private fun drawGui(update: Timestep) {
//        val winPos = app.window.pos
//        val size = app.window.size
//        val xInset = 220f
//        var pos = ImVec2()
//        var scale = ImVec2()
//        val statesWidth = 200f
//
//        ImGui.setNextWindowSize(statesWidth, 0f, ImGuiCond.Always)
//        ImGui.setNextWindowPos(winPos.first + size.first - xInset, winPos.second + 20f, ImGuiCond.Once)
//        if (ImGui.begin("metrics", NoResize or NoScrollbar or NoScrollWithMouse or NoCollapse or NoDocking)) {
//            ImGui.text("delta: " + update.deltaTime.format(5))
//            ImGui.text("time: ${update.gameTime.format(3)}")
//            ImGui.text("ms: " + update.milliseconds.format(3))
//            ImGui.text("fps: ${ImGui.getIO().framerate.format(1)}")
//            pos = ImGui.getWindowPos()
//            scale = ImGui.getWindowSize()
//        }
//        ImGui.end()
//        ImGui.setNextWindowSize(statesWidth, 0f, ImGuiCond.Always)
//        ImGui.setNextWindowPos(pos.x, pos.y + scale.y + 10, ImGuiCond.Always)
//        if (ImGui.begin("states", NoResize or NoScrollbar or NoScrollWithMouse or NoCollapse or NoDocking)) {
//            ImGui.text("fullscreen[f1]: ${app.window.fullscreen}")
//            ImGui.text("vsync [f3]: ${app.window.vsync}")
//            val frame = app.window.size
//            ImGui.text("window size: ${frame.first}, ${frame.second}")
//            pos = ImGui.getWindowPos()
//            scale = ImGui.getWindowSize()
//        }
//        ImGui.end()
//        ImGui.setNextWindowSize(statesWidth, 0f, ImGuiCond.Always)
//        ImGui.setNextWindowPos(pos.x, pos.y + scale.y + 10, ImGuiCond.Always)
//        if (ImGui.begin("transforms", NoResize or NoScrollbar or NoScrollWithMouse or NoCollapse or NoDocking)) {
//            if (MarxGui.transform("transform1", transform)) {
//                log.warn("Updated transform1")
//            }
//            pos = ImGui.getWindowPos()
//            scale = ImGui.getWindowSize()
//        }
//        ImGui.end()
//
//        ImGui.setNextWindowSize(statesWidth, 0f, ImGuiCond.Always)
//        ImGui.setNextWindowPos(pos.x, pos.y + scale.y + 10, ImGuiCond.Always)
//        if (ImGui.begin(
//                "nexus/engine/camera",
//                NoResize or NoScrollbar or NoScrollWithMouse or NoCollapse or NoDocking
//            )
//        ) {
////            if (MarxGui.camera("EditorCamera", Sandbox.editorCamera)) {
////                log.warn("Updated nexus.engine.camera")
////            }
//        }
//        ImGui.end()


    }

    override fun onEvent(event: Event) {
        if (event is Compiled)
            if (event.result.isValid)
                log.info("Successfully compiled '${event.result.type.name}' shader: ${event.result.message}")
            else
                log.error("Failed to compile '${event.result.type.name}' shader: ${event.result.message}")
        else if (event is KeyPress) {
            if (event.key == GLFW_KEY_R) { //Reload the shader
                editorShader.destroy()
                editorShader.compile(Shaders.simple())
                flatShader.compile(Shaders.flatShader())
                log.info("Reloaded shader: $flatShader")
            }
        }
    }

    override fun onDetach() {
        editorShader.destroy()
        flatShader.destroy()
        Primitives.QuadVAO.dispose()
        Primitives.TriangleVAO.dispose()
    }


}