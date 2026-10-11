import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.GTHooks.TurbineRotorHooks.*;
import com.mod.htgt6.common.handler.recipe.RM.*;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import gregapi.data.*;
import gregapi.oredict.*;
import net.minecraft.item.*;
import cpw.mods.fml.common.registry.GameRegistry;

public final class TurbineRotorRecipesFixture {
    static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    static ItemStack part(OreDictPrefix prefix,OreDictMaterial material,String name) {
        Item item=new Item();GameRegistry.registerItem(item,name);
        ItemStack stack=new ItemStack(item);
        OreDictManager.INSTANCE.setTarget_(prefix,material,stack);
        return stack;
    }
    public static void main(String[] args)throws Exception {
        CrusherOreImportFixture.initializeGT6();
        GT6TurbineRotorHooks.registerItem();
        part(OP.rotor,MT.Steel,"steel_rotor");part(OP.rotor,MT.Titanium,"titanium_rotor");
        OreDictMaterial[] shafts={MT.Magnalium,MT.Titanium,MT.TungstenSteel,MT.Americium};
        for(int i=0;i<shafts.length;i++)part(OP.stickLong,shafts[i],"shaft_"+i);
        int gtCount=RM.Assembler.mRecipeList.size();
        GT6TurbineRotorHooks.registerRecipes();
        int count=0;
        for(MachineRecipe recipe:AssemblerRecipeHandler.INSTANCE.getRecipeList()) {
            if(recipe.outputs[0].getItem()!=GT6TurbineRotorHooks.ITEM)continue;
            count++;
            ItemStack output=recipe.outputs[0];
            TurbineRotorSize size=GT6TurbineRotorHooks.ITEM.getSize(output);
            OreDictMaterial material=GT6TurbineRotorHooks.ITEM.getMaterial(output);
            check(material!=null&&output.hasTagCompound(),"Material NBT missing");
            check(recipe.duration==size.assemblyDuration&&recipe.euPerTick==size.assemblyEUt,"Processing parameters changed");
            check(recipe.minTier==size.ordinal()+1,"Wrong machine tier");
            check(recipe.inputs.length==2&&recipe.inputs[0].stackSize==size.componentCount&&recipe.inputs[1].stackSize==1,"Ingredients changed");
            check(recipe.inputs[0].isItemEqual(OP.rotor.mat(material,1)),"Rotor material changed");
            check(recipe.inputs[1].isItemEqual(OP.stickLong.mat(shafts[size.ordinal()],1)),"Shaft material changed");
            ItemStack[] inventory=new ItemStack[10];inventory[0]=recipe.inputs[0].copy();inventory[1]=recipe.inputs[1].copy();
            check(AssemblerRecipeHandler.INSTANCE.findRecipe(inventory,new net.minecraftforge.fluids.FluidTank[0])==recipe,"Native assembler cannot find recipe");
        }
        check(count>=8,"Both materials and all four sizes must be covered");
        check(RM.Assembler.mRecipeList.size()==gtCount,"Rotor recipes leaked into GT6 import source");
        ElectronicFactoryRecipeHandler.INSTANCE.importGT6CircuitAssemblerRecipes();
        for(MachineRecipe recipe:ElectronicFactoryRecipeHandler.INSTANCE.getRecipeList())
            for(ItemStack output:recipe.outputs)check(output==null||output.getItem()!=GT6TurbineRotorHooks.ITEM,"Rotor remains in electronics factory");
        int total=AssemblerRecipeHandler.INSTANCE.getRecipeList().size();
        GT6TurbineRotorHooks.registerRecipes();
        check(AssemblerRecipeHandler.INSTANCE.getRecipeList().size()==total,"Duplicate registration");
        System.out.println("Turbine rotor recipes PASS: "+count+" native recipes, materials/NBT, four tiers, costs, lookup, import exclusion and idempotence");
    }
}
