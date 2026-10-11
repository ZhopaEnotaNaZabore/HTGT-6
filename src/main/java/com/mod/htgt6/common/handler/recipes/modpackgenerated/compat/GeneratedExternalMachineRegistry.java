package com.mod.htgt6.common.handler.recipes.modpackgenerated.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import cpw.mods.fml.common.Loader;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Logger;

/** Optional-mod machine integrations kept free of hard runtime dependencies. */
public final class GeneratedExternalMachineRegistry {

    private GeneratedExternalMachineRegistry() {}

    public static void removeImmersiveEngineering(String source, String machine, String outputToken,
            GeneratedRecipeSettings settings, Logger logger) {
        if (!Loader.isModLoaded("ImmersiveEngineering")) return;
        String className;
        if ("ArcFurnace".equals(machine)) className = "blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe";
        else if ("Crusher".equals(machine)) className = "blusunrize.immersiveengineering.api.crafting.CrusherRecipe";
        else if ("MetalPress".equals(machine)) className = "blusunrize.immersiveengineering.api.crafting.MetalPressRecipe";
        else throw new IllegalArgumentException("Unsupported IE machine " + machine);
        for (ItemStack output : GeneratedValueResolver.matchingItems(outputToken, source, settings, logger)) {
            try {
                Class<?> recipeClass = Class.forName(className);
                Method remover = recipeClass.getMethod("removeRecipes", ItemStack.class);
                remover.invoke(null, output);
            } catch (Exception error) {
                handle("Cannot remove " + machine + " recipe", source, settings, logger, error);
            }
        }
    }

    public static void addGalaxySpaceAssembler(String source, String outputToken, String[] inputTokens,
            GeneratedRecipeSettings settings, Logger logger) {
        if (!Loader.isModLoaded("GalaxySpace")) return;
        ItemStack output = GeneratedValueResolver.item(outputToken, source + " output", settings, logger);
        if (output == null) return;
        Object[] inputs = new Object[inputTokens.length];
        for (int index = 0; index < inputTokens.length; index++) {
            inputs[index] = GeneratedValueResolver.ingredient(inputTokens[index],
                    source + " input[" + index + ']', settings, logger);
            if (GeneratedValueResolver.isMissing(inputs[index])) return;
        }
        try {
            Class<?> recipesClass = Class.forName(
                    "galaxyspace.systems.SolarSystem.planets.overworld.recipe.AssemberRecipes");
            Field instance = recipesClass.getField("instance");
            Method add = recipesClass.getMethod("addShapelessRecipe", ItemStack.class, Object[].class);
            add.invoke(instance.get(null), output, inputs);
        } catch (Exception error) {
            handle("Cannot add GalaxySpace Assembler recipe", source, settings, logger, error);
        }
    }

    private static void handle(String problem, String source, GeneratedRecipeSettings settings,
            Logger logger, Exception error) {
        String message = problem + " at " + source;
        if (settings.getMissingReferencePolicy() == GeneratedRecipeSettings.MissingReferencePolicy.FAIL) {
            throw new IllegalStateException(message, error);
        }
        if (settings.getMissingReferencePolicy() == GeneratedRecipeSettings.MissingReferencePolicy.WARN) {
            logger.warn(message, error);
        }
    }
}
