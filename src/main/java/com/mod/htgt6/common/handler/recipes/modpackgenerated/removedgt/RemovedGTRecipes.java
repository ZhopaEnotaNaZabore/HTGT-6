// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class RemovedGTRecipes {

    private RemovedGTRecipes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        RemovedGTFromBansInfo.register(settings, logger);
        RemovedGTFromFixes.register(settings, logger);
        RemovedGTFromGregtechchemistry.register(settings, logger);
        RemovedGTFromGregtechtierlv.register(settings, logger);
        RemovedGTFromGregtechtiermv.register(settings, logger);
        RemovedGTFromSmcHitech6.register(settings, logger);
    }
}
