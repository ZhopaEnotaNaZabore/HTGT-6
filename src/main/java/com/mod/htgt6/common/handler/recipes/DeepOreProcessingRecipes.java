package com.mod.htgt6.common.handler.recipes;

import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.common.handler.FluidDatabase;
import com.mod.htgt6.common.handler.recipe.RM.ElectromagneticProcessorRecipeMap;
import com.mod.htgt6.common.handler.recipe.RM.IndustrialCentrifugeRecipeHandler;
import gregapi.data.MT;
import gregapi.data.OP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/** Native continuation of the 32-ore / 10000 mB flotation batch. */
public final class DeepOreProcessingRecipes {
    private static boolean registered;
    private DeepOreProcessingRecipes() { }
    public static synchronized void register() {
        if (registered) return;
        ItemStack iron=OP.dust.mat(MT.Fe,8);
        ItemStack sludge=OP.dustTiny.mat(MT.PlatinumGroupSludge,1);
        FluidStack vitriol=MT.BlueVitriol.liquid(gregapi.data.CS.U,true);
        if(iron==null||sludge==null||vitriol==null||vitriol.getFluid()==null
                ||FluidDatabase.CHALCOPYRITE_SOLUTION==null||FluidDatabase.ENRICHED_COPPER_SOLUTION==null)
            throw new IllegalStateException("Missing deep ore processing ingredients");
        // One trace-size PGM by-product per complete flotation batch; no pure PGM bypass.
        vitriol.amount=8000;
        ElectromagneticProcessorRecipeMap.INSTANCE.addRecipe(new MachineRecipe(
                new ItemStack[0],new ItemStack[]{iron},
                new FluidStack[]{new FluidStack(FluidDatabase.CHALCOPYRITE_SOLUTION,10000)},
                new FluidStack[]{new FluidStack(FluidDatabase.ENRICHED_COPPER_SOLUTION,10000)},
                400,128,2));
        IndustrialCentrifugeRecipeHandler.addRecipe(new ItemStack[0],
                new FluidStack[]{new FluidStack(FluidDatabase.ENRICHED_COPPER_SOLUTION,10000)},
                new ItemStack[]{sludge},new FluidStack[]{vitriol},600,480,3);
        registered=true;
    }
}
