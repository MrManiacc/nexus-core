import org.gradle.internal.os.OperatingSystem.MAC_OS
import org.gradle.internal.os.OperatingSystem.WINDOWS

plugins {
    id("build-plugin")
}


/**
 * This is used to configure our plugin
 */
nexus {
    props {
        this["lwjgl_version"] = "3.2.3"
        this["imgui_version"] = "1.83.2"
        this["kotlin_logging_version"] = "2.0.8"
        this["guava_version"] = "30.1.1-jre"
        this["message_bus_version"] = "2.4"
        this["slf4j_version"] = "1.7.5"
        this["joml_version"] = "1.10.1"
        this["classgraph_version"] = "4.8.110"
        this["native"] = when (org.gradle.internal.os.OperatingSystem.current()) {
            WINDOWS -> "natives-windows"
            MAC_OS -> "natives-macos"
            else -> "natives-linux"
        }
    }


    configuration {
        module(":engine:assets")
        module(":engine:extension")
    }


    /**
     * This core configuration can be applied to anyone that uses it to extend a given nexus plugin extension
     */
    configuration("core") {
        plugin("kotlin", "1.5.20")
        plugin("org.jetbrains.kotlin.plugin.serialization", "1.5.20")
        maven();
        jcenter();
        google()

        //Kotlin logging/slf4j - logging libraries
        implementation("io.github.microutils:kotlin-logging-jvm:@kotlin_logging_version")
        implementation("org.slf4j:slf4j-api:@slf4j_version")
        implementation("org.slf4j:slf4j-log4j12:@slf4j_version")
        //Joml - nexus.engine.math library
        implementation("org.joml:joml:@joml_version")
        //Guava - utility library with lots of useful things
        implementation("com.google.guava:guava:@guava_version")
        //MessageBus - an event bus used for notifying the system of things
        implementation("com.dorkbox:MessageBus:@message_bus_version")
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.20")
        implementation("io.github.classgraph:classgraph:@classgraph_version")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

        sources {
            source("src/main/kotlin")
            resource("src/main/resource")
        }

    }

    /**
     * This configuration allows lwjgl libraries to be added
     */
    configuration("lwjgl") {

        platform("org.lwjgl:lwjgl-bom:@lwjgl_version")

        //Here we define our lwjgl libraries
        implementation("org.lwjgl:lwjgl:@lwjgl_version")
        implementation("org.lwjgl:lwjgl-assimp:@lwjgl_version")
        implementation("org.lwjgl:lwjgl-glfw:@lwjgl_version")
        implementation("org.lwjgl:lwjgl-openal:@lwjgl_version")
        implementation("org.lwjgl:lwjgl-opengl:@lwjgl_version")
        implementation("org.lwjgl:lwjgl-stb:@lwjgl_version")

        //Here we define our native lwjgl libraries
        runtimeOnly("org.lwjgl:lwjgl::@native")
        runtimeOnly("org.lwjgl:lwjgl-assimp::@native")
        runtimeOnly("org.lwjgl:lwjgl-glfw::@native")
        runtimeOnly("org.lwjgl:lwjgl-openal::@native")
        runtimeOnly("org.lwjgl:lwjgl-opengl::@native")
        runtimeOnly("org.lwjgl:lwjgl-stb::@native")

    }
    /**
     * Used for all libraries related to imgui
     */
    configuration("imgui") {
        implementation("io.github.spair:imgui-java-binding:@imgui_version")
        implementation("io.github.spair:imgui-java-lwjgl3:@imgui_version")
        runtimeOnly("io.github.spair:imgui-java-@native-ft:@imgui_version")
    }


    //We only need to extend the core in this case, we just define the lwjgl for later use
    extend("core")
}