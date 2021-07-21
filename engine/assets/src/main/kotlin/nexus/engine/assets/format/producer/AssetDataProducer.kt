package nexus.engine.assets.format.producer

import nexus.engine.assets.AssetData
import nexus.engine.module.naming.Name
import nexus.engine.module.naming.ResourceUrn

import java.io.IOException
import java.util.*

/***
 * This allows for quick and dirty asset production
 */
fun interface AssetDataProducer<U : AssetData> {
    /**
     * Optionally can provide a set of ResourceUrns this AssetDataProducer can provide data for.  It is not required by the asset system, and is intended only for
     * displays of available assets. If it is infeasible to provide such a list (such as when the AssetDataProducer procedurally generates assets based on part of the
     * urn) an empty set can be returned instead.
     *
     * @return A set that may contain the urns of assets this producer can provide data for.
     */
    val availableAssetUrns: Set<ResourceUrn> get() = emptySet()

    /**
     * Validates the resource urn against the available assets.
     */
    fun isValid(resourceUrn: ResourceUrn): Boolean = availableAssetUrns.contains(resourceUrn)

    /**
     * The names of modules for which this producer can produce asset data with the given resource name for. This is semi-optional, but required if a partial resource urn needs to be
     * resolved to an asset produced by this producer
     *
     * @param resourceName The name of a resource
     * @return A set of modules containing the resource.
     */

    fun getModulesProviding(resourceName: Name): Set<Name> = emptySet()

    /**
     * This should be used to generate/load asset data for a given urn. This allows us to ability of using kotlins nice
     * unit's for easy creation of asset data. This allow for matching of certain types based upon the [urn]
     *
     * @param urn The urn to get AssetData for
     * @return An optional with the AssetData, if available
     * @throws IOException If there is an error producing the AssetData.
     */
    @Throws(IOException::class) fun produce(urn: ResourceUrn): Optional<U>


}