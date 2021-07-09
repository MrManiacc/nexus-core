package marx.engine.render

import com.google.common.collect.*
import mu.*
import java.lang.IllegalStateException
import kotlin.reflect.*

/*
 * This is hte core of the
 */
object Renderer {
    val renderers: MutableMap<KClass<out RenderAPI>, RenderAPI> = Maps.newHashMap()
    val log = KotlinLogging.logger { }

    /*
sets/registers [API] to the corresponding [context]
     */
    operator fun set(API: KClass<out RenderAPI>, context: RenderAPI) = context.let {
        renderers[API] = it
        log.info { "registered rendered: ${API::class.qualifiedName}" }
    }

    /*
   This will register the give api
     */
    inline fun <reified API : RenderAPI> register(renderer: API) =
        set(API::class, renderer)

    /*
[API] the render api to get the render context for
   This will get the corresponding [RenderAPI] per the given type
     */
    operator fun <API : RenderAPI> invoke(cls: KClass<API>): API {
        if (!renderers.containsKey(cls)) return casted(cls, RenderAPI.Null)
        val api = renderers[cls] ?: return casted(cls, RenderAPI.Null)
        if (!cls.isInstance(api)) return casted(cls, RenderAPI.Null)
        return casted(cls, api)
    }

    fun <API : RenderAPI> casted(cls: KClass<API>, renderAPI: RenderAPI): API =
        cls.safeCast(renderAPI) ?: throw IllegalStateException("Failed to casted render api from${renderAPI::class.qualifiedName} to ${cls.qualifiedName}")

    inline fun <reified API : RenderAPI> casted(renderAPI: RenderAPI): API = casted(API::class, renderAPI)

    /*
[API] the render api to get the render context for
   This will get the corresponding [RenderAPI] per the given type
     */
    inline operator fun <reified API : RenderAPI> invoke(): API =
        invoke(API::class)

}
