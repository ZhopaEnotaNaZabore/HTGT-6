package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;

import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.common.item.htgt6.fluids.dustsDatabase;
import gregapi.data.FL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class ElectromagneticProcessorRecipeMap extends RecipeMap<MachineRecipe> {

    public static final ElectromagneticProcessorRecipeMap INSTANCE = new ElectromagneticProcessorRecipeMap();

    private ElectromagneticProcessorRecipeMap() {
    }

    public void registerRecipes() {
        // Recipe 1: With Fluid
      /*  {
            ItemStack[] itemInputs = { gt(OP.stickLong, MT.NeodymiumMagnetic, 1) };
            ItemStack[] itemOutputs = { gt(OP.plate, MT.NeodymiumMagnetic, 2) };
            FluidStack[] fluidInputs = { FL.Lubricant.make(100) };

            addRecipe(new MachineRecipe(
                    itemInputs,
                    itemOutputs,
                    fluidInputs,
                    null,
                    200, 128, 2
            ));
       }


*/
        //bath recipes polarizer
        OreDictPrefix[] prefixesToMagnetize = new OreDictPrefix[] {
                OP.rotor,
                OP.stick,
                OP.stickLong,
                OP.casingSmall,
                OP.wireFine,
                OP.bolt,
                OP.screw,
                OP.ingot,
                OP.ring,
                OP.plate,
                OP.gearGt,
                OP.gearGtSmall

                // Add any other OP prefixes you need here
        };

        registerMagnetizationRecipes(prefixesToMagnetize, MT.Iron, MT.IronMagnetic, 100, 64, 1);
        registerMagnetizationRecipes(prefixesToMagnetize, MT.Steel, MT.SteelMagnetic, 100, 64, 1);
        registerMagnetizationRecipes(prefixesToMagnetize, MT.Neodymium, MT.NeodymiumMagnetic, 120, 156, 3);




        //  Recipe 2: Optional Fluid (No Fluid required)
        /*Polarizer EXAMPLE
        {
            ItemStack[] itemInputs = { gt(OP.stickLong, MT.Iron, 1) };
            ItemStack[] itemOutputs = { gt(OP.stickLong, MT.IronMagnetic, 1) };
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, null, null, 100, 64, 1));
        }
  */
        //Separator

        {
            ItemStack[] itemInputs = { gt(OP.dust, MT.OREMATS.Magnetite, 1) };
            ItemStack[] itemOutputs = {gt(OP.dust, MT.OREMATS.Hematite, 1), gt(OP.nugget, MT.Gold, 1), gt(OP.nugget, MT.Nickel, 1)};
            int[] chances = { 10000, 5000, 2500 }; // 100%, 50%, 25%
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, chances, null, null, 200, 128, 2
            ));
        }

        {
            ItemStack[] itemInputs = {new ItemStack(dustsDatabase.dustTitaniumMettalicSludge, 1)};
            ItemStack[] itemOutputs = {gt(OP.dust, MT.OREMATS.Rutile, 1), gt(OP.nugget, MT.Zirconium, 1), gt(OP.nugget, MT.Nd, 1)};
            int[] chances = { 10000, 210, 180 };
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, chances, null, null, 280, 410, 3
            ));
        }







   }

    private void registerMagnetizationRecipes(OreDictPrefix[] prefixes, OreDictMaterial baseMat, OreDictMaterial magMat, int duration, int euPerTick, int tier) {
        for (OreDictPrefix prefix : prefixes) {
            ItemStack[] itemInputs = { gt(prefix, baseMat, 1) };
            ItemStack[] itemOutputs = { gt(prefix, magMat, 1) };

            // Null checks in case a specific Prefix + Material combination isn't registered in GregTech
            if (itemInputs[0] != null && itemOutputs[0] != null) {
                addRecipe(new MachineRecipe(itemInputs, itemOutputs, null, null, duration, euPerTick, tier));
            }
        }
    }

    public MachineRecipe findRecipe(ItemStack inputItem, FluidTank tank) {
        for (MachineRecipe recipe : recipeList) {
            if (matches(recipe, inputItem, tank)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean matches(MachineRecipe recipe, ItemStack inputItem, FluidTank tank) {
        // Check Item Input
        if (recipe.inputs != null && recipe.inputs.length > 0 && recipe.inputs[0] != null) {
            ItemStack req = recipe.inputs[0];
            if (inputItem == null || !areItemStacksEqual(req, inputItem) || inputItem.stackSize < req.stackSize) {
                return false;
            }
        }

        // Check Optional Fluid Input
        if (recipe.fluidInputs != null && recipe.fluidInputs.length > 0 && recipe.fluidInputs[0] != null) {
            FluidStack reqFluid = recipe.fluidInputs[0];
            if (tank == null || tank.getFluid() == null) return false;
            if (!tank.getFluid().isFluidEqual(reqFluid) || tank.getFluidAmount() < reqFluid.amount) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifies output slot capacity allowing Deep Storage limit (4096).
     */
    public boolean canOutput(ItemStack[] inventory, int startOutputSlot, MachineRecipe recipe) {
        if (recipe.outputs == null) return true;

        for (int i = 0; i < recipe.outputs.length; i++) {
            ItemStack recipeOut = recipe.outputs[i];
            if (recipeOut == null) continue;

            int targetSlot = startOutputSlot + i;
            if (targetSlot >= inventory.length) return false;

            ItemStack currentOut = inventory[targetSlot];
            if (currentOut != null) {
                if (!currentOut.isItemEqual(recipeOut) || !ItemStack.areItemStackTagsEqual(currentOut, recipeOut)) {
                    return false;
                }
                // Allow stacking up to 4096 items for Deep Storage
                if (currentOut.stackSize + recipeOut.stackSize > 4096) {
                    return false;
                }
            }
        }
        return true;
    }


    public static long getOverclockedEUt(MachineRecipe recipe, int tier) {
        if (recipe == null) return 0;
        return MachineTierSystem.overclockEUt(recipe.euPerTick, recipe.minTier, tier);
    }
}
