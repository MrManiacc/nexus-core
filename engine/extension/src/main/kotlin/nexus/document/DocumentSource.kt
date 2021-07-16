package nexus.document

import io.github.classgraph.Resource
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path

fun interface DocumentSource {

    /**
     * This should return the actual source code of given xml file
     */
    fun resolve(): String


    companion object {
        /**
         * This will create and read the given document from file
         */
        fun file(file: File): DocumentSource = stream(FileInputStream(file))

        /**
         * Reads from path, which in reality simply reads from file
         */
        fun path(path: Path): DocumentSource = file(path.toFile())

        /**
         * This will read the given stream to string vai it's bytes.
         * The [stream] will then be closed as well as the [BufferedInputStream].
         * the resulting string is returned
         */
        fun stream(stream: InputStream): DocumentSource = DocumentSource {
            val buffStream = BufferedInputStream(stream)
            val result = String(buffStream.readBytes())
            buffStream.close()
            stream.close()
            result
        }

        /**
         * This reads a resource from path.
         */
        fun resource(resource: Resource): DocumentSource = stream(resource.open())


    }

}