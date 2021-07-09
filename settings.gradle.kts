rootProject.name = "nexus-core"
pluginManagement {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
    }

}
includeBuild("build-plugin")

rootDir.walk().forEach {
    if (it.list { _, name -> name.endsWith(".gradle.kts") }
            ?.isNotEmpty() == true) {
        val name = it.toRelativeString(rootDir).replace("\\", ":").replace("/", ":")
        println(name)
        if (name != "build-plugin")
            include(name)
    }
}


