// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.removed;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval;

public final class RemovedFromFixes {

    private RemovedFromFixes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedCraftingRemoval.remove("fixes.zs:7", "<gregtech:gt.multiitem.technological:1020>", settings, logger);
        GeneratedCraftingRemoval.remove("fixes.zs:20", "<TConstruct:materials:6>", settings, logger);
    }
}
