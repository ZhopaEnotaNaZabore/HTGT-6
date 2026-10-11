package com.mod.htgt6.minecraft1202.Module.Registry.Inventory;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;
import cpw.mods.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Standalone off-hand module implemented on top of the 1.7.10 inventory,
 * container, event and SimpleImpl networking APIs.
 */
public final class OffhandModule112 {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-OffhandModule");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        OffhandSystem.init();
        if (FMLCommonHandler.instance().getSide().isClient()) registerClient();
        LOGGER.info("Native 1.7.10 off-hand module initialized (inventory slot 40, container slot 45)");
    }

    private static void registerClient() {
        try {
            Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Inventory.OffhandModule112Client")
                    .getMethod("init").invoke(null);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize the off-hand client module", exception);
        }
    }

    private OffhandModule112() { }
}
