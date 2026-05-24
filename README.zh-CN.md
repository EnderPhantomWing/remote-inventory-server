# Remote Inventory Server

[English](README.md) | **中文**

服务端 Fabric Mod，远程解析容器物品。作为 **Litematica Printer** 等客户端的后端使用。

客户端发送物品 ID + 槽位 → 服务端验证距离、容器状态、物品匹配 → 将物品给予玩家或返回详细错误码。

## 支持的版本

| Minecraft | Java |
|-----------|------|
| 1.18.2, 1.19.4 | Java 17 |
| 1.20.1 – 1.20.6 | Java 21 |
| 1.21.1 – 1.21.11 | Java 21 |
| 26.1（快照） | Java 25 |

> 单一代码库，13 个版本子项目，预处理器处理所有版本差异。

## 工作流程

```
客户端                             服务端
  │                                   │
  ├── 物品ID + 坐标 + 槽位 ──────────►│
  │                                   ├── 距离检查（≤ 32 格）
  │                                   ├── 区块是否已加载？
  │                                   ├── 方块实体是否存在？
  │                                   ├── 是否为容器？
  │                                   ├── 槽位有效且非空？
  │                                   ├── 物品 ID 是否匹配？
  │                                   └── 取出物品 → 给予玩家
  │◄──── 结果 + 坐标 ────────────────┤
```

## API

### C2S — `GetItemFromInventoryPayload`

| 字段 | 类型 | 说明 |
|------|------|------|
| `itemId` | `string` | 物品标识（如 `minecraft:diamond`） |
| `pos` | `BlockPos` | 容器坐标 |
| `slot` | `int` | 槽位索引 |

### S2C — `GetItemResultPayload`

| 字段 | 类型 | 说明 |
|------|------|------|
| `pos` | `BlockPos` | 回显的容器坐标（用于关联请求） |
| `resultType` | `enum` | 见下表 |

### 结果类型

| 返回值 | 含义 |
|--------|------|
| `SUCCESS` | 已从容器取出物品并给予玩家 |
| `PLAYER_TOO_FAR` | 超出 32 格交互范围 |
| `CONTAINER_NOT_LOADED` | 目标区块未加载 |
| `CONTAINER_NOT_FOUND` | 坐标处无方块实体 |
| `NOT_A_CONTAINER` | 方块实体不是容器 |
| `SLOT_EMPTY` | 槽位为空或超出范围 |
| `ITEM_NOT_MATCH` | 槽位中的物品与请求不匹配 |
| `INTERNAL_ERROR` | 服务端意外错误 |
| `UNKNOWN` | 无法识别的结果 |

## 构建

```bash
# 构建所有版本 + 聚合版本包
./gradlew fabricWrapper:build

# 构建单个版本
./gradlew :1.21.11:buildAndCollect

# 运行单个版本的服务端
./gradlew :1.21.11:run
```

构建产物：`fabricWrapper/build/libs/`（版本包）及各 `versions/*/build/libs/`（单个版本）。

## 依赖

- **Java 21+**（26.1 快照需要 Java 25）
- **Fabric Loader** ≥0.18.4
- **Fabric API**（版本匹配你的 MC 版本）

## 开发

### 项目结构

```
remote-inventory-server/
├── src/main/java/          # 共享源码（经过预处理）
├── src/main/resources/     # fabric.mod.json 模板
├── buildSrc/               # 自定义 Gradle 插件
├── fabricWrapper/          # 聚合 JAR（打包所有版本）
├── versions/               # 13 个 MC 版本子项目
├── build.gradle.kts        # 预处理器链配置
├── build.fabric.gradle.kts         # MC ≥1.21.5 构建配置
├── build.fabric.remap.gradle.kts   # MC <1.21.5 构建配置
└── settings.gradle.kts     # 多版本子项目引入
```

### 预处理器指令

使用 [Fallen-Breath 预处理器](https://github.com/Fallen-Breath/preprocessor) 在单一源码中支持 13 个 Minecraft 版本：

```java
//#if MC >= 12105
Identifier.parse("minecraft:diamond")           // 1.21.5+
//#elseif MC >= 12101
ResourceLocation.parse("minecraft:diamond")     // 1.21.1 – 1.21.4
//#else
new ResourceLocation("minecraft:diamond")       // ≤1.20.6
//#endif
```

## 配置

唯一的可配置参数是 `Reference.java` 中的 `MAX_CONTAINER_INTERACTION_DISTANCE = 32.0`。

## 许可

AGPL-3.0
