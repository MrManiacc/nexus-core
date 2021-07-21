package nexus.engine.module

import nexus.engine.module.naming.name
import nexus.engine.module.naming.version
import org.junit.jupiter.api.Test
import java.io.File

internal class ModulePathScannerTest {


    @Test
    fun `build package module and test it's sources `() {
        val env = ModuleEnvironment()
        // A module.json metadata file can also be used
        // A module.json metadata file can also be used
        val metadata = ModuleMetadata()
        metadata.id = name("Core")
        metadata.version = version("1.0.0")
        val factory = ModuleFactory()
        val result = factory.createDirectoryModule(metadata, File("/"))
        result.resources.forEach {
            println(it.path)
        }
    }
}