package nexus.editor.gui.assets

import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImGui
import nexus.editor.gui.Element
import nexus.editor.gui.internal.ID

interface Font : Element {
    var font: ImFont?


    val loaded: Boolean get() = font != null

    /**
     * We do our loaded upon being added
     */
    override fun addedTo(parent: Element) {
        val io = ImGui.getIO()
        val fonts = io.fonts
        val fontConfig = ImFontConfig()

        fontConfig.glyphRanges = fonts.glyphRangesDefault
        fontConfig.pixelSnapH = true
        font =
            fonts.addFontFromFileTTF("${this.nameId.folderPath}/${this.nameId.fileName}.${this.nameId.fileExtension}",
                32f,
                fontConfig)
    }

    fun push() {
        if (font != null)
            ImGui.pushFont(font)
    }

    fun pop() {
        if (font != null)
            ImGui.popFont()
    }

    companion  object {
        operator fun invoke(path: String): Font = FontInternal(path)
    }

    class FontInternal internal constructor(filePath: String) : Font {
        override var font: ImFont? = null

        /**
         * This is used as a name and id
         */
        override val nameId: ID = ID(filePath)

        /**
         * This method actually is used to render the nodes
         */
        override fun render() {
//            TODO("Not yet implemented")
            //DO nothing when rendeirngl
        }

        /**
         * This should be created from the enum values for the flags
         */
        override val flags: Int = 0

    }

}