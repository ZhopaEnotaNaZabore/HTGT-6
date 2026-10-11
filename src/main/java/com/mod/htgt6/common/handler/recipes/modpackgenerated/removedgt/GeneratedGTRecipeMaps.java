package com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import gregapi.recipes.Recipe;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Logger;

/** Native replacement for MTUtilsGT.removeAllRecipes(...). */
public final class GeneratedGTRecipeMaps {

    private GeneratedGTRecipeMaps() {}

    public static int removeByOutputs(String source, String mapName, String[] outputs,
            GeneratedRecipeSettings settings, Logger logger) {
        Recipe.RecipeMap map = Recipe.RecipeMap.RECIPE_MAPS.get(mapName);
        if (map == null) {
            handleMissing("GT6 recipe map " + mapName, source, settings, logger);
            return 0;
        }

        Set<Recipe> recipes = new LinkedHashSet<Recipe>();
        for (String output : outputs) {
            List<ItemStack> targets = GeneratedValueResolver.matchingItems(output, source, settings, logger);
            for (ItemStack target : targets) {
                List<Recipe> found = map.getNEIRecipes(target);
                if (found != null) recipes.addAll(new ArrayList<Recipe>(found));
            }
        }
        map.mRecipeList.removeAll(recipes);
        return recipes.size();
    }

    private static void handleMissing(String what, String source,
            GeneratedRecipeSettings settings, Logger logger) {
        String message = "Missing " + what + " at " + source;
        switch (settings.getMissingReferencePolicy()) {
            case WARN:
                logger.warn(message + "; entry skipped");
                break;
            case FAIL:
                throw new IllegalStateException(message);
            case IGNORE:
            default:
                break;
        }
    }
}
