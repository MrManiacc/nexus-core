package nexus.engine.assets

import nexus.engine.assets.registry.AssetManager
import nexus.engine.assets.registry.AssetTypeManager
import nexus.engine.assets.registry.AssetTypeManagerInternal.Companion.create

/**
 * This is used to fully interact with assets. It wraps every aspect of the asset system in a clean and concise way.
 * It allows for loading of assets, unloading of assets, instancing, etc.
 */
object Assets : AssetManager(AssetTypeManager.create()) {

}