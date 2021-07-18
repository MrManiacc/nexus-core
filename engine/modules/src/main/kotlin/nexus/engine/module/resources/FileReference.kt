package nexus.engine.module.resources

import java.io.IOException
import java.io.InputStream


/**
 * A handle describing and providing access to a file from a [ModuleFileSource]
 */
interface FileReference {

    /**
     * @return The name of the file
     */
    val name: String

    /**
     * @return The name of the file
     */
    val fullName: String
    /**
     * @return The path to the file (within the file source)
     */
    val path: List<String>

    /**
     * We're a folder if the last path offset isnt a fi
     */
    val isFolder: Boolean get() = !path.last().contains(".")

    /**
     * @return An new InputStream for reading the file. Closing the stream is the duty of the caller
     * @throws IOException If there is an exception opening the file
     */
    @Throws(IOException::class) fun open(): InputStream


}