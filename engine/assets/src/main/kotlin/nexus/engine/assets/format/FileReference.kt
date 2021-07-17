package nexus.engine.assets.format

import java.io.IOException
import java.io.InputStream


/**
 * A handle describing and providing access to a file from a [AssetFileProvider]
 */
interface FileReference {

    /**
     * @return The name of the file
     */
    val name: String

    /**
     * @return The path to the file (within the file source)
     */
    val path: List<String>

    /**
     * @return An new InputStream for reading the file. Closing the stream is the duty of the caller
     * @throws IOException If there is an exception opening the file
     */
    @Throws(IOException::class) fun open(): InputStream


}