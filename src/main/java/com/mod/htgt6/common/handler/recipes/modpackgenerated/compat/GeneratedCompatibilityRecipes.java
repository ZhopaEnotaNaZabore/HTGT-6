// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.compat;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class GeneratedCompatibilityRecipes {

    private GeneratedCompatibilityRecipes() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        FurnaceFromFixes.register(settings, logger);
        FurnaceFromGamestart.register(settings, logger);
        FurnaceFromGregtechtierlv.register(settings, logger);
        ExternalFromAssemberrecipes.register(settings, logger);
        ExternalFromFixes.register(settings, logger);
        ExternalFromGregtechtiermv.register(settings, logger);
        ExternalFromSmcHitech6.register(settings, logger);
    }
}
