package nexus.engine.assets.registry

import nexus.engine.assets.AssetData
import javax.annotation.concurrent.ThreadSafe

/**
 * Holds the details of an available but unloaded asset data. This includes all primary sources, supplements, deltas and overrides.
 * <p>The sources to use are determined when the load is requested - this allows for the sources to be added to or removed due to changes on the file system</p>
 *
 * 
 */
@ThreadSafe
class UnloadedAssetData<U : AssetData> {

}