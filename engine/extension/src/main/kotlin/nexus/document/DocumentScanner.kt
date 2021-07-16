package nexus.document

import io.github.classgraph.ClassGraph
import mu.KotlinLogging
import org.slf4j.Logger


/**
 * This will use classgraph to scan for the specific files
 */
class DocumentScanner {
    private val logger: Logger = KotlinLogging.logger { }

    /**
     * This will scan using the regex`
     */
    fun scan(extension: String = "xml", path: String = "META-INF"): List<Document> {
        val result = ArrayList<Document>()
        ClassGraph().acceptPaths(path).scan().use { scanResult ->
            scanResult.getResourcesWithExtension(extension).forEach {
                result.add(Document(DocumentSource.resource(it)))
                logger.info("Found plugin document file: ${it.classpathElementFile.path}")
            }
        }
        return result
    }


}