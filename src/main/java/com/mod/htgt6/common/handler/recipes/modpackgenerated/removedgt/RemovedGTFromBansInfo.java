// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class RemovedGTFromBansInfo {

    private RemovedGTFromBansInfo() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedGTRecipeMaps.removeByOutputs("Bans+Info.zs:10", "gt.recipe.loom", new String[] {"<minecraft:item_frame>"}, settings, logger);
    }
}
