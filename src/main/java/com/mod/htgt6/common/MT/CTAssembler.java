package com.mod.htgt6.common.MT;

import com.mod.htgt6.common.handler.recipe.AssemblerRecipeHandler;

import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.mc1710.item.MCItemStack;

import net.minecraft.item.ItemStack;

import com.mod.htgt6.common.handler.recipe.AssemblerRecipeHandler;

import minetweaker.MineTweakerAPI;

import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;

import net.minecraft.item.ItemStack;

import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.htgt6.Assembler")
public class CTAssembler {

    static {

        System.out.println(
                "[HTGT6] CTAssembler loaded"
        );
    }

    // ==========================================
    // ADD RECIPE
    // ==========================================

    @ZenMethod
    public static void addRecipe(
            IItemStack output,
            int duration,
            int eut,
            int tier,
            IIngredient... inputs
    ) {

        ItemStack[] recipeInputs =
                new ItemStack[9];

        for (int i = 0;
             i < inputs.length && i < 9;
             i++) {

            if (inputs[i] == null)
                continue;

            if (inputs[i] instanceof IItemStack) {

                recipeInputs[i] =
                        (ItemStack)
                                ((IItemStack) inputs[i])
                                        .getInternal();
            }
        }

        ItemStack out =
                (ItemStack) output.getInternal();

        AssemblerRecipeHandler.addRecipe(
                recipeInputs,
                out,
                duration,
                eut,
                tier
        );

        MineTweakerAPI.logInfo(
                "[HTGT6] Added assembler recipe for "
                        + out.getDisplayName()
        );
    }

    // ==========================================
    // REMOVE RECIPE
    // ==========================================

    @ZenMethod
    public static void removeRecipe(
            IItemStack output
    ) {

        ItemStack out =
                (ItemStack) output.getInternal();

        for (int i =
             AssemblerRecipeHandler.recipes.size() - 1;
             i >= 0;
             i--) {

            AssemblerRecipeHandler.AssemblerRecipe r =
                    AssemblerRecipeHandler.recipes.get(i);

            if (r.output.isItemEqual(out)) {

                AssemblerRecipeHandler.recipes.remove(i);
            }
        }

        MineTweakerAPI.logInfo(
                "[HTGT6] Removed assembler recipe for "
                        + out.getDisplayName()
        );
    }

    // ==========================================
    // DEBUG TEST
    // ==========================================

    @ZenMethod
    public static void test() {

        MineTweakerAPI.logInfo(
                "[HTGT6] CTAssembler test successful"
        );
    }
}