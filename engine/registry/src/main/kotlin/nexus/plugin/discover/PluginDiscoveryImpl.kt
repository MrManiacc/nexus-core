package nexus.plugin.discover

import io.github.classgraph.ClassGraph
import io.github.classgraph.ResourceList
import nexus.document.Document
import nexus.document.DocumentSource
import nexus.plugin.Plugin
import nexus.plugin.PluginDiscovery

/**
 * This implements the project discovery. It uses class graph to discover the plugin.xml's on the class path then
 * load them
 */
class PluginDiscoveryImpl internal constructor() : PluginDiscovery {
    /**
     * This should locate all of the plugins on the classpath
     */
    override fun discover(): Collection<Plugin> {
        val documents = findDocuments("META-INF")
        val result = ArrayList<Plugin>()
        for (document in documents)
            document.metas.forEach {
                result.add(Plugin.create(it))
            }
        return result
    }

    private fun findDocuments(vararg path: String): Collection<Document> {
        val rawFiles = ClassGraph().acceptPathsNonRecursive(*path).enableAllInfo()
            .scan(4).getResourcesWithExtension("xml")
        return parserResources(rawFiles)
    }

    private fun parserResources(list: ResourceList): Collection<Document> {
        val result = ArrayList<Document>()
        for (resource in list)
            result.add(Document(DocumentSource.resource(resource)))
        return result
    }


}