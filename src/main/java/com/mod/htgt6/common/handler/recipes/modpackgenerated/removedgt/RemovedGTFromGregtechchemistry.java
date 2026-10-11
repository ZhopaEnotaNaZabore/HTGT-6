// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class RemovedGTFromGregtechchemistry {

    private RemovedGTFromGregtechchemistry() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedGTRecipeMaps.removeByOutputs("GregTechChemistry.zs:23", "gt.recipe.heatmixer", new String[] {"<gregtech:gt.meta.dust:15402>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechChemistry.zs:28", "gt.recipe.mixer", new String[] {"<gregtech:gt.meta.dust:16009>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechChemistry.zs:33", "gt.recipe.electrolyzer", new String[] {"<gregtech:gt.meta.dust:780>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechChemistry.zs:45", "gt.recipe.centrifuge", new String[] {"<gregtech:gt.meta.dustTiny:780>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechChemistry.zs:46", "gt.recipe.sifter", new String[] {"<gregtech:gt.meta.dustTiny:780>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechChemistry.zs:47", "gt.recipe.sifter", new String[] {"<gregtech:gt.meta.dustSmall:780>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechChemistry.zs:48", "gt.recipe.roaster", new String[] {"<gregtech:gt.meta.dustTiny:780>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechChemistry.zs:49", "gt.recipe.roaster", new String[] {"<gregtech:gt.meta.dustSmall:780>"}, settings, logger);
    }
}
