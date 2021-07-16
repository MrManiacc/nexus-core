package nexus.engine.editor.render

//import imgui.ImGui
//import imgui.ImVec2
//import nexus.editor.gui.BaseNode
//import nexus.editor.gui.impl.BaseNode
//
///**
// * This is a renderable element. It should render it's
// */
//open class Panel(name: String, descriptorBlock: descriptorBlock) : BaseNode(name, descriptorBlock) {
//     val viewportSize: ImVec2 get() = ImGui.getContentRegionAvail()
//
//    /**
//     * This is the internal method that will do the actual rendering
//     */
//    override fun process() {
//        if (ImGui.begin(name)) {
//            super.process()
//        }
//        ImGui.end()
//    }
//
//}