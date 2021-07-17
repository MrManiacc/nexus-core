package nexus.editor.gui.internal

import java.util.*

/**
 * This is used to uniquely identify the name of a ui element
 */
data class ID(val id: String) {
    val uniqueId: String = getUniqueId(this)

    /**
     * This attempts to get the name aka the part after the last period
     */
    val name: String get() = id.substringAfterLast(".", id)

    /**
     * This attempts to get the package aka the part before the last period
     */
    val group: String get() = id.substringAfterLast(".", id)

    /**
     * This attempts to get the folder
     */
    val folderPath: String get() = id.substringBeforeLast("/", id)

    /**
     * This attempts to get the file extension by getting the last value after the last period (.)
     */
    val fileExtension: String get() = name

    /**
     * This gets the file's name fro mthe id
     */
    val fileName: String
        get() {
            var fName = id.substringAfterLast("/", id)
            fName = fName.substringBeforeLast(".", fName)
            return fName
        }

    /**
     * This is used as a window name identifier
     */
    val windowID: String get() = name.plus("##$id")

    /**
     * This simply returns out the window name
     */
    operator fun invoke(): String = "$folderPath/$fileName"

    /**
     * This should ruturn out the id, and all of it's information.
     */
    override fun toString(): String = "ID(group=$group, name=$name, id=$id, windowID=$windowID)"

    /**
     * This will keep track of the id's making sure to allow for uniqueness if possible
     */
    companion object {
        private val ids = HashSet<String>()
        internal fun getUniqueId(id: ID): String {
            if (ids.contains(id.id)) {
                val newId = "${id.id}##${UUID.randomUUID()}"
                ids.add(newId)
                return newId
            }
            ids.add(id.id)
            return id.id
        }
    }

}