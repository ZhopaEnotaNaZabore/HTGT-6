// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.compat;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class FurnaceFromGamestart {

    private FurnaceFromGamestart() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedFurnaceRegistry.add("gamestart.zs:46", "<HTGT6:FireclayBrick>", "<HTGT6:UnfFireclybrick>", settings, logger);
        GeneratedFurnaceRegistry.remove("gamestart.zs:116", "<ProjRed|Core:projectred.core.part:10>", settings, logger);
        GeneratedFurnaceRegistry.remove("gamestart.zs:126", "<minecraft:glass>", settings, logger);
        GeneratedFurnaceRegistry.add("gamestart.zs:127", "<minecraft:glass>", "<gregtech:gt.meta.dust:8001>", settings, logger);
    }
}
