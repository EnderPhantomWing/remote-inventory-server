import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

plugins {
    id("java-library")
    id("maven-publish")
}

// Inline property accessors (replaces buildSrc ModProjectExtension)
fun Project.propStr(key: String): String = findProperty(key)?.toString()
    ?: throw GradleException("Property '$key' not configured")

val modMavenGroup: String by lazy { rootProject.propStr("maven_group") }
val modArchivesBaseName: String by lazy { rootProject.propStr("archives_base_name") }
val modVersion: String by lazy { rootProject.propStr("mod_version") }

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

group = modMavenGroup
version = modVersion

base {
    archivesName.set("$modArchivesBaseName-versionpack")
}

val fabricSubprojects = rootProject.subprojects.filter { it.name != "fabricWrapper" }

fabricSubprojects.forEach {
    evaluationDependsOn(":${it.name}")
}

tasks {
    val collectSubModules by registering {
        val destDir = layout.buildDirectory.dir("tmp/submods/META-INF/jars")

        outputs.upToDateWhen { false }

        dependsOn(fabricSubprojects.map { it.tasks.named("build") })

        doLast {
            val destDirFile = destDir.get().asFile
            destDirFile.deleteRecursively()
            destDirFile.mkdirs()

            fabricSubprojects.forEach { sub ->
                val subDir = sub.projectDir.resolve("build/libs")
                if (subDir.exists() && subDir.isDirectory) {
                    val jars = subDir.listFiles()?.filter { it.extension == "jar" } ?: return@forEach
                    val latestJar = jars.maxByOrNull { it.lastModified() } ?: return@forEach
                    latestJar.copyTo(destDirFile.resolve(latestJar.name), overwrite = true)
                    println("Copied: ${latestJar.name}")
                }
            }
        }
    }

    named<Jar>("jar") {
        dependsOn(collectSubModules)
        dependsOn("processResources")

        from(rootProject.file("LICENSE"))
        from(layout.buildDirectory.dir("tmp/submods"))
    }

    named<ProcessResources>("processResources") {
        dependsOn(collectSubModules)

        doLast {
            val jarsDir = layout.buildDirectory.dir("tmp/submods/META-INF/jars").get().asFile
            val jars = jarsDir.listFiles()
                ?.filter { it.extension == "jar" }
                ?.map { mapOf("file" to "META-INF/jars/${it.name}") }
                ?: emptyList()

            val minecraftVersions = fabricSubprojects.mapNotNull { sub ->
                try {
                    sub.property("minecraft_dependency") as? String
                } catch (e: Exception) {
                    null
                }
            }.distinct()

            val jsonFile = layout.buildDirectory.file("resources/main/fabric.mod.json").get().asFile
            if (jsonFile.exists()) {
                val json = JsonSlurper().parse(jsonFile) as MutableMap<String, Any>
                json["jars"] = jars
                json["depends"] = mapOf(
                    "minecraft" to minecraftVersions.joinToString(" | ")
                )
                jsonFile.writeText(JsonBuilder(json).toPrettyString())
            }
        }
    }

    named("build") {
        dependsOn("jar")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modArchivesBaseName
            version = modVersion
        }
    }
    repositories {
        mavenLocal()
    }
}
