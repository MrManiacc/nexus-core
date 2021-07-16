package nexus.engine.texture

import nexus.engine.assets.AssetInstance
import java.nio.ByteBuffer

abstract class TextureInstance(textureID: Int, width: Int, height: Int, slot: Int, channels: Int) :
    AssetInstance<Texture, TextureData> {
    var textureID: Int = textureID
        protected set

    var width: Int = width
        protected set

    var height: Int = height
        protected set

    var slot: Int = slot
        protected set
    var channels: Int = channels
        protected set


    abstract fun setParameter(name: Int, value: Int)


    /**
     * Uploads image data with specified width and height.
     *
     * @param width  Width of the image
     * @param height Height of the image
     * @param data   Pixel data of the image
     */
    fun uploadData(data: ByteBuffer) = uploadData(width, height, data)


    /**
     * Uploads image data with specified internal format, width, height and
     * image format.
     *
     * @param internalFormat Internal format of the image data
     * @param width          Width of the image
     * @param height         Height of the image
     * @param format         Format of the image data
     * @param data           Pixel data of the image
     */
    abstract fun uploadData(internalFormat: Int, width: Int, height: Int, format: Int, data: ByteBuffer)

    /**
     * Uploads image data with specified width and height.
     *
     * @param width  Width of the image
     * @param height Height of the image
     * @param data   Pixel data of the image
     */
    abstract fun uploadData(width: Int, height: Int, data: ByteBuffer)

    /**
     * This should bind a texture for rendering
     */
    abstract fun bind(slot: Int = -1)


}