package nexus.editor.gui

import nexus.editor.gui.container.MutableContainer
import nexus.editor.gui.internal.Anchor

/**
 * A tool panel is an extension that is to define a panel that allows for easy tool panel rendering.
 */
interface ToolPanel : MutableContainer, Dockable {
    /**
     * An anchor is the location to which we will attach our tool window too. If not passed it will not attach to
     * anything.
     */
    override val anchor: Anchor

    /**
     * This is the size of the panel 0.15 will be 15% of the parent and appropriate
     * for side panels.
     */
    override val sizeRatio: Float


}