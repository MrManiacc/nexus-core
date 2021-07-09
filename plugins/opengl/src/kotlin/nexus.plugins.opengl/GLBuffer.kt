package nexus.plugins.opengl

import marx.engine.render.*
import org.lwjgl.opengl.GL20.*

/*
 * This is the wrapper around the buffers. It provides access to the [Renderer.RenderAPI]
 */
interface GLBuffer : Buffer {
    var bufferId: Int

    //    val renderAPI: GLRenderAPI get() = Renderer()
    val type: Int

    /*
Binds the buffer for drawing to the shader
     */
    override fun bind() =
        glBindBuffer(type, bufferId)

    /*
Binds the buffer for drawing to the shader
     */
    override fun unbind() =
        glBindBuffer(type, 0)

    /*
   This is used during clean up of the given buffer
     */
    override fun destroy() {
        if (bufferId != -1)
            glDeleteBuffers(bufferId)
        bufferId = -1
    }

    /*
   This can be sent to a shader to render the data it contains
     */
    class GLVertexBuffer(verts: FloatArray, size: Int = 3) : Buffer.VertexBuffer(verts, size), GLBuffer {
        override var bufferId: Int = -1
        override val type: Int
            get() = GL_ARRAY_BUFFER

        /*
       This will put all of our data in the the buffer
         */
        override fun create() {
            bufferId = glGenBuffers()
            bind()
            glBufferData(type, vertices, GL_STATIC_DRAW)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, vertexSize * Float.SIZE_BYTES, 0L)
            glEnableVertexAttribArray(0)
        }

    }

    /*
   This can be sent to a shader to render the data it contains
     */
    class GLIndexBuffer(indices: IntArray) : Buffer.IndexBuffer(indices), GLBuffer {
        override var bufferId: Int = -1
        override val type: Int
            get() = GL_ELEMENT_ARRAY_BUFFER

        /*
       This will put all of our data in the the buffer
         */
        override fun create() {
            bufferId = glGenBuffers()
            bind()
            glBufferData(type, indices, GL_STATIC_DRAW)
        }
    }
}