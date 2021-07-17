package nexus.editor.gui

import imgui.ImColor
import imgui.ImDrawList
import imgui.ImGui
import nexus.editor.gui.assets.Icon

/**
 * This interface wraps around many of imgui core drawing interfaces and provides easy to use methodsl
 */
interface Drawable : Transformable {
    val drawList: ImDrawList
        get() = ImGui.getWindowDrawList()

    /**
     * This will draw a cursor of the given size
     */
    fun rectFilled(x: Float, y: Float, width: Float, height: Float, color: String) =
        drawList.addRectFilled(x, y, x + width, y + height, ImColor.rgbToColor(color))

    /**
     * This will draw an outline at the cursor with the proper offset
     */
    fun rectFilledCursor(xOff: Float = 0f, yOff: Float = 0f, width: Float, height: Float, color: String) =
        rectFilled(cursorX + xOff, cursorY + yOff, width, height, color)

    /**
     * This will draw a cursor of the given size
     */
    fun rectOutline(x: Float, y: Float, width: Float, height: Float, color: String) =
        drawList.addRect(x, y, x + width, y + height, ImColor.rgbToColor(color))


    /**
     * This will draw an outline at the cursor with the proper offset
     */
    fun rectOutlineCursor(xOff: Float = 0f, yOff: Float = 0f, width: Float, height: Float, color: String) =
        rectOutline(cursorX + xOff, cursorY + yOff, width, height, color)

    /**
     * This is used to render an icon the screen.
     */
    fun icon(x: Float, y: Float, width: Float, height: Float, icon: Icon) {
        drawList.addImage(icon.renderId, x, y, x + width, x + height)
    }

    fun iconCursor(x: Float, y: Float, width: Float, height: Float, icon: Icon) {
        icon(cursorX + x, cursorY + y, width, height, icon)
    }



}