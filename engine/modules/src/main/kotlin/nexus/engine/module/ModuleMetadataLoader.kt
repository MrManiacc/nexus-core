package nexus.engine.module

import java.io.IOException
import java.io.Reader


/**
 * A module metadata loader reads module metadata from a file.
 */
interface ModuleMetadataLoader {
    /**
     * @param reader Metadata to load
     * @return The loaded module metadata.
     * @throws IOException If there was an error reading the ModuleMetadata
     */
    @Throws(IOException::class) fun read(reader: Reader): ModuleMetadata
}
