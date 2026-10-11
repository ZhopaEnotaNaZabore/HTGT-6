// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.compat;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class FurnaceFromFixes {

    private FurnaceFromFixes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedFurnaceRegistry.remove("fixes.zs:27", "<ore:ingotAluminium>", settings, logger);
        GeneratedFurnaceRegistry.remove("fixes.zs:31", "<ore:ingotPherithium>", settings, logger);
        GeneratedFurnaceRegistry.remove("fixes.zs:40", "<ore:ingotPlatinum>", settings, logger);
    }
}
