@file:Suppress("MemberVisibilityCanBePrivate", "SimpleRedundantLet")

package nex.plugin

import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.project
import java.io.File
import java.nio.file.Path
import javax.inject.Inject
import org.gradle.api.tasks.SourceSet as GradleSourceSet

/**
 * This allows you to configure the plugin
 */
open class PluginExtension @Inject constructor(internal val project: Project) {
    internal val configurationManager = ConfigurationManager(project)
    internal val baseConfiguration = Configuration(project, "_base_")
    internal val extends = Extends(this)
    internal val props = Props(this)
    var id: String = project.displayName
    var version: String = project.version.toString()
    fun extends(extends: Extends.() -> Unit) =
        this.extends.apply(extends)

    /**
     * This will extend a configuration from within the location pluginExtension
     */
    fun extend(configurationName: String) {
        configurationManager.ifPresent(configurationName) {
            it.addTo(this)
        }
    }


    /**
     * This will cascade down all of this projects [Extends] and it's other extension data like repos etc
     */
    fun extend(project: Project, vararg configurationName: String) =
        configurationName.forEach { extends.extend(project, it) }


    /**
     * This will cascade down all of this projects [Extends] and it's other extension data like repos etc
     */
    fun extend(project: Project) = project.nexus?.baseConfiguration?.addTo(this)


    fun props(props: Props.() -> Unit) = this.props.apply(props)

    fun configuration(name: String? = null, config: Configuration.() -> Unit) {
        if (name != null) {
            val configuration = configurationManager[name]//this will auto compute if absent
            configuration.apply(config).applyProps(props)
        } else
            this.baseConfiguration.apply(config).applyProps(props)
    }


}

data class SourceSet(
    private val project: Project
) {
    internal val sourceInputs = ArrayList<Source>()
    internal val resourceInputs = ArrayList<Source>()
    internal val resourceOutputs = ArrayList<Source>()

    fun source(source: String, relativeToProject: Boolean = true): Unit = sourceInputs.let { input ->
        input.add(Source(source, relativeToProject))
    }

    fun resource(source: String, relativeToProject: Boolean = true): Unit = resourceInputs.let { input ->
        input.add(Source(source, relativeToProject))
    }


    fun compilation(source: String, relative: Boolean = true): Unit = resourceOutputs.let { input ->
        input.add(Source(source, relative))
    }

    fun addToo(project: Project, main: GradleSourceSet) {
        val sources = sourceInputs.map { getPath(it.path, it.relativeToProject, project) }
        val resources = resourceInputs.map { getPath(it.path, it.relativeToProject, project) }
        main.java.setSrcDirs(sources)
        main.resources.setSrcDirs(resources)
    }

    /**
     * This should resolve the path relative to the [project].
     * when [relativeToProject] is true (default behavior), we offset using the extensions project
     * as the base. When false, we should make it relative to the root directory of the root project.
     *
     */
    internal fun getPath(source: String, relativeToProject: Boolean, project: Project): File {
        val path = source.replace("/", File.separator).replace("\\", File.separator)
        return java.io.File(if (relativeToProject) project.projectDir else project.rootDir, path)
    }

    fun addTo(sources: SourceSet) {
        sources.sourceInputs.addAll(this.sourceInputs)
        sources.resourceInputs.addAll(this.resourceInputs)
        sources.resourceOutputs.addAll(this.resourceOutputs)
    }

    override fun toString(): String {
        return "SourceSet(sourceInputs=$sourceInputs, resourceInputs=$resourceInputs, classOutputs=$resourceOutputs)"
    }

}

data class Source(val path: String, val relativeToProject: Boolean) {
    /**
     * This should resolve the path relative to the [project].
     * when [relativeToProject] is true (default behavior), we offset using the extensions project
     * as the base. When false, we should make it relative to the root directory of the root project.
     *
     */
    internal fun getPath(project: Project, relativeToProject: Boolean = true): Path {
        val path = this.path.replace("/", File.separator).replace("\\", File.separator)
        val file = File(if (relativeToProject) project.projectDir else project.rootDir, path)
        return file.toPath()
    }
}


data class ConfigurationManager(
    val project: Project,
    val dependencyGroups: MutableMap<String, Configuration> = HashMap()
) {
    operator fun get(key: String): Configuration = dependencyGroups.computeIfAbsent(key) { Configuration(project, key) }
    operator fun set(key: String, value: Configuration) {
        dependencyGroups[key] = value
    }

    fun contains(key: String) = dependencyGroups.containsKey(key)


    fun ifPresent(key: String, block: (Configuration) -> Unit) {
        if (contains(key)) //Prevent auto creation and unneeded memory leaks
            block(get(key))
    }

}


class Props(private val extension: PluginExtension) {
    private val properties: MutableMap<String, Any> = HashMap()


    operator fun set(key: String, value: Any) {
        this.properties[key] = value
    }

