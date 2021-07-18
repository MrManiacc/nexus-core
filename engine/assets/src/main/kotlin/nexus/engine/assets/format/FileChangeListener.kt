package nexus.engine.assets.format

import nexus.engine.module.Name
import nexus.engine.module.resources.FileReference
import nexus.engine.resource.ResourceUrn
import java.util.*

/**
 * Interface for subscribing for notification of file changes, for either asset files, or delta files.
 * <p>
 * This support asset files (which includes overrides and supplemental files) and delta files
 *
 * 
 */

interface FileChangeListener {

    /**
     * Notification that an asset file was added
     *
     * @param file            The asset file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the asset file
     * @return The ResourceUrn of the resource the file contributes too, if any
     */
    fun assetFileAdded(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn>

    /**
     * Notification that an asset file was modified
     *
     * @param file            The asset file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the asset file
     * @return The ResourceUrn of the resource the file contributes too, if any
     */
    fun assetFileModified(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn>

    /**
     * Notification that an asset file was removed
     *
     * @param file            The asset file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the asset file
     * @return The ResourceUrn of the resource the file contributed too, if any
     */
    fun assetFileDeleted(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn>

    /**
     * Notification that an delta file was added
     *
     * @param file            The delta file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the delta file
     * @return The ResourceUrn of the resource the file contributes too, if any
     */
    fun deltaFileAdded(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn>

    /**
     * Notification that an delta file was modified
     *
     * @param file            The delta file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the delta file
     * @return The ResourceUrn of the resource the file contributes too, if any
     */
    fun deltaFileModified(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn>

    /**
     * Notification that an delta file was removed
     *
     * @param file            The delta file
     * @param module          The name of the module the file is for
     * @param providingModule The name of the module providing the delta file
     * @return The ResourceUrn of the resource the file contributed too, if any
     */
    fun deltaFileDeleted(file: FileReference, module: Name, providingModule: Name): Optional<ResourceUrn>
}