package nexus.plugins.opengl.data

import nexus.engine.render.Buffer
import nexus.engine.render.VertexArray
import nexus.plugins.opengl.GLBuffer
import nexus.plugins.opengl.GLVertexArray

/**
 * Stores pairs of primitives as simple floats
 */
object Primitives {


    /*This creates a quad of size 0.5*/
    val QuadVAO: VertexArray = GLVertexArray().apply {
        this += GLBuffer.GLVertexBuffer(
            floatArrayOf(
                0.5f, 0.5f, 0.0f, // top right
                0.5f, -0.5f, 0.0f, // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                -0.5f, 0.5f, 0.0f// top left
            ), Buffer.VertexBuffer.DataType.Float3, 0
        )

        this += GLBuffer.GLVertexBuffer(
            floatArrayOf(
                0f, 0f,
                1f, 0f,
                1f, 1f,
                0f, 1f
            ), Buffer.VertexBuffer.DataType.Float2, 1
        )

        this += GLBuffer.GLIndexBuffer(
            intArrayOf(
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
            )
        )
    }

    /*This creates a quad of size 0.5*/
    val TriangleVAO: VertexArray = GLVertexArray().apply {
        this += GLBuffer.GLVertexBuffer(
            floatArrayOf(
                -0.5f, -0.5f, 0.0f,  // bottom left
                0.5f, -0.5f, 0.0f,  // bottom right
                0.0f, 0.5f, 0.0f, // top center
            ), Buffer.VertexBuffer.DataType.Float3, 0
        )

        this += GLBuffer.GLIndexBuffer(
            intArrayOf(
                0, 1, 2,   // first triangle
            )
        )
    }


}