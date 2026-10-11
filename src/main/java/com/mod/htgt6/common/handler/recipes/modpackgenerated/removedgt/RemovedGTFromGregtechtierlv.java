// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class RemovedGTFromGregtechtierlv {

    private RemovedGTFromGregtechtierlv() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierLV.zs:239", "gt.recipe.press", new String[] {"<gregtech:gt.multiitem.technological:30201>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierLV.zs:244", "gt.recipe.press", new String[] {"<gregtech:gt.multiitem.technological:30002>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierLV.zs:249", "gt.recipe.press", new String[] {"<gregtech:gt.multiitem.technological:30101>"}, settings, logger);
        GeneratedGTRecipeMaps.removeByOutputs("GregTechTierLV.zs:253", "gt.recipe.bath", new String[] {"<gregtech:gt.multiitem.technological:30301>"}, settings, logger);
    }
}
