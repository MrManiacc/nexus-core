package nexus.engine.editor.render

interface NamedElement : Element {
    /**
     * This is the key of the element. This is *required* because of it's use in imgui
     */
    val name: String

    /**
     * This attempts to get the name of the element by getting all of the text before the ##.
     * The name is the imgui key.
     */
    val displayName: String get() = name.substringBefore("##", name)

}