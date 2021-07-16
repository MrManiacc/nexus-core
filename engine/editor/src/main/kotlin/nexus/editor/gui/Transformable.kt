package nexus.editor.gui

import imgui.ImGui

/**
 * This can be used to extend the width/height and various other size functions from imgui nodes
 */
interface Transformable : Node {

    val cursorX: Float get() = ImGui.getCursorPosX()
    val cursorY: Float get() = ImGui.getCursorPosY()

    fun width(): Float = ImGui.getContentRegionAvailX()
    fun height(): Float = ImGui.getContentRegionAvailY()

    fun minX(): Float = ImGui.getWindowContentRegionMinX()
    fun minY(): Float = ImGui.getWindowContentRegionMinY()

    fun columnWidth(column: Int = -1): Float =
        if (column == -1) ImGui.getColumnWidth() else ImGui.getColumnWidth(column)


    fun columnOffset(column: Int = -1): Float =
        if (column == -1) ImGui.getColumnOffset() else ImGui.getColumnOffset(column)


    fun columnCount(): Int = ImGui.getColumnsCount()

    fun columnIndex(): Int = ImGui.getColumnIndex()
}