package nexus.plugin

/**
 * This should be able to be parsesd
 */
data class PluginMeta(
    val id: String,
    val includeList: List<Include>,
    val extensions: Extensions,
) {
    data class Include(
        val resource: String?,
    )

    data class Extensions(
        val namespace: String,
        val extensionList: List<Extension>,
    ) {

        interface Extension {
            val name: String
            val targetClass: String
            val isImplementation: Boolean
        }

        /**
         * This should be used for interfaces
         */
        data class InterfaceExtension(
            override val name: String,
            override val targetClass: String,
        ) : Extension {
            override val isImplementation: Boolean = false
        }

        /**
         * This is used for any implementation of an extension
         */
        data class ImplementationExtension(
            override val name: String,
            override val targetClass: String,
        ) : Extension {
            override val isImplementation: Boolean = true
        }

        companion object {
            val Empty: Extensions = Extensions("", emptyList())
        }

    }


}