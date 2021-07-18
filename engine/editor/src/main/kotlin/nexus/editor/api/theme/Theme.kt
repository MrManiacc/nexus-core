package nexus.editor.api.theme

interface Theme {
    /**
     * This applies the given theme
     */
    val panelTheme: PanelTheme.() -> Unit

    companion object {

        fun theme(block: PanelTheme.() -> Unit): Theme = PanelThemeImpl(block)

        operator fun invoke(block: PanelTheme.() -> Unit) = theme(block)
    }

    data class PanelThemeImpl internal constructor(override val panelTheme: PanelTheme.() -> Unit) : Theme

}

