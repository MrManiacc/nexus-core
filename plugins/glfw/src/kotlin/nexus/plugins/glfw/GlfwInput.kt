package nexus.plugins.glfw

import nexus.engine.input.IInput
import nexus.engine.glfw.IWindow
import org.lwjgl.glfw.GLFW

class GlfwInput(override val window: IWindow) : IInput {
    private val xBuffer = DoubleArray(1)
    private val yBuffer = DoubleArray(1)

    override val mouseX: Float
        get() {
            val state = GLFW.glfwGetCursorPos(window.handle, xBuffer, yBuffer)
            return xBuffer[0].toFloat()
        }

    override val mouseY: Float
        get() {
            val state = GLFW.glfwGetCursorPos(window.handle, xBuffer, yBuffer)
            return yBuffer[0].toFloat()
        }

    override fun isKeyDown(keyCode: Int): Boolean {
        val state = GLFW.glfwGetKey(window.handle, keyCode)
        return state == GLFW.GLFW_PRESS || state == GLFW.GLFW_REPEAT
    }

    override fun isMouseDown(mouseButton: Int): Boolean {
        val state = GLFW.glfwGetMouseButton(window.handle, mouseButton)
        return state == GLFW.GLFW_PRESS || state == GLFW.GLFW_REPEAT
    }



}