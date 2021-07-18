package nexus.engine.assets.scan

import nexus.engine.assets.AssetType

/**
 * This should collect and generate all of the assets
 */
fun interface AssetTypeScanner {
    /**
     * This should collect all of the unregistered asset types on the classpath.
     */
    fun collect(): List<AssetType<*, *>>


}