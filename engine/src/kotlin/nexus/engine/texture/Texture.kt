package nexus.engine.texture

import nexus.engine.assets.Asset
import kotlin.reflect.KClass

/**
 * This is the base texture for the engine. It should be implemented per render api.
 */
interface Texture : Asset<Texture, TextureData> {
    override val type: KClass<TextureInstance> get() = TextureInstance::class

}


