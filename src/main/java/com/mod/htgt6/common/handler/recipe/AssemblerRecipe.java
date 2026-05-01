package com.mod.htgt6.common.handler.recipe;

import net.minecraft.item.ItemStack;

public class AssemblerRecipe {
    public final ItemStack[] input; // The 3x3 grid (9 slots)
    public final ItemStack output;
    public final int duration;    // Total ticks to craft
    public final int euPerTick;   // Energy cost per tick
    public final int tier;        // Required machine tier

    public AssemblerRecipe(ItemStack[] input, ItemStack output, int duration, int euPerTick, int tier) {
        this.input = input;
        this.output = output;
        this.duration = duration;
        this.euPerTick = euPerTick;
        this.tier = tier;
    }

    public boolean matches(ItemStack[] inv) {
        for (int i = 0; i < 9; i++) {
            ItemStack recipeStack = this.input[i];
            ItemStack invStack = inv[i];

            if (recipeStack == null) {
                if (invStack != null) return false;
            } else {
                if (invStack == null) return false;
                if (invStack.getItem() != recipeStack.getItem()) return false;
                if (invStack.getItemDamage() != recipeStack.getItemDamage()) return false;
                if (invStack.stackSize < recipeStack.stackSize) return false;
            }
        }
        return true;
    }
}