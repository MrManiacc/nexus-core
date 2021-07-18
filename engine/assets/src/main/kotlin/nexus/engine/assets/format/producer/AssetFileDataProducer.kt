package nexus.engine.assets.format.producer

import mu.KotlinLogging
import nexus.engine.assets.AssetData
import nexus.engine.assets.format.FileChangeListener
import nexus.engine.module.resources.FileReference
import nexus.engine.module.Name
import nexus.engine.resource.ResourceUrn
import java.util.*
import javax.annotation.concurrent.ThreadSafe

/**
 * ModuleAssetDataProducer produces asset data from files within modules. In addition to files defining assets, it supports
 * files that override or alter assets defined in other modules, files redirecting a urn to another urn, and the ability
 * to make modifications to asset files in the file system that can be detected and used to reload assets.
 * <p>
 * ModuleAssetDataProducer does not discover files itself. Available files (and changes and removals of available files) should be provided through the {@link FileChangeSubscriber}
 * interface. The available files can also be cleared using {@link #clearAssetFiles()}
 * </p>
 * <p>
 * ModuleAsstDataProducer supports five types of files:
 * </p>
 * <ul>
 * <li>Asset files. These correspond to an AssetFileFormat, and provide the core data for an asset. They are
 * expected to be found under the /assets/<b>folderName</b> directory of modules.</li>
 * <li>Asset Supplementary files. These correspond to an AssetAlterationFileFormat, and provide additional data for an
 * asset. They are expected to be found under the /assets/<b>folderName</b> directory of modules. Supplementary formats
 * can be used by assets of any format - for instance a texture may support both png and jpg formats, and for either a
 * .info file could be provided with additional metadata.</li>
 * <li>Asset redirects. These are used to indicate a urn should be resolved to another urn. These are intended to support
 * assets being renamed or deleted. They are simple text containing the urn to redirect to, with a name corresponding to
 * a urn and a .redirect extension that contain the urn to use instead.
 * Like asset files, they are expected to be found under the /assets/<b>folderName</b> directory of modules.</li>
 * <li>Asset deltas. These are found under /deltas/<b>moduleName</b>/<b>folderName</b>, and provide changes to assets from
 * other modules. An AssetAlterationFileFormat is used to load them.</li>
 * <li>Asset overrides. These are found under /overrides/<b>moduleName</b>/<b>folderName</b>, and replace completely
 * the data of an asset from another module. All the asset formats and asset supplementary formats are used to load these.</li>
 * </ul>
 * <p>
 * When the data for an asset is requested, ModuleAssetDataProducer will return the data using all of the relevant files across
 * all modules.
 * </p>
 *
 * 
 */
@ThreadSafe
class AssetFileDataProducer<U : AssetData> : RedirectableAssetDataProducer<U>, FileChangeListener {
    private val logger = KotlinLogging.logger { }


    /**
     * This should be used to generate/load asset data for a given urn. This allows us to ability of using kotlins nice
     * unit's for easy creation of asset data. This allow for matching of certain types based upon the [urn]
     *
     * @param urn The urn to get AssetData for
     * @return An optional with the AssetData, if available
     * @throws IOException If there is an error producing the AssetData.
     */
    override fun produceData(urn: ResourceUrn): Optional<U> {
        TODO("Not yet implemented")
    }

    /**
     * Notification that an asset file was added
     *
     * @param file            The asset file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the asset file
     * @return The ResourceUrn of the resource the file contributes too, if any
     */
    override fun assetFileAdded(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn> {
        TODO("Not yet implemented")
    }

    /**
     * Notification that an asset file was modified
     *
     * @param file            The asset file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the asset file
     * @return The ResourceUrn of the resource the file contributes too, if any
     */
    override fun assetFileModified(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn> {
        TODO("Not yet implemented")
    }

    /**
     * Notification that an asset file was removed
     *
     * @param file            The asset file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the asset file
     * @return The ResourceUrn of the resource the file contributed too, if any
     */
    override fun assetFileDeleted(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn> {
        TODO("Not yet implemented")
    }

    /**
     * Notification that an delta file was added
     *
     * @param file            The delta file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the delta file
     * @return The ResourceUrn of the resource the file contributes too, if any
     */
    override fun deltaFileAdded(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn> {
        TODO("Not yet implemented")
    }

    /**
     * Notification that an delta file was modified
     *
     * @param file            The delta file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the delta file
     * @return The ResourceUrn of the resource the file contributes too, if any
     */
    override fun deltaFileModified(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn> {
        TODO("Not yet implemented")
    }

    /**
     * Notification that an delta file was removed
     *
     * @param file            The delta file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the delta file
     * @return The ResourceUrn of the resource the file contributed too, if any
     */
    override fun deltaFileDeleted(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn> {
        TODO("Not yet implemented")
    }

    /**
     * Removes all of the asset's files.
     */
    fun clearAssetFiles() {
    }
}