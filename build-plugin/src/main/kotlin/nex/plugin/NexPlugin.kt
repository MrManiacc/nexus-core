package nex.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class NexPlugin : Plugin<Project> {
    /**
     * Apply this plugin to the given target object.
     *
     * @param target The target object
     */
    override fun apply(target: Project) {
        target.extensions.create("nexus", PluginExtension::class.java, target)

        target.afterEvaluate {
            val extension = this.nexus ?: return@afterEvaluate
            extension.evaluateExtension(target)
        }
    }


}