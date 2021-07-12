package nexus.plugins.glfw

import dorkbox.messageBus.annotations.Subscribe
import nexus.engine.Application
import nexus.engine.events.Events
import nexus.engine.glfw.IWindow

import nexus.engine.render.RenderAPI
import mu.KotlinLogging
import org.joml.Vector4i
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import kotlin.io.path.ExperimentalPathApi

data class GlfwWindow(
    override val width: Int = 1920,
    override val height: Int = 1080,
    override val title: String = "glfw-window",
    val app: Application<*>,

    ) : IWindow {
    private val keyEvent = Events.Input.KeyEvent(this, -1, -1, -1, -1)
    private val keyPressEvent = Events.Input.KeyPress(this, -1, -1)
    private val keyReleaseEvent = Events.Input.KeyRelease(this, -1, -1)
    private val keyRepeatEvent = Events.Input.KeyRepeat(this, -1, -1)
    private val mousePressEvent = Events.Input.MousePress(this, -1, -1)
    private val mouseEvent = Events.Input.MouseEvent(this, -1, -1, -1)
    private val mouseReleaseEvent = Events.Input.MouseRelease(this, -1, -1)
    private val mouseRepeatEvent = Events.Input.MouseRepeat(this, -1, -1)
    private val mouseScrollEvent = Events.Input.MouseScroll(this, 0.0f, 0.0f)
    private val mouseMoveEvent = Events.Input.MouseMove(this, 0.0f, 0.0f)
    private val resizeEvent = Events.Window.Resize(this, -1, -1)
    private val saveData: Vector4i = Vector4i()
    private var mode: GLFWVidMode? = null
    private val log = KotlinLogging.logger {}
    override var handle: Long = -1
    private val renderAPI: RenderAPI get() = app.renderAPI
    override val shouldClose: Boolean get() = GLFW.glfwWindowShouldClose(handle)
    override val size: Pair<Int, Int>
        get() {
            val width = intArrayOf(0)
            val height = intArrayOf(0)
            GLFW.glfwGetWindowSize(handle, width, height)
            return width[0] to height[0]
        }
    override val pos: Pair<Int, Int>
        get() {
            val x = intArrayOf(0)
            val y = intArrayOf(0)
            GLFW.glfwGetWindowPos(handle, x, y)
            return x[0] to y[0]
        }
    override var vsync: Boolean = true
        set(value) {
            GLFW.glfwSwapInterval(if (value) 1 else 0)
            field = value
        }
    override var fullscreen: Boolean = false
        set(value) {
            if (value) {
                val monitor = getMonitorForWindow()
                mode = GLFW.glfwGetVideoMode(monitor) ?: return
                val x = IntArray(1)
                val y = IntArray(1)
                val width = IntArray(1)
                val height = IntArray(1)
                GLFW.glfwGetWindowPos(handle, x, y)
                GLFW.glfwGetWindowSize(handle, width, height)
                saveData.set(x[0], y[0], width[0], height[0])
                GLFW.glfwSetWindowMonitor(handle, monitor, 0, 0, mode!!.width(), mode!!.height(), mode!!.refreshRate())
            } else {
                GLFW.glfwSetWindowMonitor(handle, MemoryUtil.NULL, saveData.x, saveData.y, saveData.z, saveData.w, 0)
            }
            field = value
        }

    @Subscribe
    fun onInitialize(event: Events.App.Initialized) {
        log.info { "initializing the window: $this" }
        initWindow()
        renderAPI.init()
        initCallbacks()
        finalizeWindow()
        app.publish(Events.Window.Initialized(this))
    }

    @Subscribe
    @ExperimentalPathApi
    fun onKeyPress(event: Events.Input.KeyPress) {
        if (event.key == GLFW.GLFW_KEY_F1)
            fullscreen = !fullscreen
        if (event.key == GLFW.GLFW_KEY_F2)
            center()
        if (event.key == GLFW.GLFW_KEY_F3)
            vsync = !vsync
        if (event.key == GLFW.GLFW_KEY_ESCAPE)
            app.destroy()

    }

    private fun center() {
        if (fullscreen) return
        // Get the thread stack and push a new frame
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*
            GLFW.glfwSetWindowSize(handle, this.width, this.height)
            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(handle, pWidth, pHeight)
            // Center the window
            val monitor = getMonitorForWindow()
            val area = getMonitorArea(monitor)

            mode = GLFW.glfwGetVideoMode(monitor) ?: return
            GLFW.glfwSetWindowPos(
                handle,
                area.x + (mode!!.width() - pWidth[0]) / 2,
                area.y + (mode!!.height() - pHeight[0]) / 2
            )
        }
    }

    private fun getMonitorForWindow(): Long {
        val primary = GLFW.glfwGetPrimaryMonitor()
        val monitors = GLFW.glfwGetMonitors() ?: return primary
        val constraints = getConstraints(handle)
        for (i in 0 until monitors.capacity()) {
            val monHandle = monitors[i]
            val area = getMonitorArea(monHandle)
            val cx = (constraints.z / 2) + constraints.x
            val cy = (constraints.w / 2) + constraints.y
            if ((area.x < cx && area.z > cx) && (area.y < cy && area.w > cy)) {
                return monHandle
            }
        }
        return primary
    }

    private fun getMonitorArea(monitor: Long): Vector4i {
        val x = IntArray(1)
        val y = IntArray(1)
        val width = IntArray(1)
        val height = IntArray(1)
        GLFW.glfwGetMonitorWorkarea(monitor, x, y, width, height)
        return Vector4i(x[0], y[0], width[0], height[0])
    }

    private fun getConstraints(handle: Long): Vector4i {
        val x = IntArray(1)
        val y = IntArray(1)
        val width = IntArray(1)
        val height = IntArray(1)
        GLFW.glfwGetWindowPos(handle, x, y)
        GLFW.glfwGetWindowSize(handle, width, height)
        return Vector4i(x[0], y[0], width[0], height[0])
    }

    private fun finalizeWindow() {

        // Enable v-sync
        GLFW.glfwSwapInterval(if (vsync) 1 else 0)

        // Make the window visible
        GLFW.glfwShowWindow(handle)
    }

    private fun initWindow() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()
        if (!GLFW.glfwInit()) throw IllegalStateException("Unable to create glfw")
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)
        GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE)
        handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (handle == MemoryUtil.NULL) throw  RuntimeException("Failed to create the GLFW window");
    }

    private fun initCallbacks() {
        GLFW.glfwSetScrollCallback(handle) { _, xOffset, yOffset ->
            mouseScrollEvent.xOffset = xOffset.toFloat()
            mouseScrollEvent.yOffset = yOffset.toFloat()
            app.publish(mouseMoveEvent)
        }

        GLFW.glfwSetCursorPosCallback(handle) { _, x, y ->
            mouseMoveEvent.x = x.toFloat()
            mouseMoveEvent.y = y.toFloat()
            app.publish(mouseMoveEvent)
        }

        GLFW.glfwSetKeyCallback(handle) { _, key, scancode, action, mods ->
            keyEvent.key = key
            keyEvent.action = action
            keyEvent.scancode = scancode
            keyEvent.mods = mods
            app.publish(keyEvent)
            when (action) {
                0 -> {
                    keyPressEvent.key = key
                    keyPressEvent.mods = mods
                    app.publish(keyPressEvent)
                }
                1 -> {
                    keyReleaseEvent.key = key
                    keyReleaseEvent.mods = mods
                    app.publish(keyReleaseEvent)
                }
                2 -> {
                    keyRepeatEvent.key = key
                    keyRepeatEvent.mods = mods
                    app.publish(keyRepeatEvent)
                }

            }
        }
        GLFW.glfwSetMouseButtonCallback(handle) { _, button, action, mods ->
            mouseEvent.button = button
            mouseEvent.action = action
            mouseEvent.mods = mods
            app.publish(mouseEvent)
            when (action) {
                0 -> {
                    mousePressEvent.button = button
                    mousePressEvent.mods = mods
                    app.publish(mousePressEvent)
                }
                1 -> {
                    mouseReleaseEvent.button = button
                    mouseReleaseEvent.mods = mods
                    app.publish(mouseReleaseEvent)
                }
                2 -> {
                    mouseRepeatEvent.button = button
                    mouseRepeatEvent.mods = mods
                    app.publish(mouseRepeatEvent)
                }
            }
        }

        GLFW.glfwSetFramebufferSizeCallback(handle) { _, width, height ->
            resizeEvent.width = width
            resizeEvent.height = height
            app.publish(resizeEvent)
        }

        GLFW.glfwSetWindowCloseCallback(handle) { _ ->
            app.publish(Events.Window.Destroy(this))
        }
    }

    override fun swapBuffers() {
        GLFW.glfwSwapBuffers(handle)
    }

    override fun pollInput() {
        GLFW.glfwPollEvents()
    }

    @Subscribe
    fun onClose(event: Events.App.Shutdown) {
        GLFW.glfwDestroyWindow(handle)
    }

}