package com.mod.htgt6.common.handler.recipe.compressor;

import gregapi.GT_API;
import gregapi.config.ConfigCategories;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.util.ST;
import net.minecraft.item.ItemStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static gregapi.data.AM.Lead;
import static gregapi.data.AM.Mt;

public class CompressorRecipeHandler {

    // ==========================================
    // RECIPE LIST
    // ==========================================
    public static final List<CompressorRecipe> recipes = new ArrayList<CompressorRecipe>();

    // ==========================================
    // RECIPE CLASS DEFINITION
    // ==========================================
    public static class CompressorRecipe {
        public final ItemStack[] input; // Maximum of 3 inputs
        public final ItemStack output;
        public final int duration;
        public final int euPerTick;
        public final int tier;

        public CompressorRecipe(ItemStack[] input, ItemStack output, int duration, int euPerTick, int tier) {
            this.input = input;
            this.output = output;
            this.duration = duration;
            this.euPerTick = euPerTick;
            this.tier = tier;
        }
    }

    // ==========================================
    // REGISTER RECIPE MAP
    // ==========================================
    public static void addRecipe(ItemStack[] inputs, ItemStack output, int duration, int euPerTick, int tier) {
        if (inputs == null || inputs.length > 3 || output == null) return;
        recipes.add(new CompressorRecipe(inputs, output, duration, euPerTick, tier));
    }

    // ==========================================
    // SCAN INPUT SLOTS (0, 1, 2) FOR RECIPES
    // ==========================================
    @Nullable
    public static CompressorRecipe findRecipe(ItemStack[] inventory) {
        for (CompressorRecipe recipe : recipes) {
            if (matches(recipe, inventory)) {
                // Check if output slot (Slot 3) can receive the product stack
                if (inventory[3] == null) {
                    return recipe;
                }
                if (inventory[3].isItemEqual(recipe.output) && ItemStack.areItemStackTagsEqual(inventory[3], recipe.output)) {
                    if (inventory[3].stackSize + recipe.output.stackSize <= inventory[3].getMaxStackSize()) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    // Helper method to look at matching requirements unordered across slots 0-2
    private static boolean matches(CompressorRecipe recipe, ItemStack[] inventory) {
        boolean[] ingredientMatched = new boolean[recipe.input.length];
        boolean[] slotUsed = new boolean[3];

        for (int i = 0; i < recipe.input.length; i++) {
            ItemStack ingredient = recipe.input[i];
            if (ingredient == null) {
                ingredientMatched[i] = true;
                continue;
            }

            for (int slot = 0; slot < 3; slot++) {
                if (slotUsed[slot]) continue;
                ItemStack stackInSlot = inventory[slot];

                if (stackInSlot != null && stackInSlot.isItemEqual(ingredient) && stackInSlot.stackSize >= ingredient.stackSize) {
                    ingredientMatched[i] = true;
                    slotUsed[slot] = true;
                    break;
                }
            }
        }

        for (boolean matched : ingredientMatched) {
            if (!matched) return false;
        }
        return true;
    }



  public static void registerSMCrecipes() {

      addRecipe(
              new ItemStack[]{
                      new ItemStack(net.minecraft.init.Blocks.cobblestone, 9)
              },
              new ItemStack(net.minecraft.init.Items.diamond, 1),
              200,
              32,
              1
      );
      addRecipe(
              new ItemStack[] {
                      OP.plate.mat(MT.Lead, 9)
              },
              OP.plateDense.mat(MT.Lead, 1),
              200,
              32,
              1
      );

    }

    // ==========================================
    // CONSUME INPUT ITEMS
    // ==========================================
    public static void consumeInputs(CompressorRecipe recipe, ItemStack[] inventory) {
        for (ItemStack ingredient : recipe.input) {
            if (ingredient == null) continue;
            for (int slot = 0; slot < 3; slot++) {
                ItemStack stackInSlot = inventory[slot];
                if (stackInSlot != null && stackInSlot.isItemEqual(ingredient)) {
                    inventory[slot].stackSize -= ingredient.stackSize;
                    if (inventory[slot].stackSize <= 0) {
                        inventory[slot] = null;
                    }
                    break;
                }
            }
        }
    }

    // ==========================================
    // DISPENSE OUTPUT ITEM INTO SLOT 3
    // ==========================================
    public static void outputRecipe(CompressorRecipe recipe, ItemStack[] inventory) {
        if (inventory[3] == null) {
            inventory[3] = recipe.output.copy();
        } else if (inventory[3].isItemEqual(recipe.output)) {
            inventory[3].stackSize += recipe.output.stackSize;
        }
    }

    // ==========================================
    // OVERCLOCK DURATION ALGORITHM
    // ==========================================
    public static int getOverclockedDuration(CompressorRecipe recipe, int machineTier) {
        if (machineTier <= recipe.tier) return recipe.duration;
        int diff = machineTier - recipe.tier;
        int result = recipe.duration;
        for (int i = 0; i < diff; i++) {
            result /= 2;
        }
        return Math.max(result, 1);
    }

    // ==========================================
    // OVERCLOCK EU/T ALGORITHM
    // ==========================================
    public static int getOverclockedEUt(CompressorRecipe recipe, int machineTier) {
        if (machineTier <= recipe.tier) return recipe.euPerTick;
        int diff = machineTier - recipe.tier;
        int result = recipe.euPerTick;
        for (int i = 0; i < diff; i++) {
            result *= 4;
        }
        return result;
    }
}
