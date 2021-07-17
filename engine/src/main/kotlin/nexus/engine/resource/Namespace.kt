package nexus.engine.resource

/**
 * This provides an easy to use namespace value
 */
data class Namespace(val group: String, val name: String) {
    constructor(namespace: String) : this(namespace.substringBeforeLast(".", namespace),
        namespace.substringAfterLast(".", namespace))

    /**
     * This is the actual namespace value. It combines the group and string
     */
    val namespace: String get() = if (group == name) name else "$group.$name"


    /**Simply return our stirng upon invoke**/
    operator fun invoke(): String = namespace

}