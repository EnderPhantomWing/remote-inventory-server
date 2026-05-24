# PROJECT KNOWLEDGE BASE

**Generated:** 2026-05-24
**Stack:** Minecraft Fabric mod, Java 8-25, Gradle 9.5 + Kotlin DSL

## OVERVIEW

Server-side Fabric mod that resolves container inventory items remotely. Client sends item ID + slot → server validates distance, container state, item match → gives item to player or returns error. Spans Minecraft 1.18.2 through 26.1 via preprocessor.

## STRUCTURE

```
remote-inventory-server/
├── src/main/java/        # Shared source (preprocessed per version)
├── src/main/resources/   # fabric.mod.json template
├── buildSrc/             # Custom Gradle plugin (mod-plugin)
├── fabricWrapper/        # Aggregator JAR bundling all version submods
├── versions/             # 13 MC version subprojects (gradle.properties)
├── build.gradle.kts      # Preprocessor version chain config
├── build.fabric.gradle.kts       # Build config for MC ≥1.21.5
├── build.fabric.remap.gradle.kts # Build config for MC <1.21.5
└── settings.gradle.kts   # Multi-version subproject includes
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Mod entry point | `RemoteInventoryMod.java` | Calls `NetworkHandler.registerReceivers()` |
| Core logic | `container/ContainerItemResolver.java` | Distance check → container lookup → slot match → give item |
| Network packets (C2S) | `network/payload/GetItemFromInventoryPayload.java` | itemId + BlockPos + slot |
| Network packets (S2C) | `network/payload/GetItemResultPayload.java` | BlockPos + ResultType |
| Packet handler | `network/handler/GetItemFromInventoryHandler.java` | Server-thread dispatch |
| Network registration | `network/NetworkHandler.java` | PayloadTypeRegistry + handler registration |
| Result enums | `enums/ResultType.java` | SUCCESS, PLAYER_TOO_FAR, CONTAINER_NOT_FOUND, etc. |
| Build plugin | `buildSrc/` | ModPlugin.kt, ModProjectExtension.kt |

## CONVENTIONS

- **Preprocessor comments**: `//#if MC >= X` / `//#else` / `//#endif` for version-specific code. `//$$` prefix for alternate branches. Mojang mappings only.
- **Version subprojects**: Each subproject in `versions/` has `gradle.properties` with `minecraft_version`, `fabric_version`, `mcVersion` (int). `mcVersion` drives Java version selection.
- **Build split**: MC ≥1.21.5 uses `fabric-loom` (built-in remap). MC <1.21.5 uses `fabric-loom-remap`.
- **Lombok**: Project uses Lombok throughout. `@Data`, `@Slf4j` etc. expected.
- **Java versions**: `mcVersionInt >= 260000 → Java 25`, `>= 12005 → Java 21`, `>= 11800 → Java 17`, `< 11800 → Java 8`.
- **IDE runs**: Only server run config generated. VM args include Mixin debug.
- **Version packaging**: `fabricWrapper` collects all version JARs into a single "version pack" mod.

## ANTI-PATTERNS

- **No implicit version assumptions**: Every MC-specific API call must be guarded by preprocessor conditions.
- **No client-side code**: Server-only mod. No client initializer, no client networking.
- **No direct ResourceLocation constructor on MC ≥1.21**: Use `Identifier.parse()` / `Identifier.fromNamespaceAndPath()` on modern versions.
- **No hardcoded mod metadata**: Always use `${placeholder}` in JSON for Gradle substitution.

## UNIQUE STYLES

- Preprocessor `//#if` blocks with three tiers: MC ≥12105 → modern Identifier API, MC ≥12101 → ResourceLocation.parse(), fallback → `new ResourceLocation()`.
- Single `MAX_CONTAINER_INTERACTION_DISTANCE = 32.0` in `Reference.java` as the only mod config.
- Two build script variants (`fabric` vs `fabric-remap`) selected by MC version threshold.

## COMMANDS

```bash
./gradlew fabricWrapper:build          # Build all versions + aggregate
./gradlew :1.21.11:buildAndCollect    # Build single version
./gradlew :1.21.11:run                # Run server for one version
```

## NOTES

- `versions/mainProject` file points to the current primary version (1.21.11).
- Version 26.1 is the next major Minecraft version (latest snapshot). Fabric API version `0.144.3+26.1`.
- No test directory exists yet. No CI config found (no .github/workflows).
- Git is initialized but no commits detected.
