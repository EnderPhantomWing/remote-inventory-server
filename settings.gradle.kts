import groovy.json.JsonSlurper

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        maven("https://jitpack.io") {
            name = "Jitpack"
            content { includeGroupAndSubgroups("com.github") }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.replaymod.preprocess" -> {
                    useModule("com.github.Fallen-Breath:preprocessor:${requested.version}")
                }
            }
        }
    }
}

val settings = JsonSlurper().parseText(file("settings.json").readText()) as Map<String, Any>
@Suppress("UNCHECKED_CAST")
val versions = settings["versions"] as List<String>

for (version in versions) {
    include(":$version")

    val proj = project(":$version")
    proj.projectDir = file("versions/$version")
    proj.buildFileName = "../../common.gradle"
}

// Keep fabricWrapper as the version-pack aggregator (project-specific)
include(":fabricWrapper")
