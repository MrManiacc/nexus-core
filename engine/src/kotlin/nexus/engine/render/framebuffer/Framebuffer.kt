package nexus.engine.render.framebuffer

import java.util.*

/**
 * This is the core framebuffer class. This allows for all kinds of fancy off screen rendering magic bullshit.
 */
abstract class Framebuffer(protected val spec: FramebufferSpecification) : Collection<Framebuffer.Attachment> {
    var renderID: Int = -1
        protected set

    /**
     * This stores all of the attachments after their creation according to the [FramebufferFormat].
     */
    private val attachments: EnumMap<FramebufferFormat.Attachment, Attachment> =
        EnumMap(FramebufferFormat.Attachment::class.java)


    operator fun set(key: FramebufferFormat.Attachment, attachment: Attachment) {
        attachments[key] = attachment
    }

    operator fun get(key: FramebufferFormat.Attachment): Attachment? {
        return attachments[key]
    }

    abstract fun bind()

    abstract fun unbind()

    /**
     * This should force the framebuffer to clean it's self up. This means the state is not valid and that
     * we should recreate the framebuffer
     */
    abstract fun invalidate()

    /**
     * This allows for attachment based buffers
     */
    abstract class Attachment(val attachment: FramebufferFormat.Attachment) {
        var renderID: Int = -1
            protected set

        var isAttached: Boolean = false
            protected set


        abstract fun attach(): Attachment

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Attachment

            if (attachment != other.attachment) return false

            return true
        }

        override fun hashCode(): Int {
            return attachment.hashCode()
        }
    }

    /**
     * Returns the size of the collection.
     */
    override val size: Int
        get() = attachments.size

    /**
     * Checks if the specified element is contained in this collection.
     */
    override fun contains(element: Attachment): Boolean {
        return attachments.containsValue(element)
    }

    /**
     * Checks if all elements in the specified collection are contained in this collection.
     */
    override fun containsAll(elements: Collection<Attachment>): Boolean {
        return attachments.values.containsAll(elements)
    }

    /**
     * Returns `true` if the collection is empty (contains no elements), `false` otherwise.
     */
    override fun isEmpty(): Boolean {
        return attachments.isEmpty()
    }

    override fun iterator(): Iterator<Attachment> {
        return attachments.values.iterator()
    }

}