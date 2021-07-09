package nexus.plugins.opengl

/*
 * Framebuffer Objects are OpenGL Objects, which allow for the creation of user-defined Framebuffers.
 * With them, one can render to non-Default Framebuffer locations, and thus render without disturbing the main screen.
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
class GLFramebuffer {



}