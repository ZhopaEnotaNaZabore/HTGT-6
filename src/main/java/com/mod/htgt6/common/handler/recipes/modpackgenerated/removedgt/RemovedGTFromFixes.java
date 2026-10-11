// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class RemovedGTFromFixes {

    private RemovedGTFromFixes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedGTRecipeMaps.removeByOutputs("fixes.zs:32", "mc.recipe.furnace", new String[] {"<nova_craft:pherithium_ingot>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("fixes.zs:42", "gt.recipe.bath", new String[] {"<gregtech:gt.meta.crushedPurifiedTiny:780>"}, settings, logger);
    }
}
