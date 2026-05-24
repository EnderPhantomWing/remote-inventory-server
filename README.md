# Remote Inventory Server

**English** | [中文](README.zh-CN.md)

Server-side Fabric mod that resolves container inventory items remotely. Designed as a backend for **Litematica Printer** and similar clients.

Clients send an item ID + slot → server validates distance, container state, and item match → gives the item to the player or returns a detailed error code.

## Supported Versions

| Minecraft | Java |
|-----------|------|
| 1.18.2, 1.19.4 | Java 17 |
| 1.20.1 – 1.20.6 | Java 21 |
| 1.21.1 – 1.21.11 | Java 21 |
| 26.1 (snapshot) | Java 25 |

> Single codebase, 13 version subprojects, preprocessor handles the rest.

## How It Works

```
Client                              Server
  │                                   │
  ├── itemId + BlockPos + slot ──────►│
  │                                   ├── Distance check (≤ 32 blocks)
  │                                   ├── Chunk loaded?
  │                                   ├── BlockEntity exists?
  │                                   ├── Is a Container?
  │                                   ├── Slot valid & non-empty?
  │                                   ├── Item ID matches?
  │                                   └── Remove item → give to player
  │◄──── result + BlockPos ──────────┤
```

## API

### C2S — `GetItemFromInventoryPayload`

| Field | Type | Description |
|-------|------|-------------|
| `itemId` | `string` | Item identifier (e.g. `minecraft:diamond`) |
| `pos` | `BlockPos` | Container position |
| `slot` | `int` | Slot index |

### S2C — `GetItemResultPayload`

| Field | Type | Description |
|-------|------|-------------|
| `pos` | `BlockPos` | Echoed container position (for correlation) |
| `resultType` | `enum` | See below |

### Result Types

| Code | Meaning |
|------|---------|
| `SUCCESS` | Item removed from container and given to player |
| `PLAYER_TOO_FAR` | Exceeded 32-block interaction range |
| `CONTAINER_NOT_LOADED` | Target chunk not loaded |
| `CONTAINER_NOT_FOUND` | No block entity at position |
| `NOT_A_CONTAINER` | Block entity is not a Container |
| `SLOT_EMPTY` | Slot is empty or out of bounds |
| `ITEM_NOT_MATCH` | Item in slot doesn't match requested item |
| `INTERNAL_ERROR` | Unexpected server-side failure |
| `UNKNOWN` | Unrecognized result |

## Build

```bash
# Build all versions + aggregate version pack
./gradlew fabricWrapper:build

# Build a single version
./gradlew :1.21.11:buildAndCollect

# Run the server for one version
./gradlew :1.21.11:run
```

Output JARs go to `fabricWrapper/build/libs/` (version pack) and each `versions/*/build/libs/` (individual).

## Requirements

- **Java 21+** (Java 25 for 26.1 snapshot builds)
- **Fabric Loader** ≥0.18.4
- **Fabric API** (any version matching your MC version)

## Development

### Project Structure

```
remote-inventory-server/
├── src/main/java/          # Shared source (preprocessed)
├── src/main/resources/     # fabric.mod.json template
├── buildSrc/               # Custom Gradle plugin
├── fabricWrapper/          # Aggregator JAR (bundles all versions)
├── versions/               # 13 MC version subprojects
├── build.gradle.kts        # Preprocessor chain config
├── build.fabric.gradle.kts         # Build config (MC ≥1.21.5)
├── build.fabric.remap.gradle.kts   # Build config (MC <1.21.5)
└── settings.gradle.kts     # Multi-version subproject includes
```

### Preprocessor Directives

The mod uses [Fallen-Breath's preprocessor](https://github.com/Fallen-Breath/preprocessor) to support 13 Minecraft versions from a single source tree:

```java
//#if MC >= 12105
Identifier.parse("minecraft:diamond")           // 1.21.5+
//#elseif MC >= 12101
ResourceLocation.parse("minecraft:diamond")     // 1.21.1 – 1.21.4
//#else
new ResourceLocation("minecraft:diamond")       // ≤1.20.6
//#endif
```

## Configuration

The only configurable constant is `MAX_CONTAINER_INTERACTION_DISTANCE = 32.0` in `Reference.java`.

## License

AGPL-3.0
