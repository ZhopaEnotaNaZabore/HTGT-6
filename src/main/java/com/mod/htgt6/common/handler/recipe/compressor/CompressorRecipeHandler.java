package com.mod.htgt6.common.handler.recipe.compressor;

import galaxyspace.systems.SolarSystem.planets.overworld.items.ItemCompressedPlates;
import gregapi.GT_API;
import gregapi.config.ConfigCategories;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.item.ItemIntegratedCircuit;
import gregapi.util.ST;
import net.minecraft.item.ItemStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static gregapi.data.AM.*;

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
        //LV recipes
      addRecipe(new ItemStack[] {OP.plate.mat(MT.Lead, 9)}, OP.plateDense.mat(MT.Lead, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Iron, 9)}, OP.plateDense.mat(MT.Iron, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Copper, 9)}, OP.plateDense.mat(MT.Copper, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.AnnealedCopper, 9)}, OP.plateDense.mat(MT.AnnealedCopper, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Gold, 9)}, OP.plateDense.mat(MT.Gold, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Bronze, 9)}, OP.plateDense.mat(MT.Bronze, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Aluminium, 9)}, OP.plateDense.mat(MT.Aluminium, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Brass, 9)}, OP.plateDense.mat(MT.Brass, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Steel, 9)}, OP.plateDense.mat(MT.Steel, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.TinAlloy, 9)}, OP.plateDense.mat(MT.TinAlloy, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Sn, 9)}, OP.plateDense.mat(MT.Sn, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Arsenic, 9)}, OP.plateDense.mat(MT.Arsenic, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.SolderingAlloy, 9)}, OP.plateDense.mat(MT.SolderingAlloy, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Sb, 9)}, OP.plateDense.mat(MT.Sb, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Silver, 9)}, OP.plateDense.mat(MT.Silver, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.BatteryAlloy, 9)}, OP.plateDense.mat(MT.BatteryAlloy, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Chromium, 9)}, OP.plateDense.mat(MT.Chromium, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Nickel, 9)}, OP.plateDense.mat(MT.Nickel, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Plastic, 9)}, OP.plateDense.mat(MT.Plastic, 1), 1600, 32, 1);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Constantan, 9)}, OP.plateDense.mat(MT.Constantan, 1), 1600, 32, 1);



      //MV
      addRecipe( new ItemStack[] {OP.plate.mat(MT.AluminiumBrass, 9)}, OP.plateDense.mat(MT.AluminiumBrass, 1), 1600, 128, 2);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Cobalt, 9)}, OP.plateDense.mat(MT.Cobalt, 1), 1600, 128, 2);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.VanadiumSteel, 9)}, OP.plateDense.mat(MT.VanadiumSteel, 1), 1600, 128, 2);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.V, 9)}, OP.plateDense.mat(MT.V, 1), 1600, 128, 2);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Electrum, 9)}, OP.plateDense.mat(MT.Electrum, 1), 1600, 128, 2);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.BlueAlloy, 9)}, OP.plateDense.mat(MT.BlueAlloy, 1), 1600, 128, 2);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Electrotine, 9)}, OP.plateDense.mat(MT.Electrotine, 1), 1600, 128, 2);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Kanthal, 9)}, OP.plateDense.mat(MT.Kanthal, 1), 1600, 128, 2);

      //HV
      addRecipe( new ItemStack[] {OP.plate.mat(MT.StainlessSteel, 9)}, OP.plateDense.mat(MT.StainlessSteel, 1), 1600, 512, 3);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Nichrome, 9)}, OP.plateDense.mat(MT.Nichrome, 1), 1600, 512, 3);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Titanium, 9)}, OP.plateDense.mat(MT.Titanium, 1), 1600, 512, 3);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Ultimet, 9)}, OP.plateDense.mat(MT.Ultimet, 1), 1600, 512, 3);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Trinium, 9)}, OP.plateDense.mat(MT.Trinium, 1), 1600, 512, 3);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Platinum, 9)}, OP.plateDense.mat(MT.Platinum, 1), 1600, 512, 3);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Palladium, 9)}, OP.plateDense.mat(MT.Palladium, 1), 1600, 512, 3);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Ta, 9)}, OP.plateDense.mat(MT.Ta, 1), 1600, 512, 3);

      //EV
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Tungsten, 9)}, OP.plateDense.mat(MT.Tungsten, 1), 1600, 2048, 4);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.TungstenSteel, 9)}, OP.plateDense.mat(MT.TungstenSteel, 1), 1600, 2048, 4);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.TungstenCarbide, 9)}, OP.plateDense.mat(MT.TungstenCarbide, 1), 1600, 2048, 4);


      //IV
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Osmium, 9)}, OP.plateDense.mat(MT.Osmium, 1), 1600, 2048, 4);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Iridium, 9)}, OP.plateDense.mat(MT.Iridium, 1), 1600, 2048, 4);
      addRecipe( new ItemStack[] {OP.plate.mat(MT.Iritanium, 9)}, OP.plateDense.mat(MT.Iritanium, 1), 1600, 2048, 4);

//GALACTICRAFT





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
