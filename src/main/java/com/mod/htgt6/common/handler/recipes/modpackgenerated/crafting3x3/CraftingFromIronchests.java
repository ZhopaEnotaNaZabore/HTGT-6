// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class CraftingFromIronchests {

    private CraftingFromIronchests() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedCrafting3x3.addTokens("IronChests.zs:6", "<IronChest:BlockIronChest>*1", new String[][] {new String[] {"<gregtech:gt.meta.screw:260>", "<gregtech:gt.meta.plateTriple:260>", "<gregtech:gt.meta.screw:260>"}, new String[] {"<gregtech:gt.meta.plateTriple:260>", "<minecraft:chest>", "<gregtech:gt.meta.plateTriple:260>"}, new String[] {"<gregtech:gt.meta.screw:260>", "<gregtech:gt.meta.plateTriple:260>", "<gregtech:gt.meta.screw:260>"}}, settings, logger);
        GeneratedCrafting3x3.addTokens("IronChests.zs:13", "<IronChest:BlockIronChest:3>*1", new String[][] {new String[] {"<gregtech:gt.meta.screw:290>", "<gregtech:gt.meta.plateTriple:290>", "<gregtech:gt.meta.screw:290>"}, new String[] {"<gregtech:gt.meta.plateTriple:290>", "<minecraft:chest>", "<gregtech:gt.meta.plateTriple:290>"}, new String[] {"<gregtech:gt.meta.screw:290>", "<gregtech:gt.meta.plateTriple:290>", "<gregtech:gt.meta.screw:290>"}}, settings, logger);
    }
}
