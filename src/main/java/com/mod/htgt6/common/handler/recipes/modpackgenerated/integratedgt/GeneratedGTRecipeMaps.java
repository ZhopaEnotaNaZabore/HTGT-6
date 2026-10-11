package com.mod.htgt6.common.handler.recipes.modpackgenerated.integratedgt;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import gregapi.data.CS;
import gregapi.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Logger;

/** Native replacement for MTUtilsGT.addCustomRecipe(...). */
public final class GeneratedGTRecipeMaps {

    private GeneratedGTRecipeMaps() {}

    public static boolean add(String source, String mapName, boolean hidden, long euPerTick, long duration,
            long[] chances, String[] itemInputs, String[] fluidInputs, String[] fluidOutputs,
            String[] itemOutputs, GeneratedRecipeSettings settings, Logger logger) {
        Recipe.RecipeMap map = Recipe.RecipeMap.RECIPE_MAPS.get(mapName);
        if (map == null) return missing("GT6 recipe map " + mapName, source, settings, logger);

        ItemStack[] inputs = items(itemInputs, source + " inputs", settings, logger);
        ItemStack[] outputs = items(itemOutputs, source + " outputs", settings, logger);
        FluidStack[] fluidsIn = fluids(fluidInputs, source + " fluid inputs", settings, logger);
        FluidStack[] fluidsOut = fluids(fluidOutputs, source + " fluid outputs", settings, logger);
        if (inputs == null || outputs == null || fluidsIn == null || fluidsOut == null) return false;

        Recipe recipe = new Recipe(hidden, true, true, inputs, outputs, CS.NI,
                chances == null ? new long[0] : chances, fluidsIn, fluidsOut,
                duration, euPerTick, 0);
        map.addRecipe(recipe, false, false, false);
        return true;
    }

    private static ItemStack[] items(String[] tokens, String source,
            GeneratedRecipeSettings settings, Logger logger) {
        if (tokens == null) return new ItemStack[0];
        ItemStack[] result = new ItemStack[tokens.length];
        for (int index = 0; index < tokens.length; index++) {
            if (tokens[index] == null || "null".equals(tokens[index])) {
                result[index] = null;
                continue;
            }
            result[index] = GeneratedValueResolver.item(tokens[index], source + '[' + index + ']', settings, logger);
            if (result[index] == null) return null;
        }
        return result;
    }

    private static FluidStack[] fluids(String[] tokens, String source,
            GeneratedRecipeSettings settings, Logger logger) {
        if (tokens == null) return new FluidStack[0];
        FluidStack[] result = new FluidStack[tokens.length];
        for (int index = 0; index < tokens.length; index++) {
            if (tokens[index] == null || "null".equals(tokens[index])) {
                result[index] = null;
                continue;
            }
            result[index] = GeneratedValueResolver.fluid(tokens[index], source + '[' + index + ']', settings, logger);
            if (result[index] == null) return null;
        }
        return result;
    }

    private static boolean missing(String what, String source,
            GeneratedRecipeSettings settings, Logger logger) {
        String message = "Missing " + what + " at " + source;
        switch (settings.getMissingReferencePolicy()) {
            case WARN:
                logger.warn(message + "; entry skipped");
                return false;
            case FAIL:
                throw new IllegalStateException(message);
            case IGNORE:
            default:
                return false;
        }
    }
}
