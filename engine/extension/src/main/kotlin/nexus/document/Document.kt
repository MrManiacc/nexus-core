package nexus.document

import nexus.plugin.PluginMeta
import nexus.util.findChildWithName
import nexus.util.findChildrenWithName
import nexus.util.forEach
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * This provides a wrapper around a xml document
 */
class Document(source: DocumentSource) {
    private val builder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    private val pluginMetas: MutableCollection<PluginMeta> = ArrayList()

    //Prevent modification of the collection
    val metas: Collection<PluginMeta> get() = pluginMetas

    init {
        val src = source.resolve()
        val document = builder.parse(InputSource(StringReader(src)))
        document.documentElement.normalize()
        val nodes = document.getElementsByTagName("nexus-plugin")
        nodes.forEach {
            pluginMetas.add(parsePlugin(this))
        }
    }


    /**
     * This is called upon instantiation. The source is resolved from the [DocumentSource], here we are to parse the
     * source into a readable format
     */
    private fun parsePlugin(plugin: Node): PluginMeta {
        val id = getID(plugin)
        val includes = getIncludes(plugin)
        val extensions = getExtensions(plugin)
        return PluginMeta(id, includes, extensions)
    }


    private fun getID(plugin: Node): String {
        return plugin.childNodes.findChildWithName("id")?.textContent?.trim() ?: "invalid_id"
    }


    private fun getIncludes(plugin: Node): List<PluginMeta.Include> {
        val includes = ArrayList<PluginMeta.Include>()
        plugin.childNodes.findChildrenWithName("include").forEach {
            includes.add(PluginMeta.Include(it.attributes.getNamedItem("resource").textContent ?: "invalid_resource"))
        }
        return includes
    }

    private fun getExtensions(plugin: Node): PluginMeta.Extensions {
        val extensionList = ArrayList<PluginMeta.Extensions.Extension>()
        val extensionNode = plugin.childNodes.findChildWithName("extensions") ?: return PluginMeta.Extensions.Empty
        val namespace =
            extensionNode.attributes.getNamedItem("namespace").textContent ?: return PluginMeta.Extensions.Empty
        extensionNode.childNodes.forEach {
            val name = this.nodeName
            if (hasAttributes()) {
                val interfaceTarget = this.attributes.getNamedItem("interface")
                if (interfaceTarget != null)
                    extensionList.add(PluginMeta.Extensions.InterfaceExtension(name, interfaceTarget.textContent))
                val classTarget = this.attributes.getNamedItem("class")
                if (classTarget != null)
                    extensionList.add(PluginMeta.Extensions.ImplementationExtension(name, classTarget.textContent))
            }
        }
        return PluginMeta.Extensions(namespace, extensionList)
    }
}