@file:Suppress("INACCESSIBLE_TYPE")

plugins {
    id("build-plugin")
}

/**
 * This is used to configure our plugin
 */
nexus {
    extend(project(":engine"), "engine", "plugin", "core", "lwjgl", "opengl", "imgui")
}