package nexus.editor.gui.internal

enum class WindowFlag(val value: Int) {
    None(0), NoTitleBar(1), NoResize(1 shl 1),
    NoMove(1 shl 2), NoScrollBar(1 shl 3), NoScrollWithMouse(1 shl 4),
    NoCollapse(1 shl 5), AlwaysAutoResize(1 shl 6), NoBackground(1 shl 7),
    NoSavedSettings(1 shl 8), NoMouseInputs(1 shl 9), MenuBar(1 shl 10),
    HorizontalScrollBar(1 shl 11), NoFocusOnAppearing(1 shl 12), NoBringToFrontOnFocus(1 shl 13),
    AlwaysVerticalScrollbar(1 shl 14), AlwaysHorizontalScrollbar(1 shl 15), AlwaysUseWindowPadding(1 shl 16),
    NoNavInputs(1 shl 17), NoNavFocus(1 shl 18), UnsavedDocument(1 shl 20),
    NoDocking(1 shl 21),
    NoNav(NoNavInputs.value or NoNavFocus.value),
    NoDecoration(NoTitleBar.value or NoResize.value or NoScrollBar.value or NoCollapse.value),
    NoInputs(NoMouseInputs.value or NoNavInputs.value or NoNavFocus.value);

    operator fun invoke(): Int = value


    companion object {
        fun combine(flags: Array<out WindowFlag>): Int {
            var output = None()
            flags.forEach {
                output = output or it()
            }
            return output
        }
    }
}
