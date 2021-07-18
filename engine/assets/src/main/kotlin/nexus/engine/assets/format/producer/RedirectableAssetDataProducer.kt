package nexus.engine.assets.format.producer

import nexus.engine.assets.AssetData
import nexus.engine.resource.ResourceUrn


/**
 * AssetDataProducers provide asset data used to produce assets.
 *
 *
 * As the source of asset data, they also play a role in resolving partial urns and redirecting urns to other assets.
 *
 *
 *
 * AssetDataProducer is closable, and should be closed when no longer in use, so that any file system handles (or similar) can be closed.
 *
 *
 * 
 */
interface RedirectableAssetDataProducer<U : AssetData> : AssetDataProducer<U> {

    /**
     * Optionally can provide a set of ResourceUrns this AssetDataProducer can provide data for.  It is not required by the asset system, and is intended only for
     * displays of available assets. If it is infeasible to provide such a list (such as when the AssetDataProducer procedurally generates assets based on part of the
     * urn) an empty set can be returned instead.
     *
     * @return A set that may contain the urns of assets this producer can provide data for.
     */
    val availableAssetUrns: Set<ResourceUrn> get() = emptySet()

    /**
     * Gives the AssetDataProducer the opportunity to "redirect" the urn to another urn. If the asset data producer does not wish to do so it should return the original
     * urn.
     *
     *
     * A redirected urn indicates a different asset should be loaded to that requested. This can be used to leave breadcrumbs as assets are renamed so that
     * dependant modules can still discover the asset with the old name.
     *
     * By default the redirection will just pass the original urn in so no redirection happens. Method may be overridden if redirection is required for an asset.
     * @param urn The urn to redirect
     * @return Either the original urn, or a urn to redirect to.
     */
    fun redirect(urn: ResourceUrn): ResourceUrn = urn


}