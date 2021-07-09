package marx.sandbox.layer

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiWindowFlags.*
import marx.engine.*
import marx.engine.events.*
import marx.engine.events.Events.App.Timestep
import marx.engine.events.Events.Input.KeyPress
import marx.engine.events.Events.Shader.*
import marx.engine.layer.*
import marx.engine.math.*
import marx.engine.math.MathDSL.Extensions.by
import marx.engine.math.MathDSL.Extensions.via
import marx.engine.render.*
import marx.engine.utils.StringUtils.format
import marx.sandbox.*
import mu.*
import nexus.engine.editor.dsl.MarxGui
import nexus.engine.editor.wrapper.DebugRenderAPI
import nexus.plugins.opengl.GLBuffer
import nexus.plugins.opengl.GLShader
import nexus.plugins.opengl.GLVertexArray
import nexus.plugins.opengl.data.Shaders
import org.joml.*
import org.lwjgl.glfw.GLFW.*
import org.slf4j.*
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi

/*This layer is used for debugging purpose*/
class LayerDebug(app: Application<*>) : Layer<DebugRenderAPI>(app, DebugRenderAPI::class, "debug-layer") {
    private val log: Logger = KotlinLogging.logger { }
    private val rand: Random = Random(69420)
    private val flatShader = GLShader(app)
    private val editorShader = GLShader(app)
    private val transform = Transform(Vec3(0f, 0f, 0f), 0f via 0f via 0f, Vec3(1f))
    private val transformBuffer = Transform(Vec3(0f, 0f, 0f), 0f via 0f via 0f, Vec3(1f))
    private var colorOne = rand.nextFloat() by rand.nextFloat() by rand.nextFloat()
    private var colorTwo = colorOne.z by rand.nextFloat() by rand.nextFloat()
    private var colorThree = rand.nextFloat() by rand.nextFloat() by colorTwo.y
    private var colorFour = rand.nextFloat() by colorOne.x by rand.nextFloat()

    /*This creates a quad of size 0.5*/
    val quadVAO: VertexArray = GLVertexArray().apply {
        this += GLBuffer.GLVertexBuffer(
            floatArrayOf(
                0.5f, 0.5f, 0.0f,  // top right
                0.5f, -0.5f, 0.0f,  // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                -0.5f, 0.5f, 0.0f // top left
            ), 3
        )

        this += GLBuffer.GLIndexBuffer(
            intArrayOf(
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
            )
        )
    }

    /*This creates a quad of size 0.5*/
    val triangleVAO: VertexArray = GLVertexArray().apply {
        this += GLBuffer.GLVertexBuffer(
            floatArrayOf(
                -0.5f, -0.5f, 0.0f,  // bottom left
                0.5f, -0.5f, 0.0f,  // bottom right
                0.0f, 0.5f, 0.0f, // top center
            ), 3
        )

        this += GLBuffer.GLIndexBuffer(
            intArrayOf(
                0, 1, 2,   // first triangle
            )
        )
    }

    /*This is called upon the layer being presented.*/
    override fun onAttach() {
        renderAPI.init()
        quadVAO.create()
        triangleVAO.create()
        if (flatShader.compile(Shaders.flatShader())) log.warn("Successfully compiled simple shader: ${flatShader::class.qualifiedName}")
        if (editorShader.compile(Shaders.simple())) log.warn(
            "Successfully compiled editor shader: ${editorShader::class.qualifiedName}"
        )
    }

    /*This will draw every frame*/
    override fun onUpdate(time: Timestep) {
        updateCamera(time)
        drawScene()
        renderAPI.frame { drawGui(time) }
    }

    /*This updates the camera's position using the [time]*/
    private fun updateCamera(time: Timestep) = Sandbox.editorCamera.let { cam ->
        val moveSpeed = Sandbox.editorCamera.moveSpeed
        val lookSpeed = Sandbox.editorCamera.lookSpeed
        with(app.input) {
            if (isKeyDown(GLFW_KEY_D))
                cam x (moveSpeed * time.deltaTime)
            if (isKeyDown(GLFW_KEY_A))
                cam x (moveSpeed * time.deltaTime) * -1
            if (isKeyDown(GLFW_KEY_Q))
                cam roll (lookSpeed * time.deltaTime) * -1
            if (isKeyDown(GLFW_KEY_E))
                cam roll (lookSpeed * time.deltaTime)
            if (isKeyDown(GLFW_KEY_S))
                cam y (moveSpeed * time.deltaTime) * -1
            if (isKeyDown(GLFW_KEY_W))
                cam y (moveSpeed * time.deltaTime)
            if (isKeyDown(GLFW_KEY_RIGHT))
                transform x (time.deltaTime)
            if (isKeyDown(GLFW_KEY_LEFT))
                transform x (time.deltaTime) * -1
            if (isKeyDown(GLFW_KEY_UP))
                transform y (time.deltaTime)
            if (isKeyDown(GLFW_KEY_DOWN))
                transform y (time.deltaTime) * -1
        }
    }

