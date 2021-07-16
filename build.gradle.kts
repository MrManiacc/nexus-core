plugins {
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.21"

}

group = "nex.us"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}