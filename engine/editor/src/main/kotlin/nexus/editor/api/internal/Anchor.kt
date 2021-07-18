package nexus.editor.api.internal

import imgui.flag.ImGuiDir

/**
 * This is used to define the starting anchor. This will decide where to keep
 * the ui element or at the very least where to initialize it on the screen
 */
enum class Anchor(val value: Int) {
    None(ImGuiDir.None),
    Left(ImGuiDir.Left),
    Right(ImGuiDir.Right),
    Up(ImGuiDir.Up),
    Down(ImGuiDir.Down),

    //Custom value, simply means we should render in the center dockspace panel
    Center(-1),
}