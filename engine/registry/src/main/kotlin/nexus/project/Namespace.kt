package nexus.project

/**
 * A namespace is used to uniquely identify a group of extensions
 */
@JvmInline
value class Namespace(val namespace: String) {
    override fun toString(): String = namespace
}