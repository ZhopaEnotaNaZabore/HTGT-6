import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.handler.FluidDatabase;
import com.mod.htgt6.common.handler.recipes.DeepOreProcessingRecipes;
import com.mod.htgt6.common.handler.recipe.RM.*;
import com.mod.htgt6.common.TE.TEElectromagneticProcessor;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import gregapi.data.*;
import gregapi.oredict.OreDictManager;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.*;
import net.minecraftforge.common.util.ForgeDirection;
import java.lang.reflect.*;

public final class DeepOreProcessingFixture {
    static int checks;
    static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    static Fluid fluid(String name){FluidRegistry.registerFluid(new Fluid(name));return FluidRegistry.getFluid(name);}
    static void item(gregapi.oredict.OreDictPrefix prefix,gregapi.oredict.OreDictMaterial material,String name) {
        Item item=new Item();cpw.mods.fml.common.registry.GameRegistry.registerItem(item,name);
        OreDictManager.INSTANCE.setTarget_(prefix,material,new ItemStack(item));
    }
    public static void main(String[] args)throws Exception {
        CrusherOreImportFixture.initializeGT6();
        item(OP.dust,MT.Fe,"iron");item(OP.dustTiny,MT.PlatinumGroupSludge,"pgm");
        FluidDatabase.CHALCOPYRITE_SOLUTION=fluid("htgt6.chalcopyrite_solution");
        FluidDatabase.registerDeepOreFluids();Fluid enriched=FluidDatabase.ENRICHED_COPPER_SOLUTION;
        FluidDatabase.registerDeepOreFluids();check(enriched==FluidDatabase.ENRICHED_COPPER_SOLUTION,"Duplicate fluid");
        MT.BlueVitriol.liquid(new FluidStack(fluid("bluevitriol"),1000));
        DeepOreProcessingRecipes.register();
        MachineRecipe em=ElectromagneticProcessorRecipeMap.INSTANCE.getRecipeList().get(0);
        MachineRecipe centrifuge=IndustrialCentrifugeRecipeHandler.INSTANCE.getRecipeList().get(0);
        check(em.fluidInputs[0].amount==10000&&em.outputs[0].stackSize==8,"Deferritization batch");
        check(em.fluidOutputs[0].isFluidEqual(centrifuge.fluidInputs[0])&&em.fluidOutputs[0].amount==centrifuge.fluidInputs[0].amount,"Broken chain");
        check(centrifuge.fluidOutputs[0].getFluid()==MT.BlueVitriol.mLiquid.getFluid()&&centrifuge.fluidOutputs[0].amount==8000,"Not GT6 vitriol");
        check(centrifuge.outputs[0].isItemEqual(OP.dustTiny.mat(MT.PlatinumGroupSludge,1))&&centrifuge.outputs[0].stackSize==1,"PGM trace size");
        DeepOreProcessingRecipes.register();check(ElectromagneticProcessorRecipeMap.INSTANCE.getRecipeList().size()==1&&IndustrialCentrifugeRecipeHandler.INSTANCE.getRecipeList().size()==1,"Duplicate recipes");
        Method tick=TEElectromagneticProcessor.class.getDeclaredMethod("processMachineLogic");tick.setAccessible(true);
        Method mapping=net.minecraft.tileentity.TileEntity.class.getDeclaredMethod("addMapping",Class.class,String.class);mapping.setAccessible(true);mapping.invoke(null,TEElectromagneticProcessor.class,"fixture_em");
        TEElectromagneticProcessor machine=new TEElectromagneticProcessor();machine.machineTier=2;machine.energy=1000000;
        machine.fill(ForgeDirection.UP,new FluidStack(FluidDatabase.CHALCOPYRITE_SOLUTION,16000),true);
        machine.tanks[1].fill(new FluidStack(enriched,7000),true);
        tick.invoke(machine);check(machine.tanks[0].getFluidAmount()==16000,"Consumed inputs with blocked output");
        machine.tanks[1].drain(16000,true);
        check(machine.drain(ForgeDirection.DOWN,1000,true)==null,"Automation drained feedstock");
        tick.invoke(machine);check(machine.tanks[0].getFluidAmount()==6000,"Input batch consumption");
        NBTTagCompound saved=new NBTTagCompound();machine.writeToNBT(saved);
        TEElectromagneticProcessor restored=new TEElectromagneticProcessor();restored.readFromNBT(saved);restored.machineTier=2;restored.energy=1000000;
        for(int i=0;i<400;i++)tick.invoke(restored);
        check(restored.tanks[0].getFluidAmount()==6000,"Lost remaining feedstock after reload");
        check(restored.tanks[1].getFluidAmount()==10000&&restored.tanks[1].getFluid().getFluid()==enriched,"Fluid output lost/mixed");
        check(restored.getStackInSlot(1)!=null&&restored.getStackInSlot(1).stackSize==8,"Iron lost/duplicated after reload");
        check(restored.drain(ForgeDirection.DOWN,10000,true).amount==10000,"Output not extractable");
        check(restored.fill(ForgeDirection.UP,new FluidStack(enriched,1000),true)==0,"Input bypassed into output tank");
        System.out.println("Deep ore processing PASS: "+checks+" checks; fluids, batches, GT6 vitriol, trace PGM, blocked outputs, remainder, NBT resume and extraction");
    }
}
