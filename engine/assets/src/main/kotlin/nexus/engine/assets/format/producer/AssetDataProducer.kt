package nexus.engine.assets.format.producer

import nexus.engine.assets.AssetData
import nexus.engine.resource.ResourceUrn
import java.io.IOException
import java.util.*

/***
 * This allows for quick and dirty asset production
 */
fun interface AssetDataProducer<U : AssetData> {

    /**
     * This should be used to generate/load asset data for a given urn. This allows us to ability of using kotlins nice
     * unit's for easy creation of asset data. This allow for matching of certain types based upon the [urn]
     *
     * @param urn The urn to get AssetData for
     * @return An optional with the AssetData, if available
     * @throws IOException If there is an error producing the AssetData.
     */
    @Throws(IOException::class) fun produceData(urn: ResourceUrn): Optional<U>


}