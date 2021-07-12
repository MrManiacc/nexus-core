package nexus.plugins.opengl

import nexus.engine.render.Buffer
import nexus.engine.render.VertexArray
import org.lwjgl.opengl.GL30.*

/*
 * This will create a new OpenGl Vertex Attribute Object, or VAO for short.
 * It is comprised of multiple VBO (vertex buffer objects), as well as IBOS (Index buffer objects).
 */
data class GLVertexArray(
    override val buffers: MutableList<Buffer> = ArrayList()
) : VertexArray() {
    private var array: Int = -1

    /*Createthe vertex array. Must be done after the given renderAPI is setup.*/
    override fun create() {
        if (array != -1) dispose()
        array = glGenVertexArrays()
        bind()
        buffers.forEach(Buffer::create)
    }

    /*NumberBinds this vertex array, after this is called you should be able to draw the element.*/
    override fun bind() {
        glBindVertexArray(array)
    }

    /*Binds the default 0 vertex array.*/
    override fun unbind() {
        glBindVertexArray(0)
    }

    /*This is used to dispose of the vertex array after we're done with it*/
    override fun dispose() {
        unbind()
        glDeleteVertexArrays(array)
        buffers.forEach(Buffer::destroy)
    }

    /*NumberAdds a vertex buffer to be rendered with EX. vertices, texture coords, etc.*/
    override fun <T : Buffer.VertexBuffer> addVertexBuffer(vertexBuffer: T) =
        vertexBuffer.apply { buffers += this }
            .let {
                log.info { "Added vertex buffer: $it" }
            }

    /*This allows for the use of drawing faced with indexes which reduces the nexus.engine.render overhead*/
    override fun <T : Buffer.IndexBuffer> addIndexBuffer(indexBuffer: T) =
        indexBuffer.apply { buffers += this }
            .let {
                log.info { "Added vertex buffer: $it" }
            }

}