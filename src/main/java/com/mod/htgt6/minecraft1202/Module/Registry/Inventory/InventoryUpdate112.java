package com.mod.htgt6.minecraft1202.Module.Registry.Inventory;

import com.mod.htgt6.minecraft1202.Module.Client.Inventory.InventoryUpdate112Client;

import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.RecipeBookSystem112;
import cpw.mods.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Lifecycle owner for the Minecraft Java 1.12 inventory and recipe-book backport. */
public final class InventoryUpdate112 {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Inventory112");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        OffhandModule112.init();
        RecipeBookSystem112.init();
        if (FMLCommonHandler.instance().getSide().isClient()) registerClient();
        LOGGER.info("Minecraft Java 1.12 inventory and recipe-book module initialized");
    }

    private static void registerClient() {
        try {
            Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Inventory.InventoryUpdate112Client").getMethod("init").invoke(null);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize Minecraft Java 1.12 inventory client", exception);
        }
    }

    private InventoryUpdate112() { }
}
