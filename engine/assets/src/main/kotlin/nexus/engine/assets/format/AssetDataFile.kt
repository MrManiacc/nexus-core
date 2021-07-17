package nexus.engine.assets.format

import com.google.common.base.Charsets
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import java.util.*
import javax.annotation.concurrent.Immutable

/**
 * An asset data file. Provides details on the file's name, extension and allows the file to be opened as a stream.
 * <p>
 * FileFormats are not given direct access to the Path or File, as asset types provided by modules may not have IO permissions.
 * </p>
 * <p>
 * Immutable.
 * </p>
 *
 * @author Immortius
 */
@Immutable class AssetDataFile(
    /**
     * This should be created via classgraph. class
     */
    val file: FileReference,
) {

    /**
     * @return The name of the file (including extension)
     */
    val fileName: String get() = file.name

    /**
     * @return The file extension.
     */
    val fileExtension: String
        get() {
            val fName: String = this.fileName
            return if (fName.contains(".")) {
                fName.substring(fName.lastIndexOf(".") + 1)
            } else ""
        }


    /**
     * Opens a stream to read the file. It is up to the stream's user to close it after use.
     *
     * @return A new buffered input stream.
     * @throws IOException If there was an error opening the file
     */
    @Throws(IOException::class) fun openStream(): InputStream {
        return try {
            @Suppress("UNCHECKED_CAST")
            AccessController.doPrivileged(file::open as PrivilegedExceptionAction<InputStream>)
        } catch (e: PrivilegedActionException) {
            throw IOException("Failed to open stream for '$file'", e)
        }
    }

    /**
     * Opens a reader to read the file. It is up to the reader's user to close it after use.
     *
     * @param charset The character set to interpret the file with
     * @return A new buffered reader.
     * @throws IOException If there was an error opening the file
     */
    @Throws(IOException::class) fun openReader(charset: Charset = Charsets.UTF_8): BufferedReader {
        return BufferedReader(InputStreamReader(openStream(), charset))
    }


    override fun toString(): String = file.toString()

    override fun equals(other: Any?): Boolean {
        if (other === this)
            return true
        if (other is AssetDataFile)
            return other.file == file
        return false
    }

    override fun hashCode(): Int = Objects.hash(file)


}
