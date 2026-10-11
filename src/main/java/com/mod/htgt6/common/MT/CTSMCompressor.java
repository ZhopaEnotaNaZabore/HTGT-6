package com.mod.htgt6.common.MT;


import com.mod.htgt6.common.handler.recipe.RM.CompressorRecipeHandler;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
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
        ItemStack[] recipeInputs = convertInputs(inputs);

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
    public static void addImplosionRecipe(IItemStack output, int duration, int eut, int tier,
                                          IIngredient... inputs) {
        ItemStack out = (ItemStack) output.getInternal();
        CompressorRecipeHandler.addImplosionRecipe(convertInputs(inputs), out, duration, eut, tier);
        MineTweakerAPI.logInfo("[HTGT6] Added SMC implosion recipe for " + out.getDisplayName());
    }

    private static ItemStack[] convertInputs(IIngredient[] inputs) {
        int size = Math.min(inputs == null ? 0 : inputs.length, 32);
        ItemStack[] result = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            if (inputs[i] instanceof IItemStack) {
                ItemStack internal = (ItemStack) ((IItemStack) inputs[i]).getInternal();
                result[i] = internal == null ? null : internal.copy();
            }
        }
        return result;
    }
    @ZenMethod
    public static void removeRecipe (
            IItemStack output
    ) {
        ItemStack out =
                (ItemStack) output.getInternal();
        for (int i =
             CompressorRecipeHandler.INSTANCE.getRecipeList().size() - 1;
             i >= 0;
             i--) {

            MachineRecipe r = CompressorRecipeHandler.INSTANCE.getRecipeList().get(i);

            if (r.outputs.length > 0 && r.outputs[0].isItemEqual(out)) {

                CompressorRecipeHandler.INSTANCE.getRecipeList().remove(i);
            }
        }
        MineTweakerAPI.logInfo(
                "[HTGT6] Removed SMC recipe for "
                        + out.getDisplayName()
        );

    }

}
