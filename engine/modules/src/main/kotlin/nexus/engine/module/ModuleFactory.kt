package nexus.engine.module

import com.google.common.base.Charsets
import com.google.common.base.Joiner
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.reflect.Reflection
import nexus.engine.module.ex.InvalidModulePathException
import nexus.engine.module.ex.MissingModuleMetadataException
import org.reflections.Configuration
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.serializers.JsonSerializer
import org.reflections.serializers.Serializer
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.io.*
import java.net.MalformedURLException
import java.util.*
import java.util.regex.Pattern
import java.util.zip.ZipFile

/**
 * This handles all of the internals related to modules. IT can load and create, handle classes resources,
 * handle plugin files etc.
 */
open class ModuleFactory @JvmOverloads constructor(
    private val classLoader: ClassLoader = ClasspathHelper.contextClassLoader(),
    defaultCodeSubpath: String = STANDARD_CODE_SUBPATH,
    defaultLibsSubpath: String = STANDARD_LIBS_SUBPATH,
    metadataLoaders: Map<String, ModuleMetadataLoader> = ImmutableMap.of("module.yaml",
        ModuleMetadataYamlAdapater()),
) {
    private val moduleMetadataLoaderMap: MutableMap<String, ModuleMetadataLoader> = Maps.newLinkedHashMap()
    private val manifestSerializersByFilename: MutableMap<String, Serializer> =
        Maps.newLinkedHashMap()
    private var defaultCodeSubpath: String
    private var defaultLibsSubpath: String
    private var scanningForClasses = true

    /**
     * @param defaultCodeSubpath The default subpath in a path module that contains code (compiled class files)
     * @param defaultLibsSubpath The default subpath in a path module that contains libraries (jars)
     */
    constructor(defaultCodeSubpath: String, defaultLibsSubpath: String) : this(ClasspathHelper.contextClassLoader(),
        defaultCodeSubpath,
        defaultLibsSubpath,
        ImmutableMap.of<String, ModuleMetadataLoader>("module.yaml", ModuleMetadataYamlAdapater())) {
    }

    /**
     * @return The subpath of a path module that contains compiled code
     */
    fun getDefaultCodeSubpath(): String {
        return defaultCodeSubpath
    }

    /**
     * Sets the default subpath for code in a path module
     *
     * @param defaultCodeSubpath The default code subpath
     */
    fun setDefaultCodeSubpath(defaultCodeSubpath: String) {
        this.defaultCodeSubpath = defaultCodeSubpath
    }

    /**
     * @return Whether the module factory will scan modules for class files if a manifest isn't available
     */
    fun isScanningForClasses(): Boolean {
        return scanningForClasses
    }

    /**
     * @param scanForClasses Whether the module factory should scan modules for class files if a manifest isn't present
     */
    fun setScanningForClasses(scanForClasses: Boolean) {
        scanningForClasses = scanForClasses
    }

    /**
     * @return The subpath of a path module that contains libraries
     */
    fun getDefaultLibsSubpath(): String {
        return defaultLibsSubpath
    }

    /**
     * Sets the default subpath for libraries in a path module
     *
     * @param defaultLibsSubpath The default libs subpath
     */
    fun setDefaultLibsSubpath(defaultLibsSubpath: String) {
        this.defaultLibsSubpath = defaultLibsSubpath
    }

    /**
     * Adds a deserializer for a manifest file.
     *
     * @param name         The name of the manifest file this loader will load
     * @param deserializer The deserializer
     */
    fun setManifestFileType(name: String, deserializer: Serializer) {
        manifestSerializersByFilename[name] = deserializer
    }

    /**
     * @return The map of paths to module metadata loaders used for loading metadata describing modules
     */
    fun getModuleMetadataLoaderMap(): MutableMap<String, ModuleMetadataLoader> {
        return moduleMetadataLoaderMap
    }

    private fun scanOrLoadClasspathReflections(packagePath: String): Reflections {
        var path = packagePath
        if (!path.isEmpty() && !path.endsWith("/")) {
            path += "/"
        }
        val manifest = Reflections(EMPTY_CONFIG)
        try {
            var loaded = false
            for ((key, value) in manifestSerializersByFilename) {
                val resources = classLoader.getResources(path + key)
                while (resources.hasMoreElements()) {
                    resources.nextElement().openStream().use { stream ->
                        manifest.merge(value.read(stream))
                        loaded = true
                    }
                }
            }
            if (!loaded && scanningForClasses) {
                val config: Configuration =
                    ConfigurationBuilder().addScanners(ResourcesScanner(),
                        SubTypesScanner(false),
                        TypeAnnotationsScanner()).addClassLoader(classLoader).forPackages(packagePath)
                val reflections = Reflections(config)
                manifest.merge(reflections)
            }
        } catch (e: IOException) {
            logger.error("Failed to gather class manifest for classpath module", e)
        }
        return manifest
    }

    private fun scanOrLoadDirectoryManifest(directory: File): Reflections {
        val manifest = Reflections(EMPTY_CONFIG)
        try {
            var loaded = false
            for ((key, value) in manifestSerializersByFilename) {
                val manifestFile = File(directory, key)
                if (manifestFile.exists() && manifestFile.isFile) {
                    FileInputStream(manifestFile).use { stream ->
                        manifest.merge(value.read(stream))
                        loaded = true
                    }
                }
            }
            if (!loaded) {
                scanContents(directory, manifest)
            }
        } catch (e: IOException) {
            logger.error("Failed to gather class manifest for classpath module", e)
        }
        return manifest
    }

    @Throws(MalformedURLException::class) private fun scanContents(directory: File, manifest: Reflections) {
        if (scanningForClasses) {
            val config: Configuration =
                ConfigurationBuilder().addScanners(ResourcesScanner(), SubTypesScanner(false), TypeAnnotationsScanner())
                    .addUrls(directory.toURI().toURL())
            val reflections = Reflections(config)
            manifest.merge(reflections)
        }
    }

    private fun scanOrLoadArchiveManifest(archive: File): Reflections {
        val manifest = Reflections(EMPTY_CONFIG)
        try {
            var loaded = false
            ZipFile(archive).use { zipFile ->
                for ((key, value) in manifestSerializersByFilename) {
                    val modInfoEntry = zipFile.getEntry(key)
                    if (modInfoEntry != null && !modInfoEntry.isDirectory) {
                        zipFile.getInputStream(modInfoEntry).use { stream ->
                            manifest.merge(value.read(stream))
                            loaded = true
                        }
                    }
                }
            }
            if (!loaded) {
                scanContents(archive, manifest)
            }
        } catch (e: IOException) {
            logger.error("Failed to gather class manifest for classpath module", e)
        }
        return manifest
    }

    /**
     * Creates a module from a package on the main classpath.
     *
     * @param packageName The package to create the module from, as a list of the parts of the package
     * @return A module covering the contents of the package on the classpath
     */
    fun createPackageModule(packageName: String): Module {
        var metadata: ModuleMetadata? = null
        val packageParts = Arrays.asList(*packageName.split(Pattern.quote(".")).toTypedArray())
        for ((key, value) in moduleMetadataLoaderMap) {
            val metadataResource = RESOURCE_PATH_JOINER.join(packageParts) + "/" + key
            val metadataStream = classLoader.getResourceAsStream(metadataResource)
            if (metadataStream != null) {
                try {
                    InputStreamReader(metadataStream).use { reader ->
                        metadata = value.read(reader)
                        return@use
                    }
                } catch (e: IOException) {
                    logger.error("Failed to read metadata resource {}", metadataResource, e)
                }
            }
        }
        if (metadata != null) {
            return createPackageModule(metadata!!, packageName)
        }
        throw MissingModuleMetadataException("Missing or failed to load metadata for package module $packageName")
    }

    /**
     * Creates a module from a package on the main classpath.
     *
     * @param metadata    The metadata describing the module
     * @param packageName The package to create the module from, as a list of the parts of the package
     * @return A module covering the contents of the package on the classpath
     */
    fun createPackageModule(metadata: ModuleMetadata, packageName: String): Module {
        val packageParts = Arrays.asList(*packageName.split(Pattern.quote(".")).toTypedArray())
        val manifest: Reflections = scanOrLoadClasspathReflections(RESOURCE_PATH_JOINER.join(packageParts))
        return Module(
            metadata = metadata,
            moduleManifest = manifest,
            resources = ClasspathFileSource(manifest, RESOURCE_PATH_JOINER.join(packageParts), classLoader),
            classpaths = emptyList(),
            classPredicate = { x ->
                @Suppress("UnstableApiUsage") val classPackageName: String = Reflection.getPackageName(x.java)
                packageName == classPackageName || classPackageName.startsWith("$packageName.")
            })
    }

    /**
     * Creates a module from a directory.
     *
     * @param directory The directory to load as a module
     * @return A module covering the contents of the directory
     *
     * @throws IOException if no module metadata cannot be resolved or loaded
     */
    @Throws(IOException::class) fun createDirectoryModule(directory: File): Module {
        for ((key, value) in moduleMetadataLoaderMap) {
            val modInfoFile = File(directory, key)
            if (modInfoFile.exists()) {
                try {
                    BufferedReader(InputStreamReader(FileInputStream(modInfoFile), Charsets.UTF_8)).use { reader ->
                        return createDirectoryModule(value.read(reader),
                            directory)
                    }
                } catch (e: IOException) {
                    logger.error("Error reading module metadata", e)
                }
            }
        }
        throw IOException("Could not resolve module metadata for module at $directory")
    }

    /**
     * Creates a module from a directory.
     *
     * @param metadata  The metadata describing the module
     * @param directory The directory to load as a module
     * @return A module covering the contents of the directory
     */
    open fun createDirectoryModule(metadata: ModuleMetadata, directory: File): Module {
        Preconditions.checkArgument(directory.isDirectory)
        val manifest = Reflections(EMPTY_CONFIG)
        val codeLocations: MutableList<File> = Lists.newArrayList()
        val codeDir = File(directory, getDefaultCodeSubpath())
        if (codeDir.exists() && codeDir.isDirectory) {
            codeLocations.add(codeDir)
            manifest.merge(scanOrLoadDirectoryManifest(codeDir))
        }
        val libDir = File(directory, getDefaultLibsSubpath())
        if (libDir.exists() && libDir.isDirectory && libDir.listFiles() != null) {
            val libDirContents = libDir.listFiles()
            if (libDirContents != null) {
                for (lib in libDirContents) {
                    if (lib.isFile) {
                        codeLocations.add(lib)
                        manifest.merge(scanOrLoadArchiveManifest(lib))
                    }
                }
            }
        }
        return Module(
            metadata = metadata,
            resources = DirectoryFileSource(directory),
            classPredicate = { x -> false },
            moduleManifest = manifest,
            classpaths = codeLocations
        )
    }

    /**
     * Loads an archive (zip) module. This module may contain compiled code (e.g. could be a jar).
     *
     * @param archive The archive file
     * @return The loaded module
     * @throws IOException If there is an issue loading the module
     */
    @Throws(IOException::class) fun createArchiveModule(archive: File): Module {
        ZipFile(archive).use { zipFile ->
            for ((key, value) in moduleMetadataLoaderMap) {
                val modInfoEntry = zipFile.getEntry(key)
                if (modInfoEntry != null) {
                    InputStreamReader(zipFile.getInputStream(modInfoEntry),
                        Charsets.UTF_8).use { reader ->
                        return try {
                            val metadata = value.read(reader)
                            createArchiveModule(metadata, archive)
                        } catch (e: Exception) {
                            throw IOException("Failed to read metadata for module $archive", e)
                        }
                    }
                }
            }
        }
        throw IOException("Missing module metadata in archive module '$archive'")
    }

    /**
     * Loads an archive (zip) module. This module may contain compiled code (e.g. could be a jar).
     *
     * @param metadata The metadata describing the module
     * @param archive  The archive file
     * @return The loaded module
     * @throws IOException If there is an issue loading the module
     */
    @Throws(IOException::class) open fun createArchiveModule(metadata: ModuleMetadata, archive: File): Module {
        Preconditions.checkArgument(archive.isFile)
        return try {
            Module(
                metadata = metadata,
                resources = ArchiveFileSource(archive),
                classpaths = listOf(archive),
                moduleManifest = scanOrLoadArchiveManifest(archive),
                classPredicate = { x -> false })
        } catch (e: MalformedURLException) {
            throw InvalidModulePathException("Unable to convert file path to url for $archive", e)
        }
    }

    /**
     * Creates a module for a path, which can be a directory or an archive (zip/jar) file
     *
     * @param path     The path to create a module for.
     * @param metadata The metadata describing the module.
     * @return The new module.
     * @throws IOException if there is an issue reading the module
     */
    @Throws(IOException::class) fun createModule(metadata: ModuleMetadata, path: File): Module {
        return if (path.isDirectory) {
            createDirectoryModule(metadata, path)
        } else {
            createArchiveModule(metadata, path)
        }
    }

    /**
     * Creates a module from a path, determining whether it is an archive (jar or zip) or directory and handling it appropriately. A module metadata file will be loaded and
     * to determine the module's id, version and other details.
     *
     * @param path The path locating the module
     * @return The loaded module
     * @throws IOException If the module fails to load, including if the module metadata file cannot be found or loaded.
     */
    @Throws(IOException::class) fun createModule(path: File): Module {
        Preconditions.checkArgument(path.exists())
        return if (path.isDirectory) {
            createDirectoryModule(path)
        } else {
            createArchiveModule(path)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModuleFactory::class.java)
        private val RESOURCE_PATH_JOINER = Joiner.on('/')
        private val EMPTY_CONFIG: Configuration = ConfigurationBuilder()
        private const val STANDARD_CODE_SUBPATH = "build/classes"
        private const val STANDARD_LIBS_SUBPATH = "libs"
    }
    /**
     * @param classLoader        The classloader that modules should be loaded atop of
     * @param defaultCodeSubpath The default subpath in a path module that contains code (compiled class files)
     * @param defaultLibsSubpath The default subpath in a path module that contains libraries (jars)
     * @param metadataLoaders    A map of relative paths/files to metadata loaders to use for loading module metadata
     */
    /**
     * @param classLoader The classloader to use for classpath and package modules
     */
    init {
        moduleMetadataLoaderMap.putAll(metadataLoaders)
        this.defaultCodeSubpath = defaultCodeSubpath
        this.defaultLibsSubpath = defaultLibsSubpath
        manifestSerializersByFilename["manifest.json"] = JsonSerializer()
    }
}
