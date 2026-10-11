package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.common.handler.FluidDatabase;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import gregapi.data.MT;
import gregapi.data.OP;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

/** Independent map: no automatic import of chemical-plant/mixer recipes. */
public final class FlotationChamberRecipeHandler extends RecipeMap<ChemicalPlantRecipe> {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Flotation");
    public static final FlotationChamberRecipeHandler INSTANCE = new FlotationChamberRecipeHandler();
    private boolean defaultsRegistered;
    private FlotationChamberRecipeHandler() {}

    public static synchronized ChemicalPlantRecipe addRecipe(ItemStack ore, FluidStack reagent,
            FluidStack solution, int ticks, int eut, int tier) {
        if (ore == null || ore.getItem() == null || ore.stackSize <= 0
                || reagent == null || reagent.getFluid() == null || reagent.amount <= 0
                || solution == null || solution.getFluid() == null || solution.amount <= 0
                || ticks <= 0 || eut <= 0 || tier < 1) throw new IllegalArgumentException("Invalid flotation recipe");
        ChemicalPlantRecipe recipe = new ChemicalPlantRecipe("HiTech6 Flotation",
                new ItemStack[]{ore.copy()}, new ItemStack[0], null,
                new FluidStack[]{reagent.copy()}, new FluidStack[]{solution.copy()}, ticks, eut, tier, -1);
        INSTANCE.addRecipe(recipe);
        return recipe;
    }

    /** Called after GT6 and modpack scripts have registered their ores and fluids. */
    public synchronized void registerDefaults() {
        if (defaultsRegistered) return;
        FluidStack acid = MT.H2SO4.liquid(gregapi.data.CS.U, true);
        if (acid == null || acid.getFluid() == null || FluidDatabase.CHALCOPYRITE_SOLUTION == null) {
            LOGGER.warn("Flotation recipes unavailable: required fluids were not registered");
            return;
        }
        acid.amount = 10000;
        int added = 0;
        for (ItemStack ore : chalcopyriteOres()) {
            if (ore == null || ore.getItem() == null) continue;
            boolean duplicate = false;
            for (ChemicalPlantRecipe existing : recipeList) {
                ItemStack input = existing.inputs[0];
                if (input.getItem() == ore.getItem() && input.getItemDamage() == ore.getItemDamage()
                        && ItemStack.areItemStackTagsEqual(input, ore)) { duplicate = true; break; }
            }
            if (duplicate) continue;
            ItemStack input = ore.copy(); input.stackSize = 32;
            addRecipe(input, acid, new FluidStack(FluidDatabase.CHALCOPYRITE_SOLUTION, 10000), 400, 32, 1);
            added++;
        }
        if (added == 0 && recipeList.isEmpty()) {
            LOGGER.warn("Flotation recipes unavailable: no crushed chalcopyrite");
            return;
        }
        defaultsRegistered = true;
        LOGGER.info("Flotation: registered {} chalcopyrite ore variants", added);
    }

    private static List<ItemStack> chalcopyriteOres() {
        List<ItemStack> ores = new ArrayList<ItemStack>();
        ItemStack crushed = OP.crushed.mat(MT.OREMATS.Chalcopyrite, 1);
        if (crushed != null && crushed.getItem() != null) ores.add(crushed);
        // Exact prefix: neither ore blocks nor purified/centrifuged products.
        ores.addAll(OreDictionary.getOres("crushedChalcopyrite"));
        return ores;
    }
}
