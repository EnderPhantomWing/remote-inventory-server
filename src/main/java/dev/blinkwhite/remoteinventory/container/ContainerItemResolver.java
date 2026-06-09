package dev.blinkwhite.remoteinventory.container;

import dev.blinkwhite.remoteinventory.Reference;
import dev.blinkwhite.remoteinventory.config.RemoteInvConfig;
import dev.blinkwhite.remoteinventory.enums.ResultType;
import dev.blinkwhite.remoteinventory.network.payload.ScanContainerResultPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;

public class ContainerItemResolver {

    public static ResultType resolveItem(ServerPlayer player, BlockPos pos,
                                          String itemIdStr, int slot) {
        Level level = getPlayerLevel(player);

        if (RemoteInvConfig.isDistanceLimitEnabled()) {
            double maxDist = RemoteInvConfig.getMaxInteractionDistance();
            double distance = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distance > maxDist * maxDist) {
                return ResultType.PLAYER_TOO_FAR;
            }
        }

        if (!level.isLoaded(pos)) {
            return ResultType.CONTAINER_NOT_LOADED;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return ResultType.CONTAINER_NOT_FOUND;
        }

        if (!(blockEntity instanceof Container container)) {
            return ResultType.NOT_A_CONTAINER;
        }

        if (!isBlockAllowed(level, pos)) {
            return ResultType.NOT_A_CONTAINER;
        }

        if (slot < 0 || slot >= container.getContainerSize()) {
            return ResultType.SLOT_EMPTY;
        }

        ItemStack stackInSlot = container.getItem(slot);
        if (stackInSlot.isEmpty()) {
            return ResultType.SLOT_EMPTY;
        }

        Item requestedItem = resolveItemFromId(itemIdStr);
        if (requestedItem == null) {
            return ResultType.ITEM_NOT_MATCH;
        }

        if (!stackInSlot.is(requestedItem)) {
            return ResultType.ITEM_NOT_MATCH;
        }

        return giveToPlayer(player, container, slot, stackInSlot);
    }

    private static Item resolveItemFromId(String itemIdStr) {
        //#if MC >= 12105
        net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.parse(itemIdStr);
        return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        //#elseif MC >= 12101
        //$$ net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.parse(itemIdStr);
        //$$ return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        //#else
        //$$ net.minecraft.resources.ResourceLocation id = new net.minecraft.resources.ResourceLocation(itemIdStr);
        //$$ return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        //#endif
    }

    private static boolean isBlockAllowed(Level level, BlockPos pos) {
        return RemoteInvConfig.isBlockAllowed(
                BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString());
    }

    private static Level getPlayerLevel(ServerPlayer player) {
        //#if MC >= 12000
        return player.level();
        //#else
        //$$ return player.level;
        //#endif
    }

    private static ResultType giveToPlayer(ServerPlayer player, Container container,
                                            int slot, ItemStack stack) {
        try {
            ItemStack extracted = container.removeItem(slot, stack.getCount());

            if (extracted.isEmpty()) {
                return ResultType.INTERNAL_ERROR;
            }

            if (!player.getInventory().add(extracted)) {
                player.drop(extracted, false);
            }

            return ResultType.SUCCESS;
        } catch (Exception e) {
            Reference.LOGGER.error("Error giving item to player: {}", e.getMessage(), e);
            return ResultType.INTERNAL_ERROR;
        }
    }

    public static java.util.List<ScanContainerResultPayload.SlotEntry> scanContainer(
            ServerPlayer player, BlockPos pos) {
        Level level = getPlayerLevel(player);

        if (RemoteInvConfig.isDistanceLimitEnabled()) {
            double maxDist = RemoteInvConfig.getMaxInteractionDistance();
            double distance = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distance > maxDist * maxDist) {
                return java.util.List.of();
            }
        }
        if (!level.isLoaded(pos)) {
            return java.util.List.of();
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container container)) {
            return java.util.List.of();
        }

        if (!isBlockAllowed(level, pos)) {
            return java.util.List.of();
        }

        java.util.List<ScanContainerResultPayload.SlotEntry> entries = new java.util.ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                entries.add(new ScanContainerResultPayload.SlotEntry(i, itemId, stack.getCount()));
            }
        }
        return entries;
    }
}