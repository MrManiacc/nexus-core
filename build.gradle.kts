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

allprojects{
    apply(plugin="kotlin")
    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("io.mockk:mockk:1.9.3")
        testImplementation("org.assertj:assertj-core:3.11.1")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
    }

    tasks.named("test", Test::class).configure {
        useJUnitPlatform()
    }
}