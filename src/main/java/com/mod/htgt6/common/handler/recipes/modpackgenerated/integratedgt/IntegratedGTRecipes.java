// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.integratedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class IntegratedGTRecipes {

    private IntegratedGTRecipes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        IntegratedGTFromAe2.register(settings, logger);
        IntegratedGTFromEio.register(settings, logger);
        IntegratedGTFromGregtechchemistry.register(settings, logger);
        IntegratedGTFromGregtechmachines.register(settings, logger);
        IntegratedGTFromGregtechparts.register(settings, logger);
        IntegratedGTFromGregtechtierlv.register(settings, logger);
        IntegratedGTFromGregtechtiermv.register(settings, logger);
        IntegratedGTFromIc2.register(settings, logger);
        IntegratedGTFromMixer.register(settings, logger);
    }
}
