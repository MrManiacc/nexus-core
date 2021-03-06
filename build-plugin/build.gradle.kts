plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
}


gradlePlugin {
    plugins.register("build-plugin") {
        id = "build-plugin"
        implementationClass = "nex.plugin.NexPlugin"
    }
}