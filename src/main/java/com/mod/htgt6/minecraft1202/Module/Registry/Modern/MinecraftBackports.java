package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.minecraft1202.Module.Registry.Shared.BackportCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Single lifecycle switch for every implemented post-1.7 Minecraft backport. */
public final class MinecraftBackports {
    private static final Logger LOGGER = LogManager.getLogger("Minecraft_1.20.2");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        // Keep the persisted generatorName resolvable even while the module is disabled.
        // Its WorldType then refuses creation/loading instead of silently falling back to DEFAULT.
        com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120.registerIdentity();
        if (!HTGT6Config.RUN_MINECRAFT_1_7_9999) {
            initialized = true;
            LOGGER.info("Minecraft 1.7.9999 backports are disabled by config");
            return;
        }
        for (BackportCategory category : BackportCategory.values()) {
            LOGGER.info("Loading backport category: {}", category.getDisplayName());
            category.initialize();
        }
        initialized = true;
        LOGGER.info("Minecraft 1.7.9999 initialized with {} categorized backport modules",
                BackportCategory.values().length);
    }

    private MinecraftBackports() { }
}
