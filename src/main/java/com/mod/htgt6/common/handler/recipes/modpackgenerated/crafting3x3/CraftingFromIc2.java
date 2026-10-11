// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class CraftingFromIc2 {

    private CraftingFromIc2() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedCrafting3x3.addTokens("IC2.zs:11", "<IC2:itemToolDDrill:*>*1", new String[][] {new String[] {"<gregtech:gt.meta.toolHeadDrill:8300>", "<IC2:itemToolDrill:*>"}}, settings, logger);
        GeneratedCrafting3x3.addTokens("IC2.zs:24", "<HTGT6:hullBattery>*1", new String[][] {new String[] {"<ore:craftingToolScrewdriver>", "<gregtech:gt.multitileentity:28067>", "<ore:craftingToolWireCutter>"}, new String[] {"<ore:casingSmallTin>", "<minecraft:redstone>", "<ore:casingSmallTin>"}, new String[] {"<ore:casingSmallTin>", "<gregtech:gt.meta.plateDouble:8668>", "<ore:casingSmallTin>"}}, settings, logger);
        GeneratedCrafting3x3.addTokens("IC2.zs:33", "<IC2:blockElectric>*1", new String[][] {new String[] {"<gregtech:gt.multitileentity:28367>", "<IC2:itemBatREDischarged>", "<gregtech:gt.multitileentity:28367>"}, new String[] {"<IC2:itemBatREDischarged>", "<gregtech:gt.meta.storage.solid:8668>", "<IC2:itemBatREDischarged>"}, new String[] {"<gregtech:gt.multitileentity:28367>", "<IC2:itemBatREDischarged>", "<gregtech:gt.multitileentity:28367>"}}, settings, logger);
    }
}
