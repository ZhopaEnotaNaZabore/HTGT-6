package com.mod.htgt6.common.handler.recipe;

import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.data.CS;
import gregapi.data.IL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.util.ST;
import ic2.api.item.IC2Items;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AssemblerRecipeHandler {

    // ==========================================
    // RECIPE LIST
    // ==========================================

    public static final List<AssemblerRecipe>
            recipes =
            new ArrayList<AssemblerRecipe>();


    // ==========================================
    // RECIPE CLASS
    // ==========================================

    public static class AssemblerRecipe {

        public final ItemStack[] input;

        public final ItemStack output;

        public final int duration;

        public final int euPerTick;

        public final int tier;

        public AssemblerRecipe(
                ItemStack[] input,
                ItemStack output,
                int duration,
                int euPerTick,
                int tier
        ) {this.input = input;
            this.output = output;
            this.duration = duration;
            this.euPerTick = euPerTick;
            this.tier = tier;
        }
    }

    // ==========================================
    // КОНСТРУКТОР ДЕБАГГЕР
    // ==========================================

    public static void addRecipe(
            ItemStack[] input,
            ItemStack output,
            int duration,
            int euPerTick,
            int tier
    ) {
        if (output == null || output.getItem() == null) {
            System.out.println("[HTGT6] Skipped adding recipe: Output item is NULL!");
            return;
        }

        // проверка
        for (ItemStack in : input) {
            if (in != null && in.getItem() == null) {
                System.out.println("[HTGT6] Skipped adding recipe for " + output.getDisplayName() + ": An input item is NULL!");
                return;
            }
        }

        recipes.add(new AssemblerRecipe(input, output, duration, euPerTick, tier));
    }

    // ==========================================
    // REGISTER DEFAULT RECIPES
    // ==========================================

    public static void registerRecipes() {

        // ======================================
        // TORCHES
        // ======================================
        ItemStack[] torchInput = new ItemStack[9];
        torchInput[0] = new ItemStack(Items.coal, 4);
        torchInput[1] = new ItemStack(Items.stick, 4);
        addRecipe(torchInput, new ItemStack(Blocks.torch, 16), 100, 8, 1
        );

        // ======================================
        // DISPENSER
        // ======================================

        ItemStack[] dispenserInput = new ItemStack[9];
        dispenserInput[0] = new ItemStack(Blocks.cobblestone, 6);
        dispenserInput[1] = new ItemStack(Items.redstone, 4);
        dispenserInput[3] = new ItemStack(Items.iron_ingot, 2);

        addRecipe(dispenserInput, new ItemStack(Blocks.dispenser), 400, 412, 3);


        //HV module


        // ItemStack[] TmoduleHV = new ItemStack[9];
       // TmoduleHV[0] = IL.Electric_Motor_HV.get(8);
       // TmoduleHV[2] = IL.Field_Generator_HV.get(4);
       // TmoduleHV[3] = advCircuit;
      //  TmoduleHV[4] = OP.wireGt01.mat(MT.Kanthal, 1);
       // addRecipe(TmoduleHV, new ItemStack(Tmodules.TierModuleHV), 5000, 120, 10);

        }


    // ==========================================
    // FIND RECIPE
    // ==========================================

    public static AssemblerRecipe findRecipe(
            ItemStack[] inventory
    ) {

        for (AssemblerRecipe recipe
                : recipes) {

            if (matches(recipe, inventory)) {

                if (canOutput(
                        inventory[9],
                        recipe.output
                )) {

                    return recipe;
                }
            }
        }

        return null;
    }

    // ==========================================
    // MATCH RECIPE
    // ==========================================

    public static boolean matches(
            AssemblerRecipe recipe,
            ItemStack[] inventory
    ) {

        for (int i = 0; i < 9; i++) {

            ItemStack recipeStack =
                    recipe.input[i];

            ItemStack invStack =
                    inventory[i];

            // EMPTY RECIPE SLOT
            if (recipeStack == null) {

                continue;
            }

            // EMPTY MACHINE SLOT
            if (invStack == null) {

                return false;
            }

            // WRONG ITEM
            if (invStack.getItem()
                    != recipeStack.getItem()) {

                return false;
            }

            // WRONG META
            if (invStack.getItemDamage()
                    != recipeStack.getItemDamage()) {

                return false;
            }

            // NOT ENOUGH ITEMS
            if (invStack.stackSize
                    < recipeStack.stackSize) {

                return false;
            }
        }

        return true;
    }

    // ==========================================
    // OUTPUT CHECK
    // ==========================================

    public static boolean canOutput(
            ItemStack outputSlot,
            ItemStack recipeOutput
    ) {

        // EMPTY SLOT
        if (outputSlot == null) {

            return true;
        }

        // DIFFERENT ITEM
        if (!outputSlot.isItemEqual(
                recipeOutput
        )) {

            return false;
        }

        // STACK LIMIT
        return outputSlot.stackSize
                + recipeOutput.stackSize
                <= outputSlot.getMaxStackSize();
    }

    // ==========================================
    // CONSUME INPUTS
    // ==========================================

    public static void consumeInputs(
            AssemblerRecipe recipe,
            ItemStack[] inventory
    ) {

        for (int i = 0; i < 9; i++) {

            ItemStack recipeStack =
                    recipe.input[i];

            if (recipeStack == null)
                continue;

            inventory[i].stackSize -=
                    recipeStack.stackSize;

            if (inventory[i].stackSize <= 0) {

                inventory[i] = null;
            }
        }
    }

    // ==========================================
    // OUTPUT RECIPE
    // ==========================================

    public static void outputRecipe(
            AssemblerRecipe recipe,
            ItemStack[] inventory
    ) {

        // OUTPUT SLOT = 9

        if (inventory[9] == null) {

            inventory[9] =
                    recipe.output.copy();

        } else {

            inventory[9].stackSize +=
                    recipe.output.stackSize;
        }
    }

    // ==========================================
    // OVERCLOCK DURATION
    // ==========================================

    public static int getOverclockedDuration(
            AssemblerRecipe recipe,
            int machineTier
    ) {

        if (machineTier <= recipe.tier) {

            return recipe.duration;
        }

        int diff =
                machineTier - recipe.tier;

        int result =
                recipe.duration;

        for (int i = 0; i < diff; i++) {

            result /= 2;
        }

        return Math.max(result, 1);
    }

    // ==========================================
    // OVERCLOCK EU/T
    // ==========================================

    public static int getOverclockedEUt(
            AssemblerRecipe recipe,
            int machineTier
    ) {

        if (machineTier <= recipe.tier) {

            return recipe.euPerTick;
        }

        int diff =
                machineTier - recipe.tier;

        int result =
                recipe.euPerTick;

        for (int i = 0; i < diff; i++) {

            result *= 4;
        }

        return result;
    }

    // ==========================================
    // TOTAL EU
    // ==========================================

    public static int getTotalEU(
            AssemblerRecipe recipe,
            int machineTier
    ) {

        return getOverclockedDuration(
                recipe,
                machineTier
        ) * getOverclockedEUt(
                recipe,
                machineTier
        );
    }
}