    operator fun get(key: String) = properties[key]
    fun contains(key: String) = properties.contains(key)


    fun updateString(string: String): String {
        var stringOut = string
        properties.forEach {
            if (stringOut.contains("@${it.key}"))
                stringOut = stringOut.replace("@${it.key}", it.value.toString())
        }
        return stringOut
    }


}

class Extends(private val extension: PluginExtension) {

    /**
     * This will cascade down all of this projects [Extends] and it's other extension data like repos etc
     */
    fun extend(project: Project, configurationName: String) {
        val ext = getExtension(project)
        ext.configurationManager.ifPresent(configurationName) {
            it.addTo(extension)
        }
    }

    /**
     * This will extend a configuration from within the location pluginExtension
     */
    fun extend(configurationName: String) = extension.extend(configurationName)


    private fun getExtension(project: Project): PluginExtension {
        val extension =
            project.extensions["nexus"]
        if (extension !is PluginExtension) error("Failed to find the nexus extension for project: ${project.name}")
        return extension
    }
}


/**
 * A dependency set is something that allows another module to extend specifically
 */
data class Configuration(val project: Project, val name: String) {
    internal val repos: MutableList<Repo> = ArrayList()
    internal val dependencies: MutableSet<Dependency> = HashSet()
    internal val sources = SourceSet(project)
    internal val plugins: ArrayList<GradlePlugin> = ArrayList()
    fun maven() = repo("https://repo1.maven.org/maven2/")
    fun google() = repo("https://maven.google.com/")
    fun jcenter() = repo("https://jcenter.bintray.com")
    fun repo(repository: String) = repo(Repo(repository))
    fun repo(repository: Repo) = repos.add(repository)


    fun dependency(value: Dependency) = dependencies.add(value)
    fun implementation(value: Dependency) = dependency(value)
    fun implementation(value: String) = implementation(Implementation(value))

    fun module(value: String) = dependency(ProjectImplementation(value))

    fun platform(value: Dependency) = dependency(value)
    fun platform(value: String) = platform(PlatformImplementation(value))

    fun runtimeOnly(value: Dependency) = dependency(value)
    fun runtimeOnly(value: String) = runtimeOnly(RuntimeOnly(value))


    /**
     * Adds all of the [config] to the other repository
     */
    fun addTo(config: Configuration) = this.repos.forEach { config.repo(it) }
        .also { config.dependencies.addAll(this.dependencies) }
        .also { this.sources.addTo(config.sources) }
        .also { config.plugins.addAll(this.plugins) }

    fun addTo(pluginExtension: PluginExtension) = addTo(pluginExtension.baseConfiguration)

    /**
     * This will update all of the [repos], [implementations], and [runtimes]
     * to reflect the variables inside the props
     */
    fun applyProps(properties: Props) {
        this.repos.forEach {
            it.repoUrl = properties.updateString(it.repoUrl)
        }

        this.dependencies.forEach {
            it.dependencyPath = properties.updateString(it.dependencyPath)
        }
    }

    fun plugin(plugin: String, version: String? = null) {
        plugins.add(GradlePlugin(plugin, version))
    }


    fun sources(config: SourceSet.() -> Unit) {
        sources.apply(config)
    }
}

data class GradlePlugin(val name: String, val version: String? = null) {

}

data class Repo(var repoUrl: String) {
    fun addTo(project: Project) {
        project.repositories.maven(repoUrl)
    }
}

interface Dependency {
    val dependencyGroup: String
    var dependencyPath: String
    val isPlatform: Boolean get() = false
    val isProject: Boolean get() = false

    /**
     *This will add the configuration to the project
     */
    fun addTo(project: Project) {
        project.configurations.getByName(dependencyGroup).dependencies.add(
            if (!isPlatform && !isProject)
                project.dependencies.add(dependencyGroup, dependencyPath)
            else if (isProject)
                project.dependencies.project(dependencyPath)
            else
                project.dependencies.platform(dependencyPath)
        )
    }
}

data class ProjectImplementation(
    override var dependencyPath: String

) : Dependency {
    override val isProject: Boolean
        get() = true
    override val dependencyGroup: String
        get() = "implementation"
}

data class Implementation(
    override var dependencyPath: String
) : Dependency {

    override val dependencyGroup: String
        get() = "implementation"
}


data class PlatformImplementation(
    override var dependencyPath: String
) : Dependency {
    override val isPlatform: Boolean
        get() = true
    override val dependencyGroup: String
        get() = "implementation"
}


data class Compile(
    override var dependencyPath: String
) : Dependency {

    override val dependencyGroup: String
        get() = "compile"
}


data class RuntimeOnly(
    override var dependencyPath: String
) : Dependency {

    override val dependencyGroup: String
        get() = "runtimeOnly"
}

val Project.nexus: PluginExtension?
    get() {
        val extension =
            project.extensions["nexus"]
        if (extension !is PluginExtension) return null
        return extension
    }
