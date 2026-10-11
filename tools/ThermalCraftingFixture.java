package com.mod.htgt6.common.handler.recipes;
import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.MT;
import gregapi.oredict.OreDictMaterial;
import java.util.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.oredict.*;

public class ThermalCraftingFixture {
    static Item machine, parts;
    static ItemStack m(int id) { return new ItemStack(machine,1,id); }
    static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        World world=CrusherOreImportFixture.initializeGT6();
        Loader.instance().activeModContainer().getMetadata().modId="gregtech";
        machine=new Item().setHasSubtypes(true); GameRegistry.registerItem(machine,"gt.multitileentity");
        Loader.instance().activeModContainer().getMetadata().modId="HTGT6";
        parts=new Item().setHasSubtypes(true); GameRegistry.registerItem(parts,"thermal_fixture_parts");
        GameRegistry.registerItem(new Item(),"FireclayBrick");
        OreDictMaterial[] materials={MT.Pb,MT.Bi,MT.Bronze,MT.Invar,MT.Steel,MT.Cr,MT.Ti,MT.W,MT.TungstenSteel,MT.Ta4HfC5,MT.GT6U.Nb2Ti3C4,MT.Netherite,MT.Cu};
        int meta=0;
        for(OreDictMaterial mat:materials) for(String prefix:new String[]{"plate","plateDouble","plateDense","plateQuintuple","plateCurved","rotor","pipeSmall","pipeLarge"})
            OreDictionary.registerOre(prefix+mat.mNameInternal,new ItemStack(parts,1,meta++));
        Map<Integer,Integer> cores=new LinkedHashMap<Integer,Integer>();
        for(int i=1;i<12;i++) cores.put(1100+i,i==11?1105:1099+i);
        for(int i=0;i<12;i++) {
            cores.put(1150+i,1100+i);
            cores.put(9000+i,1100+i); cores.put(9050+i,9000+i);
            if(i>=2) { cores.put(1400+i,1100+i); cores.put(1450+i,1400+i); cores.put(1600+i,1100+i); cores.put(1650+i,1600+i); }
        }
        for(int i=0;i<10;i++) cores.put(1250+i,1200+i);
        List<IRecipe> recipes=CraftingManager.getInstance().getRecipeList(); recipes.clear();
        for(int id:cores.keySet()) {
            recipes.add(new ShapedOreRecipe(m(id),"P",'P',parts));
            recipes.add(new ShapedOreRecipe(m(id),"PP",'P',parts));
        }
        IRecipe lead=new ShapedOreRecipe(m(1100),"PPP",'P',parts); recipes.add(lead);
        BurningBoxRecipes.register();
        check(recipes.size()==98 && recipes.contains(lead),"97 replacements and preserved lead");
        for(IRecipe recipe:recipes) {
            int id=recipe.getRecipeOutput().getItemDamage(); if(id==1100) continue;
            Object[] input=((ShapedOreRecipe)recipe).getInput();
            check(input[4] instanceof ItemStack && ((ItemStack)input[4]).isItemEqual(m(cores.get(id))),"wrong core "+id);
            InventoryCrafting grid=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
            for(int i=0;i<input.length;i++) {
                Object part=input[i];
                if(part instanceof List) part=((List)part).get(0);
                if(part instanceof ItemStack) grid.setInventorySlotContents(i,((ItemStack)part).copy());
            }
            ItemStack result=CraftingManager.getInstance().findMatchingRecipe(grid,world);
            check(result!=null && result.isItemEqual(m(id)),"collision or missing output "+id);
            grid.setInventorySlotContents(4,m(29999));
            check(!recipe.matches(grid,world),"wrong core accepted "+id);
        }
        ThermalMachineCraftingRecipes.register();
        check(recipes.size()==98,"repeat registration duplicates");
        IRecipe previous=null;
        for(IRecipe recipe:recipes) if(recipe.getRecipeOutput().isItemEqual(m(1252))) previous=recipe;
        com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3.GeneratedCrafting3x3.replaceTokens("missing thermal plate", "<gregtech:gt.multitileentity:1252>", new String[][] {{"<ore:thermalFixtureMissingPlate>"}}, com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings.DEFAULT, org.apache.logging.log4j.LogManager.getLogger("ThermalFixture"));
        check(recipes.contains(previous),"missing ingredient removed existing boiler");
        // Exercise the actual migrated furnace registration, not a duplicate test grid.
        Loader.instance().activeModContainer().getMetadata().modId="etfuturum";
        Item oldFurnace=new Item(); GameRegistry.registerItem(oldFurnace,"blast_furnace");
        Loader.instance().activeModContainer().getMetadata().modId="Minecraft_1.20.2";
        Item furnace=new Item(); GameRegistry.registerItem(furnace,"village_pillage_blast_furnace");
        OreDictionary.registerOre("plateTinAlloy",new ItemStack(parts,1,900));
        OreDictionary.registerOre("craftingToolWrench",new ItemStack(parts,1,901));
        recipes.clear();
        recipes.add(new ShapedOreRecipe(new ItemStack(oldFurnace),"P",'P',parts));
        recipes.add(new ShapedOreRecipe(new ItemStack(furnace),"PP",'P',parts));
        com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings settings=com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings.DEFAULT;
        org.apache.logging.log4j.Logger log=org.apache.logging.log4j.LogManager.getLogger("FurnaceFixture");
        com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.RemovedFromGamestart.register(settings,log);
        com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3.CraftingFromGamestart.register(settings,log);
        int count=0;
        for(IRecipe recipe:recipes) {
            ItemStack out=recipe.getRecipeOutput(); if(out==null) continue;
            check(out.getItem()!=oldFurnace,"Et Futurum furnace still craftable");
            if(out.getItem()==furnace) {count++; check(recipe.getRecipeSize()==9,"backport cheap recipe remained");}
        }
        check(count==1,"exactly one backport furnace recipe");
        checkComponents();
        checkExtruderAndFluids();
        System.out.println("Backport furnace PASS: migrated recipe replaces cheap crafting and disables Et Futurum output");
        System.out.println("Thermal crafting PASS: 97 unique grids, correct cores, wrong-core rejection, preserved lead, idempotence and missing-ingredient fallback");
    }
    @SuppressWarnings("unchecked")
    static void checkComponents() {
        Loader.instance().activeModContainer().getMetadata().modId="gregtech";
        Item components=new Item().setHasSubtypes(true); GameRegistry.registerItem(components,"gt.multiitem.technological");
        Loader.instance().activeModContainer().getMetadata().modId="gregapi";
        Item selector=new Item().setHasSubtypes(true); GameRegistry.registerItem(selector,"gt.integrated_circuit");
        ItemStack tool=new ItemStack(parts,1,999);
        OreDictionary.registerOre("craftingToolScrewdriver",tool);
        List<IRecipe> crafting=CraftingManager.getInstance().getRecipeList(); crafting.clear();
        com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler map=com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler.INSTANCE;
        map.getRecipeList().clear();
        int index=1000;
        for(int base:new int[]{12000,12020,12040,12060,12080,12100,12120,12140}) for(int tier=0;tier<4;tier++) {
            crafting.add(new ShapedOreRecipe(new ItemStack(components,1,base+tier),"PPP","STS","PPP",
                'P',new ItemStack(parts,1,index++),'S',new ItemStack(parts,1,index++),'T',"craftingToolScrewdriver"));
        }
        com.mod.htgt6.common.handler.recipe.RM.ComponentAssemblerRecipes.register();
        check(crafting.size()==32 && map.getRecipeList().size()==32,"additive component import");
        com.mod.htgt6.common.handler.recipe.RM.ComponentAssemblerRecipes.register();
        check(map.getRecipeList().size()==32,"component import idempotence");
        for(com.mod.htgt6.api.hitech6.RM.MachineRecipe recipe:map.getRecipeList()) {
            check(recipe.duration==20 && recipe.euPerTick==8 && recipe.minTier==1,"component parameters");
            check(recipe.inputs[0].stackSize==6 && recipe.inputs[1].stackSize==2,"material quantities");
            check(recipe.inputs[2]==null && recipe.inputs[8].getItemDamage()==23 && recipe.inputs[8].stackSize==0,"tool omitted and selector required");
            ItemStack[] inventory=new ItemStack[10];
            inventory[0]=recipe.inputs[0].copy(); inventory[1]=recipe.inputs[1].copy();
            inventory[8]=new ItemStack(selector,1,23);
            check(map.findRecipe(inventory,new net.minecraftforge.fluids.FluidTank[0])==recipe,"component match");
            inventory[8].setItemDamage(2);
            check(map.findRecipe(inventory,new net.minecraftforge.fluids.FluidTank[0])==null,"wrong selector accepted");
            inventory[8]=null;
            check(map.findRecipe(inventory,new net.minecraftforge.fluids.FluidTank[0])==null,"missing selector accepted");
            inventory[8]=new ItemStack(selector,1,23);
            map.consumeInputs(recipe,inventory);
            check(inventory[0]==null && inventory[1]==null && inventory[8].stackSize==1,"selector consumed or materials not consumed");
        }
        for(int tier=4;tier<=5;tier++) {
            ItemStack[] inputs=new ItemStack[9]; inputs[0]=new ItemStack(parts,6,tier);
            com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler.addRecipe(inputs,new ItemStack(components,1,12000+tier),20,512,tier);
            com.mod.htgt6.api.hitech6.RM.MachineRecipe recipe=map.getRecipeList().get(map.getRecipeList().size()-1);
            check(recipe.duration==20 && recipe.euPerTick==512 && recipe.minTier==tier && recipe.inputs[0].stackSize==6,"EV-IV existing parameters changed");
            check(recipe.inputs[8].getItemDamage()==23 && recipe.inputs[8].stackSize==0,"EV-IV selector missing");
            check(inputs[8]==null,"caller inputs mutated");
        }
        System.out.println("Component assembler PASS: 32 ULV-HV recipes, tool removal, quantities, selector matching/consumption, EV-IV selector-only changes");
    }

    @SuppressWarnings("unchecked")
    static void checkExtruderAndFluids() throws Exception {
        com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.TRANSCENDENTIC_ALLOY=testMaterial(32001,"TranscendenticAlloy");
        com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.STABLE_TIME_SPACE=testMaterial(32002,"StableTimeSpace");
        com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.QUANTUM_ALLOY=testMaterial(32003,"QuantumAlloy");
        com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.QUANTINUM=testMaterial(32004,"Quantinum");
        com.mod.htgt6.common.handler.FluidDatabase.registerEndgameMoltenFluids();
        OreDictMaterial[] melts={com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.TRANSCENDENTIC_ALLOY,
            com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.STABLE_TIME_SPACE,
            com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.QUANTUM_ALLOY,
            com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.QUANTINUM};
        for(OreDictMaterial mat:melts) check(mat.mLiquid!=null && mat.mLiquid.getFluid()!=null,"missing DirtChest melt "+mat.mNameInternal);
        int fluids=net.minecraftforge.fluids.FluidRegistry.getRegisteredFluids().size();
        com.mod.htgt6.common.handler.FluidDatabase.registerEndgameMoltenFluids();
        check(fluids==net.minecraftforge.fluids.FluidRegistry.getRegisteredFluids().size(),"duplicate molten registration");
        Loader.instance().activeModContainer().getMetadata().modId="HTGT6";
        com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks.HighTierComponents.register();
        GameRegistry.registerItem(new Item(),"UXcoil");
        int metadata=2000;
        for(String prefix:new String[]{"plateDense","gearGt","screw"})
            OreDictionary.registerOre(prefix+MT.Bedrock_HSLA_Alloy.mNameInternal,new ItemStack(parts,1,metadata++));
        List<IRecipe> recipes=CraftingManager.getInstance().getRecipeList();recipes.clear();
        for(int id:new int[]{20201,20202,20203,20204,17129}) recipes.add(new ShapedOreRecipe(m(id),"P",'P',parts));
        com.mod.htgt6.common.handler.recipe.RM.LineAssemblerRecipeHandler map=com.mod.htgt6.common.handler.recipe.RM.LineAssemblerRecipeHandler.INSTANCE;
        map.getRecipeList().clear();
        com.mod.htgt6.common.handler.recipes.ExtruderProgressionRecipes.register(
            com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings.DEFAULT,org.apache.logging.log4j.LogManager.getLogger("ExtruderFixture"));
        check(recipes.size()==1 && recipes.get(0).getRecipeOutput().getItemDamage()==20201,"extruder crafting progression");
        check(map.getRecipeList().size()==1,"UX extruder recipe missing");
        com.mod.htgt6.api.hitech6.RM.MachineRecipe recipe=map.getRecipeList().get(0);
        check(recipe.minTier==10 && recipe.euPerTick==8388608 && recipe.outputs[0].getItemDamage()==20204,"UX extruder parameters");
        ItemStack[] inventory=new ItemStack[24];
        for(int i=0;i<recipe.inputs.length;i++) inventory[i]=recipe.inputs[i].copy();
        check(map.findRecipe(inventory,0,16,16)==recipe,"UX recipe matches LineAssembler slots");
        System.out.println("Extruder/DirtChest PASS: only basic extruder crafting remains, UX final recipe matches, four endgame melts registered idempotently");
    }

    static OreDictMaterial testMaterial(int id,String name) {
        OreDictMaterial mat=OreDictMaterial.createMaterial(id,name,name).setTextures(gregapi.render.TextureSet.SET_SHINY).heat(6000,16000);
        return mat;
    }

}
