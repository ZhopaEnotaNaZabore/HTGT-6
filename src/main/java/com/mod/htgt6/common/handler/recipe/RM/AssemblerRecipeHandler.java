package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator;
import com.mod.htgt6.common.block.greg.technological.hulls.blockHullDatabase;
import gregapi.data.IL;
import gregapi.data.MT;
import gregapi.data.OP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/** Shaped 3x3 assembler recipes backed by the shared API recipe model. */
public class AssemblerRecipeHandler extends RecipeMap<MachineRecipe> {

    public static final AssemblerRecipeHandler INSTANCE = new AssemblerRecipeHandler();

    private AssemblerRecipeHandler() {}

    public static void addRecipe(ItemStack[] inputs, ItemStack output,
                                 int duration, int euPerTick, int tier) {
        addRecipe(inputs, null, output, duration, euPerTick, tier);
    }

    /** Fluid inputs are optional; null and an empty array both create a normal item recipe. */
    public static void addRecipe(ItemStack[] inputs, FluidStack[] fluidInputs, ItemStack output,
                                 int duration, int euPerTick, int tier) {
        int componentTier = ComponentAssemblerRecipes.componentTier(output);
        if (componentTier == 4 || componentTier == 5) {
            ItemStack tag = ComponentAssemblerRecipes.selector();
            if (tag == null) throw new IllegalStateException("Missing selector tag for EV-IV component recipe");
            if (inputs.length > 9 || (inputs.length == 9 && inputs[8] != null))
                throw new IllegalArgumentException("EV-IV component recipe needs a free selector slot");
            inputs = java.util.Arrays.copyOf(inputs, 9);
            inputs[8] = tag;
        }
        INSTANCE.addRecipe(new MachineRecipe(inputs, new ItemStack[] {output},
                fluidInputs, null, duration, euPerTick, tier));
    }

