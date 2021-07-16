package nexus.engine.render.framebuffer

/**
 * Alllows for multiple attachments
 */
class FramebufferFormat(private vararg val attachments: Attachment) {

    /**
     * Image implies that we want to sample the given attachment, while
     * buffer implies that it's simply for storage like a depth buffer storage will never really be read from
     * in theory, while it's still more practical in real life to do so.
     */
    enum class Attachment {
        None, ColorImage, ColorBuffer, DepthBuffer, DepthImage
    }

    /**
     * Checks if this format has the given attachemnt
     */
    fun has(attachment: Attachment): Boolean {
        for (att in attachments)
            if (att == attachment) return true
        return false
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FramebufferFormat

        if (!attachments.contentEquals(other.attachments)) return false

        return true
    }

    override fun hashCode(): Int {
        return attachments.contentHashCode()
    }


}