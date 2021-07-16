package nexus.engine.editor.layer

import imgui.ImGui
import nexus.editor.gui.Workspace
import nexus.editor.gui.core.panels.AssetBrowserPanel
import nexus.editor.gui.core.panels.PropertiesPanel
import nexus.editor.gui.core.panels.ScenePanel
import nexus.editor.gui.core.panels.ViewportPanel
import nexus.editor.gui.impl.DockspaceNode
import nexus.engine.Application
import nexus.engine.editor.wrapper.DebugRenderAPI
import nexus.engine.events.Events.App.Timestep
import nexus.engine.layer.Layer

/**
 * This is the main layer for rendering the editor. This is used for handling all of the boilerplate editor
 * render code, like collecting all of the third party panels, labels, actions etc and rendering them here.
 * This is the core for all gui actives within the editor.
 */
class LayerEditor(app: Application<*>) : Layer<DebugRenderAPI>(app, DebugRenderAPI::class) {
    private val workspace: Workspace = DockspaceNode(
        id = "nexus.engine.dockspace",
    ).apply {
        add(AssetBrowserPanel(app))
        add(ScenePanel(app))
        add(PropertiesPanel(app))
        add(ViewportPanel(app))
    }

    override fun onAttach() = renderAPI.init()

    override fun onUpdate(update: Timestep) {
        renderAPI.frame {
            ImGui.getIO().deltaTime = update.deltaTime
            workspace.render()
        }
    }
}