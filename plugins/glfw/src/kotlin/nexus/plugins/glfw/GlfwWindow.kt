package nexus.plugins.glfw

import dorkbox.messageBus.annotations.*
import marx.engine.*
import marx.engine.events.*
import marx.engine.events.Events.Input.KeyPress
import marx.engine.nexus.plugins.glfw.IWindow
import marx.engine.render.*
import mu.*
import org.joml.*
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.system.MemoryUtil.*
import kotlin.io.path.ExperimentalPathApi
data class GlfwWindow(
    override val width: Int = 1920,
    override val height: Int = 1080,
    override val title: String = "glfw-window",
    val app: Application<*>,

    ) : IWindow {
    private val keyEvent = Events.Input.KeyEvent(this, -1, -1, -1, -1)
    private val keyPressEvent = KeyPress(this, -1, -1)
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
    override val shouldClose: Boolean get() = glfwWindowShouldClose(handle)
    override val size: Pair<Int, Int>
        get() {
            val width = intArrayOf(0)
            val height = intArrayOf(0)
            glfwGetWindowSize(handle, width, height)
            return width[0] to height[0]
        }
    override val pos: Pair<Int, Int>
        get() {
            val x = intArrayOf(0)
            val y = intArrayOf(0)
            glfwGetWindowPos(handle, x, y)
            return x[0] to y[0]
        }
    override var vsync: Boolean = true
        set(value) {
            glfwSwapInterval(if (value) 1 else 0)
            field = value
        }
    override var fullscreen: Boolean = false
        set(value) {
            if (value) {
                val monitor = getMonitorForWindow()
                mode = glfwGetVideoMode(monitor) ?: return
                val x = IntArray(1)
                val y = IntArray(1)
                val width = IntArray(1)
                val height = IntArray(1)
                glfwGetWindowPos(handle, x, y)
                glfwGetWindowSize(handle, width, height)
                saveData.set(x[0], y[0], width[0], height[0])
                glfwSetWindowMonitor(handle, monitor, 0, 0, mode!!.width(), mode!!.height(), mode!!.refreshRate())
            } else {
                glfwSetWindowMonitor(handle, NULL, saveData.x, saveData.y, saveData.z, saveData.w, 0)
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
    fun onKeyPress(event: KeyPress) {
        if (event.key == GLFW_KEY_F1)
            fullscreen = !fullscreen
        if (event.key == GLFW_KEY_F2)
            center()
        if (event.key == GLFW_KEY_F3)
            vsync = !vsync
        if (event.key == GLFW_KEY_ESCAPE)
            app.destroy()

    }

    private fun center() {
        if (fullscreen) return
        // Get the thread stack and push a new frame
        stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*
            glfwSetWindowSize(handle, this.width, this.height)
            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(handle, pWidth, pHeight)
            // Center the window
            val monitor = getMonitorForWindow()
            val area = getMonitorArea(monitor)

            mode = glfwGetVideoMode(monitor) ?: return
            glfwSetWindowPos(
                handle,
                area.x + (mode!!.width() - pWidth[0]) / 2,
                area.y + (mode!!.height() - pHeight[0]) / 2
            )
        }
    }

    private fun getMonitorForWindow(): Long {
        val primary = glfwGetPrimaryMonitor()
        val monitors = glfwGetMonitors() ?: return primary
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
        glfwGetMonitorWorkarea(monitor, x, y, width, height)
        return Vector4i(x[0], y[0], width[0], height[0])
    }

    private fun getConstraints(handle: Long): Vector4i {
        val x = IntArray(1)
        val y = IntArray(1)
        val width = IntArray(1)
        val height = IntArray(1)
        glfwGetWindowPos(handle, x, y)
        glfwGetWindowSize(handle, width, height)
        return Vector4i(x[0], y[0], width[0], height[0])
    }

    private fun finalizeWindow() {

        // Enable v-sync
        glfwSwapInterval(if (vsync) 1 else 0)

        // Make the window visible
        glfwShowWindow(handle)
    }

    private fun initWindow() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) throw IllegalStateException("Unable to create glfw")
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE)
        handle = glfwCreateWindow(width, height, title, NULL, NULL)
        if (handle == NULL) throw  RuntimeException("Failed to create the GLFW window");
    }

    private fun initCallbacks() {
        glfwSetScrollCallback(handle) { _, xOffset, yOffset ->
            mouseScrollEvent.xOffset = xOffset.toFloat()
            mouseScrollEvent.yOffset = yOffset.toFloat()
            app.publish(mouseMoveEvent)
        }

        glfwSetCursorPosCallback(handle) { _, x, y ->
            mouseMoveEvent.x = x.toFloat()
            mouseMoveEvent.y = y.toFloat()
            app.publish(mouseMoveEvent)
        }

        glfwSetKeyCallback(handle) { _, key, scancode, action, mods ->
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
        glfwSetMouseButtonCallback(handle) { _, button, action, mods ->
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

        glfwSetFramebufferSizeCallback(handle) { _, width, height ->
            resizeEvent.width = width
            resizeEvent.height = height
            app.publish(resizeEvent)
        }

        glfwSetWindowCloseCallback(handle) { _ ->
            app.publish(Events.Window.Destroy(this))
        }
    }

    override fun swapBuffers() {
        glfwSwapBuffers(handle)
    }

    override fun pollInput() {
        glfwPollEvents()
    }

    @Subscribe
    fun onClose(event: Events.App.Shutdown) {
        glfwDestroyWindow(handle)
    }

}