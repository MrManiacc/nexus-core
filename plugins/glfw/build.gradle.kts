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
    }

    extend(project(":engine"), "core", "lwjgl")
    extend("opengl")
}