package nexus.plugins.opengl

import dorkbox.messageBus.annotations.Subscribe
import mu.KotlinLogging
import nexus.engine.events.Events
import nexus.engine.render.framebuffer.Framebuffer
import nexus.engine.render.framebuffer.FramebufferFormat.Attachment.ColorImage
import nexus.engine.render.framebuffer.FramebufferFormat.Attachment.DepthBuffer
import nexus.engine.render.framebuffer.FramebufferSpecification
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL45
import org.lwjgl.opengl.GL45.*
import org.slf4j.Logger

/*
 * Framebuffer Objects are OpenGL Objects, which allow for the creation of user-defined Framebuffers.
 * With them, one can nexus.engine.render to non-Default Framebuffer locations, and thus nexus.engine.render without disturbing the main screen.
 *
 * Framebuffer objects are a collection of attachments. To help explain lets explicitly define certain terminology.
 * 1. Image->
 * An image is a single 2D array of pixels. It has a specific format for these pixels.
 * 2. Layered Image->
 * A layered image is a sequence of images of a particular size and format.
 * Layered images come from single mipmap levels of certain Texture types.
 * 3. Texture->
 * A texture is an object that contains some number of images, as defined above. All of the images have the same format, but they do not have to have the same size (different mip-maps, for example). Textures can be accessed from Shaders via various methods.
 * 4. Renderbuffer->
 * A renderbuffer is an object that contains a single image. Renderbuffers cannot be accessed by Shaders in any way. The only way to work with a renderbuffer, besides creating it, is to put it into an FBO.
 * 5. Attach->
 * To connect one object to another. This term is used across all of OpenGL, but FBOs make the most use of the concept. Attachment is different from binding. Objects are bound to the context; objects are attached to one another.
 * 6. Attachment point->
 * A named location within a framebuffer object that a framebuffer-attachable image or layered image can be attached to. Attachment points restrict the general kind of Image Format for images attached to them.
 * 7. Framebuffer-attachable image->
 * Any image, as previously described, that can be attached to a framebuffer object.
 * 8. Framebuffer-attachable layered image->
 * Any layered image, as previously described, that can be attached to a framebuffer object.
 */
class GLFramebuffer(specification: FramebufferSpecification) : Framebuffer(specification) {
    private val logger: Logger = KotlinLogging.logger { }
    override fun bind() = glBindFramebuffer(GL_FRAMEBUFFER, renderID)
    override fun unbind() = glBindFramebuffer(GL_FRAMEBUFFER, 0)


    /**
     * This should force the framebuffer to clean it's self up. This means the state is not valid and that
     * we should recreate the framebuffer
     */
    override fun invalidate() {
        if (this.renderID != -1) {
            GL30.glDeleteFramebuffers(renderID)
            renderID = -1
        }

        this.renderID = GL45.glCreateFramebuffers()
        bind()
        if (spec.format.has(ColorImage))
            this[ColorImage] = ColorAttachment(specification = spec).attach()
        if (spec.format.has(DepthBuffer))
            this[DepthBuffer] = DepthAttachment(specification = spec).attach()

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            logger.warn("Framebuffer is incomplete!")
        }
        unbind()
    }

    @Subscribe
    fun onResize(event: Events.Camera.Resize) {
        spec.width = event.width
        spec.height = event.height
        invalidate()
        println("resizing fbo: $spec")
    }

    /**
     * This provides an easy to use color attachment
     */
    class ColorAttachment(
        val internalFormat: Int = GL_RGBA8,
        val format: Int = GL_RGBA,
        val colorAttachmentSlot: Int = 0,
        val specification: FramebufferSpecification,
    ) : Attachment(ColorImage) {
        override fun attach(): Attachment {
            if (isAttached) return this
            isAttached = true
            this.renderID = glCreateTextures(GL_TEXTURE_2D)
            glBindTexture(GL_TEXTURE_2D, renderID)
            GL11.glTexImage2D(
                GL_TEXTURE_2D,
                0,
                internalFormat,
                specification.width,
                specification.height,
                0,
                format,
                GL_UNSIGNED_BYTE,
                0L
            )
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0 + colorAttachmentSlot,
                GL_TEXTURE_2D,
                renderID,
                0
            )
            glBindTexture(GL_TEXTURE_2D, 0)
            return this
        }


    }


    /**
     * This provides an easy to use color attachment
     */
    class DepthAttachment(
        val format: Int = GL_DEPTH24_STENCIL8,
        val specification: FramebufferSpecification,
    ) : Attachment(ColorImage) {
        override fun attach(): Attachment {
            this.renderID = glCreateTextures(GL_TEXTURE_2D)
            glBindTexture(GL_TEXTURE_2D, renderID)
            glTexStorage2D(GL_TEXTURE_2D, 1, format, specification.width, specification.height)
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, renderID, 0)
            glBindTexture(GL_TEXTURE_2D, 0)
            return this
        }


    }


}