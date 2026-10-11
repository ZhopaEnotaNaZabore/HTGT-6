package com.mod.htgt6.common.agent;

import gregapi.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;

import java.util.Map;

/** Read-only snapshot over the live recipe registries. */
public final class AgentRecipeInspector {
    private AgentRecipeInspector() {}

    @SuppressWarnings("rawtypes")
    public static String summary() {
        int gtRecipes = 0;
        for (Recipe.RecipeMap map : Recipe.RecipeMap.RECIPE_MAPS.values()) gtRecipes += map.mRecipeList.size();
        return "Рецепты: crafting=" + CraftingManager.getInstance().getRecipeList().size()
                + ", furnace=" + FurnaceRecipes.smelting().getSmeltingList().size()
                + ", GT6 maps=" + Recipe.RecipeMap.RECIPE_MAPS.size() + ", GT6 entries=" + gtRecipes;
    }

    @SuppressWarnings("rawtypes")
    public static String findForOutput(ItemStack target) {
        if (target == null) return "Возьмите проверяемый предмет в руку.";
        StringBuilder out = new StringBuilder("Выход: ").append(stackName(target));
        int matches = 0;
        for (Object value : CraftingManager.getInstance().getRecipeList()) {
            if (!(value instanceof IRecipe)) continue;
            ItemStack result = ((IRecipe) value).getRecipeOutput();
            if (same(result, target)) {
                matches++;
                append(out, "crafting", value.getClass().getSimpleName());
            }
        }
        for (Object obj : FurnaceRecipes.smelting().getSmeltingList().entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            if (entry.getValue() instanceof ItemStack && same((ItemStack) entry.getValue(), target)) {
                matches++;
                append(out, "furnace", stackName((ItemStack) entry.getKey()));
            }
        }
        for (Recipe.RecipeMap map : Recipe.RecipeMap.RECIPE_MAPS.values()) {
            int inMap = 0;
            for (Recipe recipe : map.mRecipeList) {
                if (recipe == null || recipe.mOutputs == null) continue;
                for (ItemStack result : recipe.mOutputs) if (same(result, target)) { inMap++; break; }
            }
            if (inMap > 0) {
                matches += inMap;
                append(out, "GT6 " + map.mNameInternal, inMap + " рецепт(ов)");
            }
        }
        if (matches == 0) out.append("\nСовпадений в crafting/furnace/GT6 не найдено.");
        else out.append("\nВсего совпадений: ").append(matches);
        return out.toString();
    }

    private static void append(StringBuilder out, String source, String detail) {
        if (out.length() < 1400) out.append("\n").append(source).append(": ").append(detail);
    }

    private static boolean same(ItemStack left, ItemStack right) {
        return left != null && right != null && left.getItem() == right.getItem()
                && (left.getItemDamage() == right.getItemDamage() || left.getItemDamage() == 32767);
    }

    private static String stackName(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "<null>";
        Object key = net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem());
        return stack.getDisplayName() + " [" + key + ":" + stack.getItemDamage() + "] x" + stack.stackSize;
    }
}
