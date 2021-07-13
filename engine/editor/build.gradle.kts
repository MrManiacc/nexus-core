@file:Suppress("INACCESSIBLE_TYPE")

plugins {
    id("build-plugin")
}

/**
 * This is used to configure our plugin
 */
nexus {
    configuration {
        module(":engine")
        module(":plugins:opengl")
        module(":plugins:glfw")
        module(":engine:assets")
    }

    extend(project(":engine"), "core", "lwjgl", "imgui")
}