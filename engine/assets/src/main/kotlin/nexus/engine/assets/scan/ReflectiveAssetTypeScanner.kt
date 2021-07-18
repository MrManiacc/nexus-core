package nexus.engine.assets.scan

import mu.KotlinLogging
import nexus.engine.assets.*
import nexus.engine.assets.format.AssetFileFormat
import org.reflections8.Reflections
import org.reflections8.scanners.TypeAnnotationsScanner
import kotlin.reflect.KClass

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
        val reflections = Reflections(TypeAnnotationsScanner()).getTypesAnnotatedWith(RegisterAssetType::class.java)
        // iterate each instance of asset class
        reflections.forEach {
            val annotation = it.getAnnotation(RegisterAssetType::class.java)
            if (Asset::class.java.isAssignableFrom(it)) {
                val assetClass = it as KClass<out Asset<out AssetData>>//TODO check the saftey of this
                val factoryClass =
                    annotation.factory as KClass<out AssetFactory<out Asset<out AssetData>, out AssetData>>//TODO check the saftey of this
                val formatClass = it as KClass<out AssetFileFormat<out AssetData>>//TODO check the saftey of this
                val assetType: AssetType<out Asset<out AssetData>, out AssetData> =
                    AssetType(assetClass, factoryClass, formatClass) //This creates an unsafe instance of the asset type
                result.add(assetType)
                logger.info { "Automatically created and queued for registration AssetType=${assetClass.qualifiedName}, factory=${factoryClass.qualifiedName}, format=${formatClass.qualifiedName}" }
            }
        }
        return result
    }
}