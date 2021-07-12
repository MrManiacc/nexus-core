package nexus.plugins.opengl

import org.lwjgl.BufferUtils.createByteBuffer
import org.lwjgl.system.MemoryUtil.memSlice
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object IOUtil {

    private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
        val newBuffer: ByteBuffer = createByteBuffer(newCapacity)
        buffer.flip()
        newBuffer.put(buffer)
        return newBuffer
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resource   the resource to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    @Throws(IOException::class)
    fun ioResourceToByteBuffer(resource: String, bufferSize: Int): ByteBuffer? {
        var buffer: ByteBuffer
        val path: Path = Paths.get(resource)
        if (Files.isReadable(path)) {
            Files.newByteChannel(path).use { fc ->
                buffer = createByteBuffer(fc.size() as Int + 1)
                while (fc.read(buffer) != -1) {
                }
            }
        } else {
            IOUtil::class.java.classLoader.getResourceAsStream(resource).use { source ->
                Channels.newChannel(source).use { rbc ->
                    buffer = createByteBuffer(bufferSize)
                    while (true) {
                        val bytes: Int = rbc.read(buffer)
                        if (bytes == -1) {
                            break
                        }
                        if (buffer.remaining() === 0) {
                            buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2) // 50%
                        }
                    }
                }
            }
        }
        buffer.flip()
        return memSlice(buffer)
    }
}
