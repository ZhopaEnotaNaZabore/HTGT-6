package com.mod.htgt6.common.handler.recipes.modpackgenerated.removed;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

/** Native equivalent of MineTweaker recipes.remove(output). */
public final class GeneratedCraftingRemoval {

    private static int removedRecipes;

    private GeneratedCraftingRemoval() {}

    public static int remove(String source, String output,
            GeneratedRecipeSettings settings, Logger logger) {
        int removed = 0;
        for (ItemStack stack : GeneratedValueResolver.matchingItems(output, source, settings, logger)) {
            if (stack == null || stack.getItem() == null) continue;
            removed += removeDirectly(stack);
        }
        removedRecipes += removed;
        if (removed == 0) logger.debug("No crafting recipe matched {} at {}", output, source);
        return removed;
    }

    /**
     * MineTweaker recipes.remove(output) removes matching entries directly.
     * CR.remout cannot be used here because GT6 deliberately protects recipes
     * whose ICraftingRecipeGT.isRemovableByGT() returns false.
     */
    @SuppressWarnings("unchecked")
    private static int removeDirectly(ItemStack target) {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        int removed = 0;
        for (Iterator<IRecipe> iterator = recipes.iterator(); iterator.hasNext();) {
            IRecipe recipe = iterator.next();
            if (recipe == null) continue;
            ItemStack result = recipe.getRecipeOutput();
            if (!sameOutput(target, result)) continue;
            iterator.remove();
            removed++;
        }
        return removed;
    }

    private static boolean sameOutput(ItemStack target, ItemStack result) {
        if (result == null || result.getItem() == null || target.getItem() != result.getItem()) return false;
        int targetMeta = target.getItemDamage();
        return targetMeta == OreDictionary.WILDCARD_VALUE || targetMeta == result.getItemDamage();
    }

    public static int consumeRemovedRecipeCount() {
        int result = removedRecipes;
        removedRecipes = 0;
        return result;
    }
}
