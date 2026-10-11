// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class RemovedGTFromGregtechtiermv {

    private RemovedGTFromGregtechtiermv() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierMV.zs:94", "gt.recipe.assembler", new String[] {"<gregtech:gt.multiitem.technological:30102>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierMV.zs:99", "gt.recipe.assembler", new String[] {"<gregtech:gt.multiitem.technological:30202>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierMV.zs:388", "gt.recipe.cruciblealloying", new String[] {"<EnderIO:itemAlloy:1>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierMV.zs:396", "gt.recipe.cruciblealloying", new String[] {"<ore:*GoldInductive>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierMV.zs:397", "gt.recipe.cruciblealloying", new String[] {"<gregtech:gt.meta.ingot:8604>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierMV.zs:467", "gt.recipe.bath", new String[] {"<gregtech:gt.multiitem.technological:30302>"}, settings, logger);
    }
}
