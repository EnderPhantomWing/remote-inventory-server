# SOURCE PACKAGE

## OVERVIEW

Core mod source: 8 Java files across 4 packages implementing remote container inventory resolution. All files are preprocessed via `//#if` directives to support 14 Minecraft versions.

## STRUCTURE

```
remoteinventory/
├── container/           # 1 file - Core resolution logic
├── enums/               # 1 file - ResultType enum
├── network/
│   ├── handler/         # 1 file - Packet receive + dispatch
│   └── payload/         # 2 files - C2S + S2C packet definitions
├── Reference.java       # Constants (MOD_ID, distance)
└── RemoteInventoryMod.java  # ModInitializer entry point
```

## KEY FLOW

```
RemoteInventoryMod.onInitialize()
  → NetworkHandler.registerReceivers()
    → PayloadTypeRegistry.register() (MC ≥1.20.5)
    → GetItemFromInventoryHandler.register()
      → ContainerItemResolver.resolveItem(player, pos, itemId, slot)
        → distance check → container lookup → slot match → giveItem()
```

## CONVENTIONS

- **No client code**: `@Environment(EnvType.CLIENT)` never used. No `ClientModInitializer`.
- **Preprocessor triples**: Three-way `//#if MC >= 12105` / `//#elseif MC >= 12101` / `//#else` blocks for Identifier/Payload APIs.
- **Payload classes**: CustomPacketPayload implementation for MC ≥1.20.5; old-style `FriendlyByteBuf` for older versions (alternate branch files).
- **Error handling**: Try/catch in handler wraps all resolve logic; returns `ResultType.INTERNAL_ERROR` on exception.
- **Lombok**: Expected on all classes (`@Data`, `@Slf4j`, etc.) but currently only `Reference.LOGGER` uses it.

## ANTI-PATTERNS

- **Never suppress type errors**: No `@SuppressWarnings("unchecked")` or raw type casts.
- **Never import client classes**: No `net.minecraft.client.*` imports anywhere.
- **Never use `@Environment(EnvType.SERVER)`**: Redundant on server-only mods.
