package nexus.plugins.opengl

import mu.KotlinLogging
import nexus.engine.assets.AssetInstance
import nexus.engine.texture.*
import org.lwjgl.opengl.GL45.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import org.slf4j.Logger
import java.nio.ByteBuffer

/**
 * This should create a new texture from the given path
 */
class GLTexture2D : Texture2D() {
    private var path: String? = null
    private var width: Int? = null
    private var height: Int? = null
    private var imageBuffer: ByteBuffer? = null
    private var image: ByteBuffer? = null
    private val log: Logger = KotlinLogging.logger { }
    private var channels: Int = 3

    /**
     * This should prepare the asset
     */
    override fun initialize(data: TextureData): GLTexture2D {
        if (data.path != null && data.image == null) {
            this.imageBuffer = IOUtil.ioResourceToByteBuffer(data.path!!, 8 * 1024)
            this.path = data.path
            log.debug("Loaded texture from path '$path' to an image buffer")
        }
        if (data.image != null) {
            this.imageBuffer = data.image
            log.debug("Updated the image buffer")
        }
        MemoryStack.stackPush().use { stack ->
            /* Prepare image buffers */
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val comp = stack.mallocInt(1)
            if (!stbi_info_from_memory(this.imageBuffer!!, w, h, comp)) {
                log.error("Failed to load image: ${this.path}")
                return this
            }

            stbi_set_flip_vertically_on_load(data.flipVertically)
            // Decode the image
            this.image = stbi_load_from_memory(imageBuffer!!, w, h, comp, 0);

            if (image == null) {
                log.error("Failed to load image: " + stbi_failure_reason());
                return this
            }

            /* Get width and height of image */
            width = w.get()
            height = h.get()
            this.channels = comp.get()
        }

        return this
    }

    /**
     * This should return a new asset instance of this type from the givnen asset
     */
    override fun instantiate(data: TextureData): AssetInstance<Texture, TextureData> {
        val texture =
            GLTexture2DInstance(glGenTextures(), this.width ?: -1, this.height ?: -1, data.slot, this.channels)
        texture.bind()
        if (data is TextureInstanceData) {
            data.parameters.forEach {
                texture.setParameter(it.first, it.second)
            }
        } else {
            texture.setParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            texture.setParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        }
        this.image?.let {
            texture.uploadData(it)
            stbi_image_free(it)
        }
        //TODO make parameters specifiable via the textureData
        return texture
    }

    /**
     * This is used for internal binding of texturesl
     */
    private class GLTexture2DInstance(
        textureID: Int = -1,
        width: Int = -1,
        height: Int = -1,
        slot: Int,
        channels: Int,
    ) :
        TextureInstance(textureID, width, height, slot, channels) {

        override fun setParameter(name: Int, value: Int) {
            glTexParameteri(GL_TEXTURE_2D, name, value)
        }


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
        override fun uploadData(internalFormat: Int, width: Int, height: Int, format: Int, data: ByteBuffer) {
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, data)
        }

        /**
         * Uploads image data with specified width and height.
         *
         * @param width  Width of the image
         * @param height Height of the image
         * @param data   Pixel data of the image
         */
        override fun uploadData(width: Int, height: Int, data: ByteBuffer) {
            when (this.channels) {
                4 -> {
                    uploadData(GL_RGBA8, width, height, GL_RGBA, data)
                }
                3 -> {
                    uploadData(GL_RGB8, width, height, GL_RGB, data)
                }
                1 -> {
                    uploadData(GL_RED, width, height, GL_RED, data)
                }
            }
        }


        /**
         * This should bind a texture for rendering
         */
        override fun bind(slotIn: Int) {
            glActiveTexture(GL_TEXTURE0 + slotIn)
            val slot = if (slotIn == -1) this.slot else slotIn
            glBindTexture(GL_TEXTURE_2D, textureID)
            glBindTextureUnit(slot, textureID)
        }

        /**
         * This should dispose of the asset instance
         */
        override fun dispose() {
            glDeleteTextures(textureID)
        }

    }
}