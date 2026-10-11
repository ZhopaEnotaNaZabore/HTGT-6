package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.IndustrialCrusherRecipeHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Exercises native recipe copying and map selection using the same policy called by the controller. */
public final class IndustrialCrusherRecipeCheck {
    public static void main(String[] args) throws Exception {
        Item item = new Item();
        ItemStack input = new ItemStack(item, 2), output = new ItemStack(item, 3);
        int[] chances = {2500};
        ChemicalPlantRecipe nativeRecipe = IndustrialCrusherRecipeHandler.addRecipe(-1,
                new ItemStack[] {input}, null, new ItemStack[] {output}, null, chances, 100, 32, 1);
        require(nativeRecipe != null, "Native registration");
        input.stackSize = 64; output.stackSize = 64; chances[0] = 0;
        require(nativeRecipe.inputs[0].stackSize == 2 && nativeRecipe.outputs[0].stackSize == 3,
                "Recipe retained mutable caller stacks");
        ChemicalPlantRecipe crusher = recipe("GT6 Crusher", item), shredder = recipe("GT6 Shredder", item);
        for (int selected = 0; selected <= 2; selected++) {
            require(IndustrialCrusherRecipeHandler.acceptsMode(selected, crusher) == (selected != 2), "Crusher mode filter");
            require(IndustrialCrusherRecipeHandler.acceptsMode(selected, shredder) == (selected != 1), "Shredder mode filter");
            require(IndustrialCrusherRecipeHandler.acceptsMode(selected, nativeRecipe), "Native recipes must remain available");
        }
        System.out.println("IndustrialCrusherRecipeCheck: OK");
    }
    private static ChemicalPlantRecipe recipe(String source, Item item) {
        return new ChemicalPlantRecipe(source, new ItemStack[] {new ItemStack(item)},
                new ItemStack[] {new ItemStack(item)}, new int[] {10000}, new FluidStack[0],
                new FluidStack[0], 100, 32, 1, -1);
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
