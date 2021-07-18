

plugins {
    id("build-plugin")
}

/**
 * This is used to configure our plugin
 */
nexus {
    configuration {
        module(":engine")
    }

    extend(project(":engine"), "core", "lwjgl")
}