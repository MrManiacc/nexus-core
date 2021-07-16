package nexus.engine.render

import org.apache.log4j.Layout
import kotlin.reflect.jvm.isAccessible

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
        /**This is used for configuring the vertex buffer*/
        protected val layouts: Array<Layout?> = arrayOfNulls(MAX_LAYOUTS)


        override fun toString(): String =
            "VertexBuffer(vertices=${vertices.contentToString()}, vertexSize=$vertexSize)"

        /**
         * This allows for setting of the layouts. THe order matters.
         */
        fun layout(vararg layouts: Layout): VertexBuffer = this.apply {
            layouts.forEachIndexed { i, layout -> this.layouts[i] = layout }
        }

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

        /**
         * This is used for laying out the specific layout of the vertex buffer
         */
        data class Layout(val type: DataType, val name: String)

        enum class DataType(val size: Int, val stride: Int) {
            Float3(3, Float.SIZE_BYTES * 3), Float2(2, Float.SIZE_BYTES * 2), Float1(1, Float.SIZE_BYTES),
            Int3(3, Int.SIZE_BYTES * 3), Int2(2, Int.SIZE_BYTES * 2), Int1(1, Int.SIZE_BYTES)
        }

        companion object {
            const val MAX_LAYOUTS = 15
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