    public static void registerRecipes() {
        ItemStack[] hullLuv = new ItemStack[9];
        hullLuv[0] = gt(OP.casingMachine, MT.Ir, 1);
        hullLuv[1] = mte(29616, 16);
        hullLuv[2] = gt(OP.gearGtSmall, MT.Iritanium, 8);
        hullLuv[3] = gt(OP.casingSmall, MT.Iritanium, 32);
        hullLuv[4] = gt(OP.screw, MT.Iritanium, 32);
        addRecipe(hullLuv, new ItemStack(blockHullDatabase.HullLUV, 1), 400, 6142, 5);

        ItemStack[] io1 = new ItemStack[9];
        io1[0] = gt(OP.casingSmall, MT.Ti, 8);
        io1[1] = mte(28916, 6);
        io1[2] = gt(OP.stickLong, MT.NeodymiumMagnetic, 1);
        io1[3] = mte(29003, 8);
        addRecipe(io1, IL.Electric_Motor_EV.get(1), 20, 512, 3);

        ItemStack[] io2 = new ItemStack[9];
        io2[0] = gt(OP.casingSmall, MT.TungstenSteel, 8);
        io2[1] = mte(29117, 6);
        io2[2] = gt(OP.stickLong, MT.NeodymiumMagnetic, 1);
        io2[3] = mte(29803, 8);
        addRecipe(io2, IL.Electric_Motor_IV.get(1), 20, 1024, 4);

        ItemStack[] io3 = new ItemStack[9];
        io3[0] = IL.Electric_Motor_EV.get(2);
        io3[1] = mte(28916, 6);
        io3[2] = gt(OP.plate, GT6MaterialAutoGenerator.POLY_ETHYLENE, 6);
        addRecipe(io3, IL.Conveyor_Module_EV.get(1), 20, 512, 3);

        ItemStack[] io4 = new ItemStack[9];
        io4[0] = IL.Electric_Motor_IV.get(2);
        io4[1] = mte(29117, 6);
        io4[2] = gt(OP.plate, GT6MaterialAutoGenerator.POLY_ETHYLENE, 6);
        addRecipe(io4, IL.Conveyor_Module_IV.get(1), 20, 1024, 4);

        ItemStack[] io5 = new ItemStack[9];
        io5[0] = IL.Electric_Motor_EV.get(1);
        io5[1] = mte(26182, 1);
        io5[2] = gt(OP.casingSmall, MT.Ti, 8);
        io5[3] = gt(OP.rotor, MT.Ti, 1);
        io5[4] = gt(OP.screw, MT.Ti, 8);
        io5[5] = gt(OP.ring, GT6MaterialAutoGenerator.POLY_ETHYLENE, 8);
        io5[6] = mte(28916, 2);
        addRecipe(io5, IL.Electric_Pump_EV.get(1), 20, 512, 3);

        ItemStack[] io6 = new ItemStack[9];
        io6[0] = IL.Electric_Motor_IV.get(1);
        io6[1] = mte(26202, 1);
        io6[2] = gt(OP.casingSmall, MT.TungstenSteel, 8);
        io6[3] = gt(OP.rotor, MT.TungstenSteel, 1);
        io6[4] = gt(OP.screw, MT.TungstenSteel, 8);
        io6[5] = gt(OP.ring, GT6MaterialAutoGenerator.POLY_ETHYLENE, 8);
        io6[6] = mte(29117, 2);
        addRecipe(io6, IL.Electric_Pump_IV.get(1), 20, 1024, 4);

        ItemStack[] io7 = new ItemStack[9];
        io7[0] = IL.Electric_Motor_EV.get(1);
        io7[1] = gt(OP.plate, MT.Ti, 6);
        io7[2] = gt(OP.casingSmall, MT.Ti, 10);
        io7[3] = gt(OP.screw, MT.Ti, 8);
        io7[4] = gt(OP.gearGtSmall, MT.Ti, 2);
        io7[5] = gt(OP.stickLong, MT.Ti, 2);
        io7[6] = mte(28916, 3);
        addRecipe(io7, IL.Electric_Piston_EV.get(1), 20, 512, 3);

        ItemStack[] io8 = new ItemStack[9];
        io8[0] = IL.Electric_Motor_IV.get(1);
        io8[1] = gt(OP.plate, MT.TungstenSteel, 6);
        io8[2] = gt(OP.casingSmall, MT.TungstenSteel, 10);
        io8[3] = gt(OP.screw, MT.TungstenSteel, 8);
        io8[4] = gt(OP.gearGtSmall, MT.TungstenSteel, 2);
        io8[5] = gt(OP.stickLong, MT.TungstenSteel, 2);
        io8[6] = mte(29117, 3);
        addRecipe(io8, IL.Electric_Piston_IV.get(1), 20, 1024, 4);

        ItemStack[] io9 = new ItemStack[9];
        io9[0] = IL.Electric_Motor_EV.get(2);
        io9[1] = IL.Electric_Piston_IV.get(1);
        io9[2] = gt(OP.casingSmall, MT.Ti, 10);
        io9[3] = gt(OP.screw, MT.Ti, 8);
        io9[4] = gt(OP.gearGtSmall, MT.Ti, 2);
        io9[5] = gt(OP.stickLong, MT.Ti, 2);
        io9[6] = IL.Circuit_Elite.get(1);
        io9[7] = mte(28916, 6);
        addRecipe(io9, IL.Robot_Arm_EV.get(1), 20, 512, 3);

        ItemStack[] io10 = new ItemStack[9];
        io10[0] = IL.Electric_Motor_IV.get(2);
        io10[1] = IL.Electric_Piston_IV.get(1);
        io10[2] = gt(OP.casingSmall, MT.TungstenSteel, 10);
        io10[3] = gt(OP.screw, MT.TungstenSteel, 8);
        io10[4] = gt(OP.gearGtSmall, MT.TungstenSteel, 2);
        io10[5] = gt(OP.stickLong, MT.TungstenSteel, 2);
        io10[6] = IL.Circuit_Master.get(1);
        io10[7] = mte(29117, 6);
        addRecipe(io10, IL.Robot_Arm_IV.get(1), 20, 1024, 4);

        ItemStack[] io11 = new ItemStack[9];
        io11[0] = gt(OP.stickLong, MT.NeodymiumMagnetic, 1);
        io11[1] = gt(OP.casingSmall, MT.Ti, 12);
        io11[2] = new ItemStack(Items.ender_eye, 1);
        io11[3] = mte(29003, 6);
        io11[4] = IL.Circuit_Elite.get(2);
        io11[5] = mte(28916, 4);
        addRecipe(io11, IL.Emitter_EV.get(1), 20, 512, 3);

        ItemStack[] io12 = new ItemStack[9];
        io12[0] = gt(OP.stickLong, MT.NeodymiumMagnetic, 1);
        io12[1] = gt(OP.casingSmall, MT.TungstenSteel, 12);
        io12[2] = new ItemStack(Items.ender_eye, 1);
        io12[3] = mte(29803, 6);
        io12[4] = IL.Circuit_Master.get(2);
        io12[5] = mte(29117, 4);
        addRecipe(io12, IL.Emitter_IV.get(1), 20, 1024, 4);

        ItemStack[] io13 = new ItemStack[9];
        io13[0] = gt(OP.stickLong, MT.NeodymiumMagnetic, 1);
        io13[1] = gt(OP.casingSmall, MT.Ti, 16);
        io13[2] = new ItemStack(Items.ender_eye, 1);
        io13[3] = mte(29003, 4);
        io13[4] = IL.Circuit_Elite.get(1);
        io13[5] = mte(28916, 2);
        addRecipe(io13, IL.Sensor_EV.get(1), 20, 512, 3);

        ItemStack[] io14 = new ItemStack[9];
        io14[0] = gt(OP.stickLong, MT.NeodymiumMagnetic, 1);
        io14[1] = gt(OP.casingSmall, MT.TungstenSteel, 16);
        io14[2] = new ItemStack(Items.ender_eye, 1);
        io14[3] = mte(29803, 4);
        io14[4] = IL.Circuit_Master.get(1);
        io14[5] = mte(29117, 2);
        addRecipe(io14, IL.Sensor_IV.get(1), 20, 1024, 4);
    }

