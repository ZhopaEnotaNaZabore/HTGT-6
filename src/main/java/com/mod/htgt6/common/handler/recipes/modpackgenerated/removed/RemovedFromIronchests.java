// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.removed;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval;

public final class RemovedFromIronchests {

    private RemovedFromIronchests() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedCraftingRemoval.remove("IronChests.zs:5", "<IronChest:BlockIronChest>", settings, logger);
        GeneratedCraftingRemoval.remove("IronChests.zs:12", "<IronChest:BlockIronChest:3>", settings, logger);
    }
}
