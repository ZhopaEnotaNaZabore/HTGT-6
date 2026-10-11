// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.compat;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class ExternalFromFixes {

    private ExternalFromFixes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedExternalMachineRegistry.removeImmersiveEngineering("fixes.zs:28", "ArcFurnace", "<ImmersiveEngineering:metal:1>", settings, logger);
        GeneratedExternalMachineRegistry.removeImmersiveEngineering("fixes.zs:35", "ArcFurnace", "<gregtech:gt.meta.ingot:740>", settings, logger);
        GeneratedExternalMachineRegistry.removeImmersiveEngineering("fixes.zs:38", "Crusher", "<gregtech:gt.meta.dust:780>", settings, logger);
        GeneratedExternalMachineRegistry.removeImmersiveEngineering("fixes.zs:39", "ArcFurnace", "<gregtech:gt.meta.ingot:780>", settings, logger);
        GeneratedExternalMachineRegistry.removeImmersiveEngineering("fixes.zs:45", "Crusher", "<ImmersiveEngineering:metal:14>", settings, logger);
    }
}
