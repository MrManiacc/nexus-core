package marx.engine.render

import java.lang.IllegalStateException
import kotlin.reflect.jvm.*

/*
 * This is the underlying data structure that will be sent to the graphics card. It should have all of the
 * mesh data
 */
interface Buffer {

    fun create()
    fun bind()
    fun unbind()
    fun destroy()

    companion object {

        inline operator fun <reified T : VertexBuffer> invoke(floats: FloatArray, size: Int = 3): T {
            val clazz = T::class
            val constructors = clazz.constructors

            for (ctr in constructors) {
                ctr.isAccessible = true
                val params = ctr.parameters
                if (params.size == 2) {
                    ctr.isAccessible = true
                    return ctr.call(floats, size)
                }
            }
            throw IllegalStateException("Failed to find the correct constructor!!")
        }

        inline operator fun <reified T : IndexBuffer> invoke(indices: IntArray): T {
            val clazz = T::class
            val constructors = clazz.constructors
            for (ctr in constructors) {
                val params = ctr.parameters
                if (params.size == 1) {
                    ctr.isAccessible = true
                    return ctr.call(indices)
                }
            }
            throw IllegalStateException("Failed to find the correct constructor!!")
        }
    }

    abstract class VertexBuffer(val vertices: FloatArray, val vertexSize: Int) : Buffer {

        override fun toString(): String =
            "VertexBuffer(vertices=${vertices.contentToString()}, vertexSize=$vertexSize)"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is VertexBuffer) return false

            if (!vertices.contentEquals(other.vertices)) return false
            if (vertexSize != other.vertexSize) return false

            return true
        }

        override fun hashCode(): Int {
            var result = vertices.contentHashCode()
            result = 31 * result + vertexSize
            return result
        }


    }

    abstract class IndexBuffer(val indices: IntArray) : Buffer {
        override fun toString(): String =
            "IndexBuffer(indices=${indices.contentToString()})"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is IndexBuffer) return false

            if (!indices.contentEquals(other.indices)) return false

            return true
        }

        override fun hashCode(): Int {
            return indices.contentHashCode()
        }


    }


}