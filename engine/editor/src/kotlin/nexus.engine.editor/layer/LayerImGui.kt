package nexus.engine.editor.layer

import imgui.*
import imgui.type.*
import nexus.engine.editor.dsl.*
import nexus.engine.editor.wrapper.*
import marx.engine.*
import marx.engine.events.*
import marx.engine.events.Events.App.Timestep
import marx.engine.events.Events.Gui.*
import marx.engine.layer.*
import marx.engine.math.*
import marx.engine.math.MathDSL.Extensions.by

class LayerImGui(app: Application<*>) : Layer<DebugRenderAPI>(app, DebugRenderAPI::class) {

    val transform = Transform(0f by 0f by 0f, 0f by 0f by 0f, 1f by 1f by 1f)

    private val dockspaceName = "core_dockspace"
    private val viewportEvent = ViewportOverlay()
    private val propertiesEvent = PropertiesOverlay()
    private val vsync = ImBoolean(false)

    override fun onAttach() {
        renderAPI.init()
    }

    override fun onUpdate(update: Timestep) = renderAPI.frame { onRenderUi(update, ImGui.getIO()) }

    /*This is called inside the render frame of imgui. It's an overlay so it should be last.*/
    private fun onRenderUi(
        update: Timestep,
        io: ImGuiIO
    ) {
        io.deltaTime = update.deltaTime
        renderAPI.dockspace(dockspaceName, ::renderProperties, ::renderViewport)
    }

    /*This should render the imgui properties windows on the sidebar*/
    private fun renderProperties() {
//        with(MarxUI){
//            label("testing#34324", "Testing")
//        }

        app.publish(propertiesEvent)
        if (MarxGui.transform("testing", transform, 0.1f))
            println("updated transform!")
    }

    /*This should render the imgui properties windows on the sidebar*/
    private fun renderViewport() {
        app.publish(viewportEvent)
    }

    /*This is used to destroy the application upon pressing escape*/
    override fun onEvent(event: Event) {}

}