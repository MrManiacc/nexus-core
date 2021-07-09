package marx.engine.input

import marx.engine.nexus.plugins.glfw.IWindow

interface IInput {
    val window: IWindow
    val mouseX: Float
    val mouseY: Float
    val mousePos: Pair<Float, Float> get() = mouseX to mouseY
    fun isKeyDown(keyCode: Int): Boolean
    fun isMouseDown(mouseButton: Int): Boolean
}