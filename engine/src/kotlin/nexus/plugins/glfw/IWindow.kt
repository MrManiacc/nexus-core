package marx.engine.nexus.plugins.glfw

import marx.engine.render.*

interface IWindow {
    val title: String
    val width: Int
    val height: Int
    val shouldClose: Boolean get() = false
    var fullscreen: Boolean
    var vsync: Boolean
    fun swapBuffers()
    fun pollInput()
    var handle: Long

    val size: Pair<Int, Int>
    val pos: Pair<Int, Int>
}