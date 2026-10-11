package com.mod.htgt6.common.handler.recipe.RM;


import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import gregapi.data.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;


public class LineAssemblerRecipeHandler extends RecipeMap<MachineRecipe> {

    public static final LineAssemblerRecipeHandler INSTANCE = new LineAssemblerRecipeHandler();

    private LineAssemblerRecipeHandler() {
    }


    // gt(), mte() and molten() are inherited from the shared RecipeMap API.


    //ДЛЯ ЕБАНЫХ КАБЕЛЕЙ КОТОРЫЕ МОЗГ ВЫЕБАЛИ НА ДВОЕ СУТОК, mte(aID, aAMount),
    public void registerRecipes() {
// Only register if the required GT6 material/fluid exists.

        //   if (MT.Trinium != null && MT.Trinium.mLiquid != null)
        {

            ItemStack[] itemInputs = {gt(OP.stickLong, MT.NeodymiumMagnetic, 2), OP.casingSmall.mat(MT.Iritanium, 16), gt(OP.wireFine, MT.Iridium, 64),
                    gt(OP.wireFine, MT.Iridium, 64), gt(OP.wireFine, MT.Iridium, 64), gt(OP.wireFine, MT.Iridium, 64), OP.screw.mat(MT.Iritanium, 32), mte(29617, 8)};
            ItemStack[] itemOutputs = {IL.Electric_Motor_LuV.get(1)};
            FluidStack[] fluidInputs = {
                    FL.Lubricant.make(1000),
                    molten(MT.Trinium.mLiquid, 432)};
            addRecipe(new MachineRecipe(
                    itemInputs,
                    itemOutputs,
                    fluidInputs,
                    null,
                    1200,
                    8192,
                    5));
        }

        {
            ItemStack[] itemInputs = {
                    IL.Electric_Motor_LuV.get(1), gt(OP.plate, MT.Iritanium, 8), gt(OP.rotor, MT.Iritanium, 4), mte(26662, 8),
                    gt(OP.ring, MT.Rubber, 32), gt(OP.stickLong, MT.Iritanium, 8), mte(29617, 8),
            };
            FluidStack[] fluidInputs = {FL.Lubricant.make(1000), FL.polyvinylchloride.make(1296), molten(MT.Trinium.mLiquid, 720)};
            ItemStack[] itemOutputs = {IL.Electric_Pump_LuV.get(1)};
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, fluidInputs, null, 1800, 8192, 5));

        }


        {
            ItemStack[] itemInputs ={ IL.Electric_Motor_LuV.get(2), gt(OP.casingSmall, MT.Iritanium, 16), gt(OP.plate, MT.Rubber, 16), gt(OP.gearGtSmall, MT.Iritanium, 8),  mte(29617, 8)};
            FluidStack[] fluidInputs = {FL.Lubricant.make(1000), FL.polyvinylchloride.make(576), molten(MT.Trinium.mLiquid, 576)};
            ItemStack[] itemOutputs = {IL.Conveyor_Module_LuV.get(1)};
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, fluidInputs, null, 1200, 8192, 5));
        }
        {
            ItemStack[] itemInputs ={IL.Electric_Motor_LuV.get(1), gt(OP.casingSmall, MT.Iritanium, 10), gt(OP.gearGtSmall, MT.Iritanium, 16), gt(OP.gearGt, MT.Iritanium, 8), gt(OP.stickLong, MT.Iritanium, 8), gt(OP.screw, MT.Iritanium, 32), mte(29617, 8)};
            FluidStack[] fluidInputs = {FL.Lubricant.make(1000), FL.polyvinylchloride.make(720), molten(MT.Trinium.mLiquid, 720)};
            ItemStack[] itemOutputs = {IL.Electric_Piston_LuV.get(1)};
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, fluidInputs, null, 1750, 8192, 5));
        }
        {
            ItemStack[] itemInputs ={IL.Electric_Motor_LuV.get(2), IL.Electric_Piston_LuV.get(1), IL.Circuit_Ultimate.get(4), gt(OP.stickLong, MT.Iritanium, 8), gt(OP.casingSmall, MT.Iritanium, 16), gt(OP.gearGtSmall, MT.Iritanium, 8), gt(OP.screw, MT.Iritanium, 32),mte(29617, 8) };
            FluidStack[] fluidInputs = {FL.Lubricant.make(3000), FL.polyvinylchloride.make(1296), molten(MT.Trinium.mLiquid, 720)};
            ItemStack[] itemOutputs = {IL.Robot_Arm_LuV.get(1)};
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, fluidInputs, null, 1200, 8192, 5));
        }

        {
            ItemStack[] itemInputs ={new ItemStack(Items.nether_star, 1), IL.Circuit_Ultimate.get(4), gt(OP.casingSmall, MT.Iritanium, 32), gt(OP.plateCurved, MT.Iritanium, 16), gt(OP.screw, MT.Iritanium, 64), gt(OP.wireFine, MT.Ir, 64), gt(OP.wireFine, MT.Ir, 64),mte(29617, 12) };
            FluidStack[] fluidInputs = {FL.Lubricant.make(8000), FL.polyvinylchloride.make(4128), molten(MT.Trinium.mLiquid, 1750)};
            ItemStack[] itemOutputs = {IL.Emitter_LuV.get(1)};
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, fluidInputs, null, 1200, 8192, 5));
        }
        {
            ItemStack[] itemInputs ={new ItemStack(Items.nether_star, 1),IL.Circuit_Ultimate.get(2), gt(OP.casingSmall, MT.Iritanium, 32), gt(OP.plateCurved, MT.Iritanium, 16), gt(OP.screw, MT.Iritanium, 64), gt(OP.wireFine, MT.Ir, 64), mte(29617, 12)};
            FluidStack[] fluidInputs = {FL.Lubricant.make(8000), FL.polyvinylchloride.make(3620), molten(MT.Trinium.mLiquid, 1412)};
            ItemStack[] itemOutputs = {IL.Sensor_LuV.get(1)};
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, fluidInputs, null, 1200, 8192, 5));
        }
        {
            ItemStack[] itemInputs ={IL.Emitter_LuV.get(1), new ItemStack(Items.nether_star, 1), IL.Circuit_Ultimate.get(8), gt(OP.plateDense, MT.NeodymiumMagnetic, 12), mte(29207, 8), gt(OP.wireFine, MT.Ir, 64), gt(OP.wireFine, MT.Ir, 64), gt(OP.wireFine, MT.Ir, 64), gt(OP.wireFine, MT.Ir, 64), gt(OP.wireFine, MT.Ir, 64), gt(OP.wireFine, MT.Ir, 64), gt(OP.casingSmall, MT.Iritanium, 64), mte(29617, 12) };
            FluidStack[] fluidInputs = {FL.Lubricant.make(16000), FL.polyvinylchloride.make(12000), molten(MT.Osmium.mLiquid, 2500)};
            ItemStack[] itemOutputs = {IL.Field_Generator_LuV.get(1)};
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, fluidInputs, null, 4120, 8192, 5));
        }








    }
}


     /* EXAMPLE

     {
            ItemStack[] itemInputs ={};
            FluidStack[] fluidInputs = {};
            ItemStack[] itemOutputs = {};
            addRecipe(new MachineRecipe(itemInputs, itemOutputs, fluidInputs, null, 1200, 8192, 5));
       }

   */
