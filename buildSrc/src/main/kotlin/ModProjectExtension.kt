import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

fun Project.propOrNull(key: String) = findProperty(key)
fun Project.prop(key: String) = propOrNull(key) ?: throw GradleException("buildSrc: Property $key not configured / value is null")

fun Project.propStrOrNull(key: String): String? = propOrNull(key)?.toString()
fun Project.propStr(key: String): String = propStrOrNull(key)
    ?: throw GradleException("buildSrc: Property $key not configured / cannot convert to string")

val Project.modId get() = propStr("mod_id")
val Project.modName get() = propStr("mod_name")
val Project.modVersion get() = propStr("mod_version")
val Project.modMavenGroup get() = propStr("mod_maven_group")
val Project.modArchivesBaseName get() = propStr("mod_archives_base_name")

val Project.modDescription get() = propStrOrNull("mod_description")
val Project.modHomepage get() = propStrOrNull("mod_homepage")
val Project.modLicense get() = propStrOrNull("mod_license")
val Project.modSources get() = propStrOrNull("mod_sources")

val Project.mcDependency get() = propStrOrNull("minecraft_dependency")
val Project.mcVersion get() = propStrOrNull("minecraft_version")
val Project.mcVersionInt get() = propStrOrNull("mcVersion")?.toIntOrNull() ?: -1
val Project.fabricLoaderVersion get() = propStrOrNull("loader_version")
val Project.fabricApiVersion get() = propStrOrNull("fabric_version")

val Project.lombokVersion get() = propStr("lombok_version")

val Project.javaVersion
    get() = when {
        mcVersionInt >= 260000 -> JavaVersion.VERSION_25
        mcVersionInt >= 12005 -> JavaVersion.VERSION_21
        mcVersionInt >= 11800 -> JavaVersion.VERSION_17
        mcVersionInt >= 11700 -> JavaVersion.VERSION_16
        else -> JavaVersion.VERSION_1_8
    }
val Project.mixinJavaVersion get() = "JAVA_${javaVersion}"

val Project.fullProjectVersion: String get() = getFullProjectVersion(modVersion)

private fun getFullProjectVersion(modVersion: String): String {
    val isRelease = System.getenv("IS_THIS_RELEASE")?.toBoolean() == true
    val isCi = System.getenv("CI") == "true" || System.getenv("GITHUB_ACTIONS") == "true"

    return when {
        isRelease -> modVersion
        isCi -> {
            val time = SimpleDateFormat("yyMMdd")
                .apply { timeZone = TimeZone.getTimeZone("GMT+08:00") }
                .format(Date())
                .toString()
            val buildNumber = System.getenv("GITHUB_RUN_NUMBER")
            val version = "$modVersion+$time"
            if (buildNumber != null) {
                "$version+build.$buildNumber"
            } else {
                version
            }
        }
        else -> {
            val time = SimpleDateFormat("yyMMdd")
                .apply { timeZone = TimeZone.getTimeZone("GMT+08:00") }
                .format(Date())
                .toString()
            "$modVersion+$time"
        }
    }
}

val Project.placeholderProps: Map<String, Any?>
    get() = mapOf(
        "mod_id" to modId,
        "mod_wrapper_id" to propStr("mod_wrapper_id"),
        "mod_name" to modName,
        "mod_version" to fullProjectVersion,
        "mod_description" to modDescription,
        "mod_homepage" to modHomepage,
        "mod_license" to modLicense,
        "mod_sources" to modSources,
        "loader_version" to fabricLoaderVersion,
        "fabric_api_version" to fabricApiVersion,
        "minecraft_dependency" to mcDependency,
        "compatibility_level" to mixinJavaVersion,
    ).filterValues { it != null }.mapValues { it.value!! }
