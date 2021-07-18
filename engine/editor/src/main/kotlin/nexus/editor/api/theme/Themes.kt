package nexus.editor.api.theme

/**
 * This object stores all of our base themes
 */
object Themes {

    /**
    Primary: #282a36
    Secondary: #44475a
    Selection: #44475a
    Font: #f8f8f2
    Accent: #bd93f9
     */
    val BaseTheme = Theme {
        setTextColor(color("#f8f8f2"))
        setBodyColor(color("#282a36"))
        setAreaColor(color("#44475a"))
        setHeadColor(color("#bd93f9"))
        setTitleBar(color("#ffffff"))
    }

}