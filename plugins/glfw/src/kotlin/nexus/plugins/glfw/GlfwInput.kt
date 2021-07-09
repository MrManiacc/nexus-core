package nexus.plugins.glfw

import marx.engine.input.*
import marx.engine.nexus.plugins.glfw.IWindow
import org.lwjgl.glfw.GLFW.*

class GlfwInput(override val window: IWindow) : IInput {
    private val xBuffer = DoubleArray(1)
    private val yBuffer = DoubleArray(1)

    override val mouseX: Float
        get() {
            val state = glfwGetCursorPos(window.handle, xBuffer, yBuffer)
            return xBuffer[0].toFloat()
        }

    override val mouseY: Float
        get() {
            val state = glfwGetCursorPos(window.handle, xBuffer, yBuffer)
            return yBuffer[0].toFloat()
        }

    override fun isKeyDown(keyCode: Int): Boolean {
        val state = glfwGetKey(window.handle, keyCode)
        return state == GLFW_PRESS || state == GLFW_REPEAT
    }

    override fun isMouseDown(mouseButton: Int): Boolean {
        val state = glfwGetMouseButton(window.handle, mouseButton)
        return state == GLFW_PRESS || state == GLFW_REPEAT
    }



}
