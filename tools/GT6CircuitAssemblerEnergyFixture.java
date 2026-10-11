package com.mod.htgt6.common.electronics;

import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import gregapi.data.CS;
import gregapi.data.RM;
import gregapi.data.TD;
import gregapi.fluid.FluidTankGT;
import gregapi.recipes.Recipe;
import gregapi.tileentity.base.TileEntityBase05Inventories;
import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import java.lang.reflect.Field;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Runs bundled GT6 checkRecipe/doWork, including its real low-power overclock. */
public final class GT6CircuitAssemblerEnergyFixture {
    static final class Machine extends MultiTileEntityBasicMachine {
        @Override public void updateInventory(){mInventoryChanged=true;}
        @Override public boolean doSoundInterrupt(){return true;}
    }
    public static void main(String[] args)throws Exception{
        net.minecraft.world.World world=CrusherOreImportFixture.initializeGT6();
        CS.GAPI.mFinishedServerStarted=1;
        CS.GAPI_POST=CS.GAPI;
        String source=new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(
                "src/main/java/com/mod/htgt6/common/handler/recipes/modpackgenerated/integratedgt/IntegratedGTFromGregtechtierlv.java")),java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher cost=java.util.regex.Pattern.compile("GregTechTierLV\\.zs:254\", \"gt\\.recipe\\.assembler\", false, (\\d+)L, (\\d+)L").matcher(source);
        require(cost.find(),"locate actual HiTech6 T1 recipe");
        long power=Long.parseLong(cost.group(1)),duration=Long.parseLong(cost.group(2));
        Machine old=run(world,12,duration);
        require(old.mMinEnergy==48&&old.mMaxProgress>0&&old.slot(0)==null,"old recipe consumes input then requires 48 LU/t");
        require(old.slot(RM.Assembler.mInputItemsCount)==null,"old recipe stalls at nominal 32 LU/t");
        Machine fixed=run(world,power,duration);
        ItemStack output=fixed.slot(RM.Assembler.mInputItemsCount);
        require(output!=null&&output.getItem()==Items.diamond&&output.stackSize==4,"fixed recipe completes at nominal 32 LU/t");
        require(fixed.slot(0)==null,"ingredients consumed once");
        System.out.println("GT6 circuit assembler PASS: old 12 -> 48 LU/t stall; actual recipe "+power+" LU/t completes at 32 LU/t");
    }
    private static Machine run(net.minecraft.world.World world,long power,long duration)throws Exception{
        return run(world,power,duration,TD.Energy.LU,32,16,64,false,2000);
    }
    static Machine run(net.minecraft.world.World world,long power,long duration,
            gregapi.code.TagData energy,long nominal,long minimum,long maximum,boolean cheap,int ticks)throws Exception{
        Recipe recipe=new Recipe(false,false,true,new ItemStack[]{new ItemStack(Items.iron_ingot,4)},
                new ItemStack[]{new ItemStack(Items.diamond,4)},null,new long[]{10000},null,null,duration,power,0);
        Machine machine=new Machine();machine.setWorldObj(world);machine.mRecipes=RM.Assembler;
        RM.Assembler.mRecipeList.clear();RM.Assembler.mRecipeList.add(recipe);
        machine.mLastRecipe=recipe;
        machine.mEnergyTypeAccepted=energy;
        machine.mInput=nominal;machine.mInputMin=minimum;machine.mInputMax=maximum;
        machine.mCheapOverclocking=cheap;
        machine.mDisabledItemInput=machine.mDisabledItemOutput=machine.mDisabledFluidInput=machine.mDisabledFluidOutput=true;
        machine.mItemInputs=0;machine.mTanksInput=new FluidTankGT[0];machine.mTanksOutput=new FluidTankGT[0];
        Field inventory=TileEntityBase05Inventories.class.getDeclaredField("mInventory");inventory.setAccessible(true);
        inventory.set(machine,new ItemStack[RM.Assembler.mInputItemsCount+RM.Assembler.mOutputItemsCount+1]);
        machine.slot(0,new ItemStack(Items.iron_ingot,4));
        for(int tick=0;tick<ticks;tick++){
            // Pistons and other alternating sources must reverse stroke to finish a cycle.
            long packet=TD.Energy.ALL_ALTERNATING.contains(energy)&&(tick&1)==1?-nominal:nominal;
            machine.doInject(energy,(byte)6,packet,1,true);
            machine.doWork(100+tick);
        }
        return machine;
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