    /*Draws our debug test scene*/
    private fun drawScene() {

        scene.sceneOf(Sandbox.editorCamera) {
            //TODO remove this it's EXPENSIVE!!!
            for (y in -10..10) {
                for (x in -10..10) {
                    submit(quadVAO, flatShader, transformBuffer, { shader, transform ->
                        shader.uploadMat4(
                            "u_ModelMatrix", transform.matrix
                                .identity()
                                .translate(x * 0.11f by y * 0.11f by 0)
                                .scale(0.10f)
                        )
                        if (x % 2 == 0 && y % 2 != 0) shader.uploadVec3("u_Color", colorOne)
                        else if (x % 2 == 0 && y % 2 == 0) shader.uploadVec3("u_Color", colorTwo)
                        else if (x % 2 != 0 && y % 2 == 0) shader.uploadVec3("u_Color", colorThree)
                        else shader.uploadVec3("u_Color", colorFour)
                    })
                }
            }

            submit(triangleVAO, editorShader, transform)
        }
    }

    /*This is called inside the render frame of imgui. It's an overlay so it should be last.*/
    private fun drawGui(update: Timestep) {
        val winPos = app.window.pos
        val size = app.window.size
        val xInset = 220f
        var pos = ImVec2()
        var scale = ImVec2()
        val statesWidth = 200f
        ImGui.setNextWindowSize(statesWidth, 0f, ImGuiCond.Always)
        ImGui.setNextWindowPos(winPos.first + size.first - xInset, winPos.second + 20f, ImGuiCond.Once)
        if (ImGui.begin("metrics", NoResize or NoScrollbar or NoScrollWithMouse or NoCollapse or NoDocking)) {
            ImGui.text("delta: " + update.deltaTime.format(5))
            ImGui.text("time: ${update.gameTime.format(3)}")
            ImGui.text("ms: " + update.milliseconds.format(3))
            ImGui.text("fps: ${ImGui.getIO().framerate.format(1)}")
            pos = ImGui.getWindowPos()
            scale = ImGui.getWindowSize()
        }
        ImGui.end()
        ImGui.setNextWindowSize(statesWidth, 0f, ImGuiCond.Always)
        ImGui.setNextWindowPos(pos.x, pos.y + scale.y + 10, ImGuiCond.Always)
        if (ImGui.begin("states", NoResize or NoScrollbar or NoScrollWithMouse or NoCollapse or NoDocking)) {
            ImGui.text("fullscreen[f1]: ${app.window.fullscreen}")
            ImGui.text("vsync [f3]: ${app.window.vsync}")
            val frame = app.window.size
            ImGui.text("window size: ${frame.first}, ${frame.second}")
            pos = ImGui.getWindowPos()
            scale = ImGui.getWindowSize()
        }
        ImGui.end()
        ImGui.setNextWindowSize(statesWidth, 0f, ImGuiCond.Always)
        ImGui.setNextWindowPos(pos.x, pos.y + scale.y + 10, ImGuiCond.Always)
        if (ImGui.begin("transforms", NoResize or NoScrollbar or NoScrollWithMouse or NoCollapse or NoDocking)) {
            if (MarxGui.transform("transform1", transform)) {
                log.warn("Updated transform1")
            }
            pos = ImGui.getWindowPos()
            scale = ImGui.getWindowSize()
        }
        ImGui.end()

        ImGui.setNextWindowSize(statesWidth, 0f, ImGuiCond.Always)
        ImGui.setNextWindowPos(pos.x, pos.y + scale.y + 10, ImGuiCond.Always)
        if (ImGui.begin("camera", NoResize or NoScrollbar or NoScrollWithMouse or NoCollapse or NoDocking)) {
            if (MarxGui.camera("EditorCamera", Sandbox.editorCamera)) {
                log.warn("Updated camera")
            }
        }
        ImGui.end()




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
                colorOne = rand.nextFloat() by rand.nextFloat() by rand.nextFloat()
                colorTwo = colorOne.z by rand.nextFloat() by rand.nextFloat()
                colorThree = rand.nextFloat() by rand.nextFloat() by colorTwo.y
                colorFour = rand.nextFloat() by colorOne.x by rand.nextFloat()
                log.info("Reloaded shader: $flatShader")
            }
        }
    }

    override fun onDetach() {
        editorShader.destroy()
        flatShader.destroy()
        quadVAO.dispose()
        triangleVAO.dispose()
    }


}