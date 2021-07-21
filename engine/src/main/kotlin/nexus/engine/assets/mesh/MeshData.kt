package nexus.engine.assets.mesh

import nexus.engine.assets.AssetData
import nexus.engine.assets.AssetDataFlags
import kotlin.reflect.KClass

/**
 * This is used for our mesh data. This is sent to the
 */
class MeshData : AssetData, AssetDataFlags {

    //This is required, a mesh MUST have vertices
    var vertices: FloatArray = floatArrayOf()

    //Not required, but allows for texturing and all kinds of things
    var textureCoords: FloatArray = floatArrayOf()

    //using for physics, normal mapping, and all kinds of lighting things, not required required, but bascially required
    var normals: FloatArray = floatArrayOf()

    //used for parallax mapping and other lighting things
    var tangents: FloatArray = floatArrayOf()

    //Not required, but when supplied allows for rendering with glElements which is more efficient
    var indices: IntArray = intArrayOf()

    //This allows us to supply specific flags to the mesh when loading.
    override val flags: MutableMap<KClass<*>, MutableMap<String, Any>> = HashMap()
    override fun toString(): String {
        return "MeshData(vertices=${vertices.contentToString()}, textureCoords=${textureCoords.contentToString()}, normals=${normals.contentToString()}, tangents=${tangents.contentToString()}, indices=${indices.contentToString()}, flags=$flags)"
    }

}