    public static void registerTierModules() {
        // Bootstrap each upgrade on the preceding tier: never require the output module itself.
        addTierModuleRecipe(new ItemStack[] {
                new ItemStack(cpw.mods.fml.common.registry.GameRegistry.findItem("gregtech", "gt.multiitem.technological"), 8, 30302),
                gt(OP.wireGt02, MT.Ag, 32),
                gt(OP.bolt, MT.Al, 64),
                new ItemStack(cpw.mods.fml.common.registry.GameRegistry.findItem("HTGT6", "MVcoil"), 10),
                gt(OP.casingSmall, MT.Al, 64),
                IL.Field_Generator_MV.get(1)
        }, new ItemStack(com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules.TierModuleMV), 1200, 32, 1);
        addTierModuleRecipe(new ItemStack[] {
                new ItemStack(cpw.mods.fml.common.registry.GameRegistry.findItem("IC2", "itemPartCircuitAdv"), 8),
                gt(OP.wireGt02, MT.Kanthal, 32),
                gt(OP.bolt, MT.StainlessSteel, 64),
                new ItemStack(cpw.mods.fml.common.registry.GameRegistry.findItem("HTGT6", "HVcoil"), 10),
                gt(OP.casingSmall, MT.StainlessSteel, 64),
                IL.Field_Generator_HV.get(1)
        }, new ItemStack(com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules.TierModuleHV), 1200, 128, 2);
    }

    private static void addTierModuleRecipe(ItemStack[] inputs, ItemStack output,
            int duration, int euPerTick, int tier) {
        for (ItemStack input : inputs) {
            if (input == null || input.getItem() == null)
                throw new IllegalStateException("Missing ingredient for MV/HV tier module");
        }
        addRecipe(inputs, output, duration, euPerTick, tier);
    }

    public MachineRecipe findRecipe(ItemStack[] inventory, FluidTank[] tanks) {
        for (MachineRecipe recipe : recipeList) {
            if (matchesShaped(recipe, inventory) && hasEnoughFluids(recipe.fluidInputs, tanks)
                    && canOutput(inventory[9], recipe.outputs[0])) return recipe;
        }
        return null;
    }

    private boolean matchesShaped(MachineRecipe recipe, ItemStack[] inventory) {
        for (int i = 0; i < 9; i++) {
            ItemStack required = i < recipe.inputs.length ? recipe.inputs[i] : null;
            if (required == null) continue;
            ItemStack present = inventory[i];
            if (present == null || !areItemStacksEqual(required, present)
                    || present.stackSize < required.stackSize) return false;
        }
        return true;
    }

    public void consumeInputs(MachineRecipe recipe, ItemStack[] inventory) {
        for (int i = 0; i < 9 && i < recipe.inputs.length; i++) {
            ItemStack required = recipe.inputs[i];
            if (required == null) continue;
            inventory[i].stackSize -= required.stackSize;
            if (inventory[i].stackSize <= 0) inventory[i] = null;
        }
    }

    public void outputRecipe(MachineRecipe recipe, ItemStack[] inventory) {
        super.outputRecipe(recipe, inventory, 9);
    }

    private boolean hasEnoughFluids(FluidStack[] required, FluidTank[] tanks) {
        if (required == null || required.length == 0) return true;
        for (FluidStack fluid : required) {
            if (fluid == null) continue;
            int needed = 0;
            int available = 0;
            for (FluidStack other : required) {
                if (other != null && other.isFluidEqual(fluid)) needed += other.amount;
            }
            for (FluidTank tank : tanks) {
                if (tank.getFluid() != null && tank.getFluid().isFluidEqual(fluid)) available += tank.getFluidAmount();
            }
            if (available < needed) return false;
        }
        return true;
    }
}
