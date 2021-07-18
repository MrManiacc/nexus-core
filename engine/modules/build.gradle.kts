@file:Suppress("INACCESSIBLE_TYPE")

plugins {
    id("build-plugin")
}

/**
 * This is used to configure our plugin
 */
nexus {
    configuration {
        implementation("com.github.zafarkhaja:java-semver:0.9.0")
        implementation("com.amihaiemil.web:eo-yaml:5.2.1")
    }
    extend(project(":engine"), "core")
}