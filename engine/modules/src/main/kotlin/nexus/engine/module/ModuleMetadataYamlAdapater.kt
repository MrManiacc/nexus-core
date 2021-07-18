package nexus.engine.module

import java.io.Reader

/**
 * This reads a given modules metadata from a [Reader]
 */
class ModuleMetadataYamlAdapater : ModuleMetadataLoader {
    /**
     * @param reader Metadata to load
     * @return The loaded module metadata.
     * @throws IOException If there was an error reading the ModuleMetadata
     */
    override fun read(reader: Reader): ModuleMetadata {
        TODO("Not yet implemented")
    }

}
