// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.integratedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class IntegratedGTFromAe2 {

    private IntegratedGTFromAe2() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedGTRecipeMaps.add("AE2.zs:11", "gt.recipe.assembler", false, 512L, 1296L, new long[] {10000L}, new String[] {"<gregtech:gt.meta.plateCurved:140>*16", "<appliedenergistics2:item.ItemMultiMaterial:1>*1", "<IC2:itemPartCircuit>*1", "<gregtech:gt.meta.stickLong:8647>*1", "<gregapi:gt.integrated_circuit:14>*0"}, new String[] {}, new String[] {}, new String[] {"<appliedenergistics2:tile.BlockSkyCompass>"}, settings, logger);
    }
}
