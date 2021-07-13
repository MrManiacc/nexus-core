package nexus.engine.render.framebuffer

/**
 * This is used to specify exactly what we want from our framebuffer. It allows us to get very specific because framebuffers
 * are a very critical part of the render api, this should be very easy to customize. This specification should allow
 * for many default values but should also allow for overridable values.
 */
data class FramebufferSpecification(
    var width: Int,
    var height: Int,
    //How many times we should use this for rendering
    val samples: Int = 1,
    //Create a *fake* framebuffer. This allows for render passes. When true, this goes to the screen.
    val swapChainTarget: Boolean = false,
    //This is a the format of the framebuffer, meaning it's the type, aka a depth, color etc.
    val format: FramebufferFormat = FramebufferFormat(),
)