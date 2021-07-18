package nexus.engine.module

import nexus.engine.module.utilities.combineToSet
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException


/**
 * A scanner for reading modules off of the filesystem. These modules may either be archives (zip or jar) or directories. To qualify as a module they must contain a
 * json module metadata file (by default named "module.txt").
 *
 * @author Immortius
 */
class ModulePathScanner {
    val moduleFactory: ModuleFactory

    constructor() {
        moduleFactory = ModuleFactory()
    }

    constructor(factory: ModuleFactory) {
        moduleFactory = factory
    }

    /**
     * Scans one or more paths for modules. Paths are scanned in order, with directories scanned before files. If a module is discovered multiple times (same id and version),
     * the first copy of the module found is used.
     *
     * @param registry        The registry to populate with discovered modules
     * @param path            The first path to scan
     * @param additionalPaths Additional paths to scan
     */
    fun scan(registry: ModuleRegistry, path: File, vararg additionalPaths: File) {
        val discoveryPaths: Set<File> = combineToSet(path, *additionalPaths)
        scan(registry, discoveryPaths)
    }

    /**
     * Scans a collection of paths for modules.
     * Paths are scanned in order, with directories scanned before files. If a module is discovered multiple times (same id and version), the first copy of the module
     * found is used.
     *
     * @param registry The registry to populate with discovered modules
     * @param paths    The paths to scan
     */
    fun scan(registry: ModuleRegistry, paths: Collection<File>) {
        for (discoveryPath in paths) {
            scanModuleDirectories(registry, discoveryPath)
            scanModuleArchives(registry, discoveryPath)
        }
    }

    /**
     * Scans a directory for module archives (jar or zip)
     *
     * @param registry      The registry to populate with discovered modules
     * @param discoveryPath The directory to scan
     */
    private fun scanModuleArchives(registry: ModuleRegistry, discoveryPath: File) {
        val files = discoveryPath.listFiles { x: File ->
            !x.isDirectory && (x.name.endsWith(".jar") || x.name.endsWith(".zip"))
        }
        if (files != null) {
            for (modulePath in files) {
                loadModule(registry, modulePath)
            }
        }
    }

    /**
     * Scans a directory for module directories
     *
     * @param registry      The registry to populate with discovered modules
     * @param discoveryPath The directory to scan
     */
    private fun scanModuleDirectories(registry: ModuleRegistry, discoveryPath: File) {
        val files = discoveryPath.listFiles { obj: File -> obj.isDirectory }
        if (files != null) {
            for (modulePath in files) {
                loadModule(registry, modulePath)
            }
        }
    }

    private fun loadModule(registry: ModuleRegistry, modulePath: File) {
        try {
            val module = moduleFactory.createModule(modulePath)
            if (registry.add(module)) {
                logger.info("Discovered module: {}", module)
            } else {
                logger.info("Discovered duplicate module: {}-{}, skipping", module.id, module.version)
            }
        } catch (e: IOException) {
            logger.warn("Failed to load module at '{}'", modulePath, e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModulePathScanner::class.java)
    }
}
