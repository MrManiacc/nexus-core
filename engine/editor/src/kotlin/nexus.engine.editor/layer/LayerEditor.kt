package nexus.engine.editor.layer

import imgui.ImGui
import imgui.ImVec2
import nexus.engine.Application
import nexus.engine.editor.render.Panel
import nexus.engine.editor.wrapper.DebugRenderAPI
import nexus.engine.events.Events
import nexus.engine.events.Events.App.Timestep
import nexus.engine.layer.Layer
import nexus.engine.render.framebuffer.FramebufferFormat

/**
 * This is the main layer for rendering the editor. This is used for handling all of the boilerplate editor
 * render code, like collecting all of the third party panels, labels, actions etc and rendering them here.
 * This is the core for all gui actives within the editor.
 */
class LayerEditor(app: Application<*>) : Layer<DebugRenderAPI>(app, DebugRenderAPI::class) {
    private val dockspaceName = "core_dockspace"

    private var viewportSize: ImVec2 = ImVec2(1280f, 720f)



    private val viewport: Panel = Panel("Editor##$dockspaceName") {
        render {
            val size = ImGui.getContentRegionAvail()
            if (size.x != viewportSize.x || size.y != viewportSize.y) {
                viewportSize = size
                app.publish(Events.Camera.Resize(size.x.toInt(), size.y.toInt()))
            }
            app.viewport[FramebufferFormat.Attachment.ColorImage]?.renderID?.let {

//               ImGui.getWindowDrawList().addImage(it, ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY())

                ImGui.image(
                    it,
                    viewportSize.x,
                    viewportSize.y
                )
            }
        }


    }




    override fun onAttach() = renderAPI.init()

    override fun onUpdate(update: Timestep) {
        renderAPI.frame {
            ImGui.getIO().deltaTime = update.deltaTime
            renderAPI.dockspace(dockspaceName, viewport)
        }
    }
}