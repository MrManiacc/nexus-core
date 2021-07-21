package nexus.engine.assets.scan

import mu.KotlinLogging
import nexus.engine.assets.Asset
import nexus.engine.assets.AssetData
import nexus.engine.assets.AssetType
import nexus.engine.assets.RegisterAssetType
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.slf4j.Logger
import java.util.*

/**
 * This is used for reflectively scanning the class path using the given search paramters
 */
class ReflectiveAssetTypeScanner : AssetTypeScanner {
    private val logger = KotlinLogging.logger { }

    /**
     * This should collect all of the unregistered asset types on the classpath.
     */
    @Suppress("UNCHECKED_CAST")
    override fun collect(): List<AssetType<*, *>> {
        val result = ArrayList<AssetType<*, *>>()
        val empty: Optional<Logger> = Optional.empty()

        val reflections =
            Reflections(
                ConfigurationBuilder()
                    .addScanners(TypeAnnotationsScanner(), SubTypesScanner(false))
                    .addUrls(ClasspathHelper.forPackage("nexus")),
            ).getTypesAnnotatedWith(RegisterAssetType::class.java)

        // iterate each instance of asset class
        reflections.forEach {
            val annotation = it.getAnnotation(RegisterAssetType::class.java)
            if (Asset::class.java.isAssignableFrom(it)) {

                val assetClass = (it as Class<out Asset<out AssetData>>).kotlin//TODO check the saftey of this
                val assetType: AssetType<out Asset<out AssetData>, out AssetData> =
                    AssetType(annotation.format,
                        annotation.factory,
                        annotation.producers,
                        assetClass) //This creates an unsafe instance of the asset type
                result.add(assetType)
                logger.info { "Automatically created and queued for registration AssetType=${assetClass.qualifiedName}" }
            }
        }
        return result
    }
}