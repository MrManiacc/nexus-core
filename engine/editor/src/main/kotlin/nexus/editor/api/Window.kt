package nexus.editor.api

import nexus.editor.api.internal.Anchor

/**
 * A floating window is a type of gui element that is used for dialog, file selectors or anything else like that.
 * it's a literal floating window that might be equivlent to the popup windows we see in intellij say when selecting
 * someting like File -> Project Settings, where the popup window, the actual ui content will be the equilvent of this
 * [FloatWindow] class
 */
interface Window : Element {
    /**
     * THis is only used for the starting position of the float window, in reality
     * it can be dragged around like any other normal window
     */
    val anchor: Anchor

    /**
     * If a window can be external from the process or if it's docked internally
     */
    val externalize: Boolean


}