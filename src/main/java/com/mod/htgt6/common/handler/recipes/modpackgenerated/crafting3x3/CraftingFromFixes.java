// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class CraftingFromFixes {

    private CraftingFromFixes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedCrafting3x3.addTokens("fixes.zs:8", "<gregtech:gt.multiitem.technological:1020>*1", new String[][] {new String[] {"<ore:plateSteel>", "<ore:plateSteel>", "<ore:plateSteel>"}, new String[] {"<ore:stickLongSteel>", "<ore:craftingToolWrench>", "<ore:stickLongSteel>"}, new String[] {"<ore:plateSteel>", "<ore:plateSteel>", "<ore:plateSteel>"}}, settings, logger);
        GeneratedCrafting3x3.addTokens("fixes.zs:16", "<HTGT6:ThermometreNEW>*1", new String[][] {new String[] {"<gregtech:gt.multiitem.randomtools:10000>"}}, settings, logger);
        GeneratedCrafting3x3.addTokens("fixes.zs:21", "<TConstruct:materials:6>*1", new String[][] {new String[] {"<minecraft:mossy_cobblestone>", "<ore:craftingToolChisel>"}}, settings, logger);
    }
}
