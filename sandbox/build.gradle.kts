plugins {
    id("build-plugin")
    id("application")
}

/**
 * This is used to configure our plugin
 */
nexus {
    configuration {
        module(":engine")
        module(":engine:assets")
        module(":engine:editor")
        module(":plugins:opengl")
        module(":plugins:glfw")
    }

    extend(project(":engine"), "core", "lwjgl", "opengl", "imgui")
}

application {
    mainClassName="marx.MainKt"
}