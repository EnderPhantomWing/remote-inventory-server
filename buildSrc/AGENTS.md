# BUILD CUSTOMIZATIONS

## OVERVIEW

Custom Gradle plugin (`mod-plugin`) that applies boilerplate configurations to all version subprojects: Java version, Lombok, encoding, JAR manifest, and resource template expansion.

## FILES

| File | Role |
|------|------|
| `ModPlugin.kt` | Plugin entry: applies java plugin, configures compilation, Lombok, resources, JAR |
| `ModProjectExtension.kt` | `prop()` / `propStr()` accessors for `gradle.properties`, `javaVersion` resolver, `placeholderProps` map |
| `build.gradle.kts` | Registers `mod-plugin` with `ModPlugin` as implementation class |

## KEY BEHAVIOR

- **Java version resolution**: `mcVersionInt` driven:
  - `>= 260000 → Java 25` (MC 26.1)
  - `>= 12005 → Java 21` (MC 1.20.5+)
  - `>= 11800 → Java 17` (MC 1.18+)
  - `< 11800 → Java 8`
- **Resource expansion**: `fabric.mod.json` uses `${placeholder}` syntax → expanded by Gradle's `CopySpec.expand()`.
- **Version string**: CI-aware (`IS_THIS_RELEASE` env var). Non-release builds get `modVersion+YYMMDD[+build.N]`.
- **Compiler flags**: `-Xlint:deprecation`, `-Xlint:unchecked`. Suppressed `-Xlint:-options` for Java ≤8.

## CONVENTIONS

- All property keys come from root `gradle.properties` or version subproject `gradle.properties`.
- Version subprojects MUST define: `minecraft_version`, `fabric_version`, `mcVersion` (int).
- `placeholderProps` map keys must match `${key}` in resource JSON files.

## ANTI-PATTERNS

- Never hardcode Minecraft version or dependency versions in plugin code - always read from project properties.
- Never add Fabric Loom configuration here; it's in the build script variants (`build.fabric*.gradle.kts`).
