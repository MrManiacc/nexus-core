package nexus.engine.assets.texture

import nexus.engine.assets.old.AssetTypeOld
import kotlin.reflect.KClass

/**
 * This is the base texture for the engine. It should be implemented per render api.
 */
interface Texture : AssetTypeOld<Texture, TextureData> {
    override val type: KClass<TextureInstance> get() = TextureInstance::class
}


