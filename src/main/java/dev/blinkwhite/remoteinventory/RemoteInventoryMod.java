package dev.blinkwhite.remoteinventory;

import dev.blinkwhite.remoteinventory.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;

public class RemoteInventoryMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Reference.LOGGER.info("Initializing Remote Inventory Server...");
        NetworkHandler.registerReceivers();
        Reference.LOGGER.info("Remote Inventory Server initialized.");
    }
}
