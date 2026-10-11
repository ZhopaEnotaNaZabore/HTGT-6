package com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3;

import java.util.ArrayList;
import java.util.List;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedStackRef;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.Logger;

/** Registers generated shaped recipes whose pattern fits inside a 3x3 grid. */
public final class GeneratedCrafting3x3 {

    private static int attempted, registered;

    public static void logRegistrationSummary(Logger logger) {
        logger.info("Generated crafting: {} registered, {} skipped out of {} attempts",
                registered, attempted - registered, attempted);
    }

    private GeneratedCrafting3x3() {}

    /** Resolve and add first: missing optional ingredients must not erase a working recipe. */
    @SuppressWarnings("unchecked")
    public static boolean replaceTokens(String source, String output, String[][] ingredients,
            GeneratedRecipeSettings settings, Logger logger) {
        ItemStack target = GeneratedValueResolver.item(output, source + " output", settings, logger);
        if (target == null) { attempted++; return false; }
        List<net.minecraft.item.crafting.IRecipe> recipes =
                net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
        java.util.Set<net.minecraft.item.crafting.IRecipe> previous = java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<net.minecraft.item.crafting.IRecipe, Boolean>());
        for (net.minecraft.item.crafting.IRecipe recipe : recipes) {
            ItemStack result = recipe == null ? null : recipe.getRecipeOutput();
            if (result != null && result.getItem() == target.getItem()
                    && result.getItemDamage() == target.getItemDamage()) previous.add(recipe);
        }
        if (!addTokens(source, output, ingredients, settings, logger)) return false;
        for (java.util.Iterator<net.minecraft.item.crafting.IRecipe> it = recipes.iterator(); it.hasNext();) {
            if (previous.contains(it.next())) it.remove();
        }
        return true;
    }

    public static boolean addTokens(String source, String output, String[][] ingredients,
            GeneratedRecipeSettings settings, Logger logger) {
        attempted++;
        if (ingredients == null || ingredients.length == 0
                || ingredients.length > settings.getMaximumHeight()) {
            throw new IllegalArgumentException("Recipe height outside generated 3x3 grid: " + source);
        }
        int width = ingredients[0].length;
        if (width == 0 || width > settings.getMaximumWidth()) {
            throw new IllegalArgumentException("Recipe width outside generated 3x3 grid: " + source);
        }

        ItemStack resolvedOutput = GeneratedValueResolver.item(output, source + " output", settings, logger);
        if (resolvedOutput == null) return false;
        List<Object> recipe = new ArrayList<Object>();
        List<Object> values = new ArrayList<Object>();
        for (int row = 0; row < ingredients.length; row++) {
            if (ingredients[row] == null || ingredients[row].length != width) {
                throw new IllegalArgumentException("Uneven generated recipe grid: " + source);
            }
            StringBuilder pattern = new StringBuilder(width);
            for (int column = 0; column < width; column++) {
                String token = ingredients[row][column];
                if (token == null || "null".equals(token)) {
                    pattern.append(' ');
                    continue;
                }
                Object ingredient = GeneratedValueResolver.ingredient(token,
                        source + " [" + row + ',' + column + ']', settings, logger);
                if (GeneratedValueResolver.isMissing(ingredient)) return false;
                int keyIndex = indexOfIngredient(values, ingredient);
                if (keyIndex < 0) {
                    values.add(ingredient);
                    keyIndex = values.size() - 1;
                }
                if (keyIndex >= 52) throw new IllegalArgumentException("Too many unique ingredients: " + source);
                pattern.append(keyCharacter(keyIndex));
            }
            recipe.add(pattern.toString());
        }
        for (int index = 0; index < values.size(); index++) {
            recipe.add(Character.valueOf(keyCharacter(index)));
            recipe.add(values.get(index));
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(resolvedOutput, recipe.toArray(new Object[recipe.size()])));
        registered++;
        return true;
    }

    public static boolean add(String source, GeneratedStackRef output, String[] pattern,
            GeneratedRecipeSettings settings, Logger logger, Object... keys) {
        validatePattern(pattern, settings);
        if (keys == null || (keys.length & 1) != 0) {
            throw new IllegalArgumentException("Recipe keys must be character/value pairs: " + source);
        }

        ItemStack resolvedOutput = resolve(output, source + " output", settings, logger);
        if (resolvedOutput == null) return false;

        List<Object> recipe = new ArrayList<Object>(pattern.length + keys.length);
        for (String row : pattern) recipe.add(row);

        for (int index = 0; index < keys.length; index += 2) {
            Object symbol = keys[index];
            if (!(symbol instanceof Character)) {
                throw new IllegalArgumentException("Recipe key is not a character: " + source);
            }
            Object ingredient = keys[index + 1];
            if (ingredient instanceof GeneratedStackRef) {
                ingredient = resolve((GeneratedStackRef) ingredient,
                        source + " ingredient '" + symbol + "'", settings, logger);
                if (ingredient == null) return false;
            } else if (ingredient instanceof ItemStack) {
                ingredient = ((ItemStack) ingredient).copy();
            } else if (!(ingredient instanceof String)) {
                throw new IllegalArgumentException("Unsupported ingredient for " + source + ": " + ingredient);
            }
            recipe.add(symbol);
            recipe.add(ingredient);
        }

        GameRegistry.addRecipe(new ShapedOreRecipe(resolvedOutput, recipe.toArray(new Object[recipe.size()])));
        return true;
    }

    private static void validatePattern(String[] pattern, GeneratedRecipeSettings settings) {
        if (pattern == null || pattern.length == 0 || pattern.length > settings.getMaximumHeight()) {
            throw new IllegalArgumentException("Recipe height must be between 1 and "
                    + settings.getMaximumHeight());
        }
        int width = pattern[0] == null ? 0 : pattern[0].length();
        if (width == 0 || width > settings.getMaximumWidth()) {
            throw new IllegalArgumentException("Recipe width must be between 1 and "
                    + settings.getMaximumWidth());
        }
        for (String row : pattern) {
            if (row == null || row.length() != width) {
                throw new IllegalArgumentException("Every recipe row must have the same width");
            }
        }
    }

    private static ItemStack resolve(GeneratedStackRef reference, String location,
            GeneratedRecipeSettings settings, Logger logger) {
        if (reference == null) throw new IllegalArgumentException("Missing stack reference at " + location);
        ItemStack stack = reference.resolve();
        if (stack != null) return stack;

        String message = "Missing generated recipe reference " + reference + " at " + location;
        switch (settings.getMissingReferencePolicy()) {
            case WARN:
                logger.warn(message + "; recipe disabled");
                break;
            case FAIL:
                throw new IllegalStateException(message);
            case IGNORE:
            default:
                break;
        }
        return null;
    }

    private static int indexOfIngredient(List<Object> values, Object candidate) {
        for (int index = 0; index < values.size(); index++) {
            Object existing = values.get(index);
            if (existing instanceof ItemStack && candidate instanceof ItemStack) {
                ItemStack left = (ItemStack) existing;
                ItemStack right = (ItemStack) candidate;
                if (ItemStack.areItemStacksEqual(left, right)) return index;
            } else if (existing.equals(candidate)) {
                return index;
            }
        }
        return -1;
    }

    private static char keyCharacter(int index) {
        return index < 26 ? (char) ('A' + index) : (char) ('a' + index - 26);
    }
}
