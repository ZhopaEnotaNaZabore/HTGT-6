package com.mod.htgt6.common.handler.recipes.modpackgenerated;

import com.mod.htgt6.common.handler.recipe.RM.modpackgenerated.AssemblerModpackRecipes;
import com.mod.htgt6.common.handler.recipe.RM.modpackgenerated.CompressorModpackRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.compat.GeneratedCompatibilityRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3.GeneratedCraftingRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3.CentrifugeTierCraftingRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3.MissingMachineTierRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.disabled.DisabledRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.integratedgt.IntegratedGTRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.RemovedCraftingRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt.RemovedGTRecipes;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.warn.GeneratedRecipeWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Entry point for recipes migrated from the external modpack scripts. */
public final class ModpackGeneratedRecipes {

    private static final Logger LOGGER = LogManager.getLogger("HTGT6-ModpackRecipes");
    private static final GeneratedRecipeSettings SETTINGS = GeneratedRecipeSettings.DEFAULT;
    private static boolean invoked;

    private ModpackGeneratedRecipes() {}

    public static synchronized void register() {
        if (invoked) return;
        invoked = true;

        if (!SETTINGS.isEnabled()) {
            LOGGER.warn("Generated modpack recipes are disabled (policy={}, grid={}x{})",
                    SETTINGS.getMissingReferencePolicy(), SETTINGS.getMaximumWidth(),
                    SETTINGS.getMaximumHeight());
            return;
        }

        GeneratedCraftingRemoval.consumeRemovedRecipeCount();
        DisabledRecipes.register(SETTINGS, LOGGER);
        int disabledRemoved = GeneratedCraftingRemoval.consumeRemovedRecipeCount();
        RemovedCraftingRecipes.register(SETTINGS, LOGGER);
        int craftingRemoved = GeneratedCraftingRemoval.consumeRemovedRecipeCount();
        RemovedGTRecipes.register(SETTINGS, LOGGER);
        GeneratedCraftingRecipes.register(SETTINGS, LOGGER);
        // Normalize all four GT6 centrifuges after migrated recipes have been
        // inserted, replacing the two older one-off definitions as well.
        CentrifugeTierCraftingRecipes.register(SETTINGS, LOGGER);
        int centrifugeRecipesReplaced = GeneratedCraftingRemoval.consumeRemovedRecipeCount();
        // Fill only audited T1-T4 gaps; keep existing authored recipes and T5+ unchanged.
        MissingMachineTierRecipes.register(SETTINGS, LOGGER);
        GeneratedRecipeWarnings.register(SETTINGS, LOGGER);
        IntegratedGTRecipes.register(SETTINGS, LOGGER);
        GeneratedCompatibilityRecipes.register(SETTINGS, LOGGER);
        com.mod.htgt6.common.handler.recipes.ExtruderProgressionRecipes.register(SETTINGS, LOGGER);
        AssemblerModpackRecipes.register(SETTINGS, LOGGER);
        com.mod.htgt6.common.handler.recipe.RM.ComponentAssemblerRecipes.register();
        com.mod.htgt6.common.handler.recipes.IronChestProgressionRecipes.register(SETTINGS, LOGGER);
        com.mod.htgt6.common.handler.recipes.IronChestModifierRecipes.register(SETTINGS, LOGGER);
        int ironChestRecipesReplaced = GeneratedCraftingRemoval.consumeRemovedRecipeCount();
        CompressorModpackRecipes.register(SETTINGS, LOGGER);
        LOGGER.info("Removed {} disabled and {} regular crafting recipes directly; "
                        + "GT6 non-removable recipe flags were intentionally bypassed",
                disabledRemoved, craftingRemoved + centrifugeRecipesReplaced + ironChestRecipesReplaced);
        LOGGER.info("Registered the normalized ULV-HV crafting chain for all 4 GT6 centrifuges");
        com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3.GeneratedCrafting3x3
                .logRegistrationSummary(LOGGER);
    }

    public static GeneratedRecipeSettings settings() {
        return SETTINGS;
    }
}
