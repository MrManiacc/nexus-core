package nexus.engine.texture

import nexus.engine.assets.AssetData
import java.nio.ByteBuffer

/**
 * This data class should be able to get an actual path from the given string path
 */
open class TextureData(
    //Load from path, this is optional can specific the data as well.
    val path: String? = null,
    //The data that can be loaded via stbi_load_from_memory
    val image: ByteBuffer? = null,
    //When true we tell stb to flip the image upon loading
    val flipVertically: Boolean = true,
    //The slot to load the texture to
    val slot: Int = 0,
    //Override the width
    val width: Int? = null,
    //Override the height
    val height: Int? = null
) : AssetData<Texture, TextureData> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextureData

        if (path != other.path) return false
        if (image != other.image) return false
        if (flipVertically != other.flipVertically) return false
        if (slot != other.slot) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path?.hashCode() ?: 0
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + flipVertically.hashCode()
        result = 31 * result + slot
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        return result
    }
}


/**
 * This data class should be able to get an actual path from the given string path
 */
class TextureInstanceData(
    slot: Int = 0,
    vararg val parameters: Pair<Int, Int>
) : TextureData(slot = slot)