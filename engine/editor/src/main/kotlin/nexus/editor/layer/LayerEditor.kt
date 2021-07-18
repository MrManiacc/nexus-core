package nexus.editor.layer

import dorkbox.messageBus.annotations.Subscribe
import imgui.ImGui
import nexus.editor.api.Workspace
import nexus.editor.api.core.panels.*
import nexus.editor.api.impl.DockspaceElement
import nexus.engine.Application
import nexus.editor.wrapper.DebugRenderAPI
import nexus.engine.events.Events
import nexus.engine.events.Events.App.Timestep
import nexus.engine.layer.Layer
import java.nio.file.Path

/**
 * This is the main layer for rendering the editor. This is used for handling all of the boilerplate editor
 * render code, like collecting all of the third party panels, labels, actions etc and rendering them here.
 * This is the core for all gui actives within the editor.
 */
class LayerEditor(app: Application<*>) : Layer<DebugRenderAPI>(app, DebugRenderAPI::class) {

    private val workspace: Workspace = DockspaceElement(
        app,
        id = "nexus.engine.dockspace",
    ).apply {
        add(ContentPanel(app, Path.of(".")))
        add(ScenePanel(app))
        add(PropertiesPanel(app))
        add(NodeGraphPanel(app))
        add(ViewportPanel(app))
    }

    override fun onAttach() = renderAPI.init()

    override fun onUpdate(update: Timestep) {
        renderAPI.frame {
//            AssetTypeManager().files.forEach {
//                ImGui.text(it.name)
//            }

            ImGui.getIO().deltaTime = update.deltaTime
            workspace.render()
        }
    }


    /**
     * For some reason imgui isn't recieving scroll input, so we manually pass it here
     */
    @Subscribe fun onScroll(scroll: Events.Input.MouseScroll) {
        ImGui.getIO().mouseWheel = scroll.yOffset
        ImGui.getIO().mouseWheelH = scroll.xOffset
    }

}