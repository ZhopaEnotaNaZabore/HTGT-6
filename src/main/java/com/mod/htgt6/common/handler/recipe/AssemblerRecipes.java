package com.mod.htgt6.common.handler.recipe;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class AssemblerRecipes {
    private static final List<AssemblerRecipe> recipeList = new ArrayList<AssemblerRecipe>();

    public static void init() {
        // Example: 1 Coal (Slot 0) -> 1 Diamond (Tier 2 req, 200 ticks, 32 EU/t)
        addRecipe(new ItemStack(Items.diamond), 2, 200, 32, new ItemStack(Items.coal));
    }

    public static void addRecipe(ItemStack out, int tier, int time, int eu, ItemStack... in) {
        recipeList.add(new AssemblerRecipe(out, tier, time, eu, in));
    }

    public static AssemblerRecipe getMatchingRecipe(ItemStack[] inv, int tier) {
        for (AssemblerRecipe r : recipeList) {
            if (r.matches(inv, tier)) return r;
        }
        return null;
    }
}