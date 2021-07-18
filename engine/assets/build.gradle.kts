@file:Suppress("INACCESSIBLE_TYPE")

plugins {
    id("build-plugin")
}

/**
 * This is used to configure our plugin
 */
nexus {

    configuration {
        module(":engine:modules")
    }

    /**
     * This should import base and lwjgl configs (lwjgl extends base). This means we get all the base deps but without
     * the need of the modules class.
     */
    extend(project(":engine"), "core")
}