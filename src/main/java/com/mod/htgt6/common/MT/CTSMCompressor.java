package com.mod.htgt6.common.MT;


import com.mod.htgt6.common.handler.recipe.assembler.AssemblerRecipeHandler;
import com.mod.htgt6.common.handler.recipe.compressor.CompressorRecipeHandler;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.htgt6.SMCompressor")
public class CTSMCompressor {

    static {
        System.out.println(
                "[HTGT6] CTSMC loaded"
        );
    }

    @ZenMethod
    public static void addRecipe(
            IItemStack output,
            int duration,
            int eut,
            int tier,
            IIngredient... inputs
    ) {
        // Dynamically size the array, capping it at a maximum of 3 items
        int size = Math.min(inputs.length, 3);
        ItemStack[] recipeInputs = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            if (inputs[i] == null) continue;

            if (inputs[i] instanceof IItemStack) {
                recipeInputs[i] = (ItemStack) ((IItemStack) inputs[i]).getInternal();
            }
        }

        ItemStack out = (ItemStack) output.getInternal();

        CompressorRecipeHandler.addRecipe(
                recipeInputs,
                out,
                duration,
                eut,
                tier
        );

        MineTweakerAPI.logInfo("[HTGT6] Added SMC recipe for " + out.getDisplayName());
    }
    @ZenMethod
    public static void removeRecipe (
            IItemStack output
    ) {
        ItemStack out =
                (ItemStack) output.getInternal();
        for (int i =
             CompressorRecipeHandler.recipes.size() - 1;
             i >= 0;
             i--) {

            CompressorRecipeHandler.CompressorRecipe r =
                    CompressorRecipeHandler.recipes.get(i);

            if (r.output.isItemEqual(out)) {

                CompressorRecipeHandler.recipes.remove(i);
            }
        }
        MineTweakerAPI.logInfo(
                "[HTGT6] Removed SMC recipe for "
                        + out.getDisplayName()
        );

    }

}
