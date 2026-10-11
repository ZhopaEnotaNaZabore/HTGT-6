// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.
package com.mod.htgt6.common.handler.recipes.modpackgenerated.warn;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import org.apache.logging.log4j.Logger;

public final class GeneratedRecipeWarnings {

    private GeneratedRecipeWarnings() {}

    public static void register(GeneratedRecipeSettings settings, Logger logger) {
        WarningsFromBansInfo.register(settings, logger);
        WarningsFromCtaHitech6.register(settings, logger);
    }
}
