import org.gradle.internal.os.OperatingSystem.MAC_OS
import org.gradle.internal.os.OperatingSystem.WINDOWS

plugins {
    id("build-plugin")
}

/**
 * This is used to configure our plugin
 */
nexus {
    configuration {
        module(":engine")
        module(":engine:editor")
        module(":plugins:opengl")
        module(":plugins:glfw")
    }

    extend(project(":engine"), "core", "lwjgl", "opengl", "imgui")
}