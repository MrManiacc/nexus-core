package nex.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.the

/**
 * This is used for actually using the data that was gather in the first faze from the extension, and
 * apply it to the project.
 */
internal fun PluginExtension.evaluateExtension(target: Project) {
    with(baseConfiguration) {
        plugins.forEach {
            target.apply(plugin = it.name)
        }
        repos.forEach {
            it.addTo(target)
        }
        dependencies.forEach {
            it.addTo(target)
        }
        updateSources(target)
    }
}


private fun Configuration.updateSources(target: Project) {
    val main = target.the<SourceSetContainer>().named("main").get()
    sources.addToo(target, main)
}

private fun preEval(target: Project) {

}
