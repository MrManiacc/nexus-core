plugins {
    id("build-plugin")
}

/**
 * This is used to configure our plugin
 */
nexus {
    extend(project(":engine"), "engine", "core", "lwjgl", "opengl")
}