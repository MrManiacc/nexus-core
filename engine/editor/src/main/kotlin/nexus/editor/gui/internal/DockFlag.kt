package nexus.editor.gui.internal

enum class DockFlag(val value: Int) {
    None(0), KeepAliveOnly(1), NoCentralNode(1 shl 1),
    NoDockingInCentralNode(1 shl 2), PassThroughCentralNode(1 shl 3),
    NoSplit(1 shl 4), NoResize(1 shl 5), AutoHideTabBar(1 shl 6),
    Dockspace(1 shl 10), CentralNode(1 shl 11), NoTabBar(1 shl 12),
    HiddenTabBar(1 shl 13), NoWindowMenuButton(1 shl 14),
    NoCloseButton(1 shl 15), NoDocking(1 shl 16), NoDockingSplitMe(1 shl 17),
    NoDockingSplitOther(1 shl 18), NoDockingOverMe(1 shl 19), NoDockingOverOther(1 shl 20),
    NoResizeX(1 shl 21), NoResizeY(1 shl 22);

    operator fun invoke(): Int = value

    companion object {
        fun combine(flags: Array<out DockFlag>): Int {
            var output = None()
            flags.forEach {
                output = output or it()
            }
            return output
        }
    }
}