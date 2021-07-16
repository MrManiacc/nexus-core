@file:Suppress("INACCESSIBLE_TYPE")

plugins {
    id("build-plugin")
}

/**
 * This is used to configure our plugin
 */
nexus {
    props {
        this["bytebuddy_version"] = "1.10.22"
    }

    configuration {
        implementation("com.tickaroo.tikxml:core:0.8.15")
        implementation("com.tickaroo.tikxml:annotation:0.8.13")
        implementation("net.bytebuddy:byte-buddy:@bytebuddy_version")
    }

    extend(project(":engine"), "core", "lwjgl", "imgui")
}