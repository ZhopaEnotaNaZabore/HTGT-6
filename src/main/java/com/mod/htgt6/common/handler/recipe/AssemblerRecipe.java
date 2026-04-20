package com.mod.htgt6.common.handler.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;



public class AssemblerRecipe {
    public final ItemStack[] inputs; // The 9 slots
    public final ItemStack output;
    public final int minTier;
    public final int duration;
    public final int euPerTick;

    public AssemblerRecipe(ItemStack output, int minTier, int duration, int euPerTick, ItemStack... inputs) {
        this.output = output;
        this.minTier = minTier;
        this.duration = duration;
        this.euPerTick = euPerTick;
        this.inputs = inputs;
    }

    public boolean matches(ItemStack[] inv, int machineTier) {
        if (machineTier < this.minTier) return false;

        // Simple check: do all inputs match slots 0-8?
        for (int i = 0; i < 9; i++) {
            ItemStack recipeStack = (i < inputs.length) ? inputs[i] : null;
            ItemStack invStack = inv[i];

            if (recipeStack == null && invStack == null) continue;
            if (recipeStack == null || invStack == null) return false;
            if (recipeStack.getItem() != invStack.getItem()) return false;
            if (invStack.stackSize < recipeStack.stackSize) return false;
        }
        return true;
    }
}