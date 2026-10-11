// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.integratedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class IntegratedGTFromIc2 {

    private IntegratedGTFromIc2() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        GeneratedGTRecipeMaps.add("IC2.zs:18", "gt.recipe.injector", false, 256L, 1300L, new long[] {10000L}, new String[] {"<HTGT6:BoronGlass1>*1", "<IC2:itemPartAlloy>*1"}, new String[] {"<liquid:concrete>*1296"}, new String[] {"null"}, new String[] {"<IC2:blockAlloyGlass>"}, settings, logger);
        GeneratedGTRecipeMaps.add("IC2.zs:29", "gt.recipe.canner", false, 16L, 500L, new long[] {10000L}, new String[] {"<HTGT6:hullBattery>*1", "<minecraft:redstone>*2"}, new String[] {}, new String[] {}, new String[] {"<IC2:itemBatREDischarged>"}, settings, logger);
    }
}
