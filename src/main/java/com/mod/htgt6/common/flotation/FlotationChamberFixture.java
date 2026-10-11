package com.mod.htgt6.common.flotation;

import com.mod.htgt6.common.handler.FluidDatabase;
import com.mod.htgt6.common.handler.recipe.RM.FlotationChamberRecipeHandler;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/** Headless contract test; synthetic ore/acid isolate the new recipe and NBT paths. */
public final class FlotationChamberFixture {
    public static void main(String[] args) throws Exception {
        Grid world = BountifulMechanicsFixture120.initialize();
        java.lang.reflect.Field mods = cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");
        mods.setAccessible(true);
        mods.set(cpw.mods.fml.common.Loader.instance(), new java.util.HashMap<String, cpw.mods.fml.common.ModContainer>());
        Fluid acid = new Fluid("flotation_fixture_acid"); FluidRegistry.registerFluid(acid);
        Fluid solution = new Fluid("htgt6.chalcopyrite_solution"); FluidRegistry.registerFluid(solution);
        FluidDatabase.CHALCOPYRITE_SOLUTION = FluidRegistry.getFluid(solution.getName());
        // GT6's material table requires full mod startup; test the map contract in isolation.
        FlotationChamberRecipeHandler.addRecipe(new ItemStack(Items.iron_ingot, 32),
                new FluidStack(acid, 10000), new FluidStack(solution, 10000), 400, 32, 1);
        ChemicalPlantRecipe recipe = FlotationChamberRecipeHandler.INSTANCE.getRecipeList().get(0);
        require(recipe.inputs[0].stackSize == 32 && recipe.fluidInputs[0].amount == 10000
                && recipe.fluidOutputs[0].amount == 10000 && recipe.outputs.length == 0, "32 ore / 10000 mB");
        require(recipe.fluidInputs[0].getFluid() == acid, "reagent identity");
        FlotationChamberContent.register();
        GameRegistry.registerBlock(FlotationChamberContent.glass,"flotation_fixture_glass");
        GameRegistry.registerBlock(com.mod.htgt6.common.mining.MiningUnitContent.port,"flotation_fixture_port");
        verifyStructure(world);
        MultiTileFlotationChamber server=new MultiTileFlotationChamber(); server.setWorldObj(world);
        NBTTagCompound saved=new NBTTagCompound();
        saved.setBoolean("StructureFormed",true); saved.setBoolean("ChemicalProcessing",true);
        saved.setInteger("ChemicalRecipeIndex",0); saved.setInteger("Progress",100); saved.setInteger("MaxProgress",400);
        saved.setBoolean("FlotationCharge",true);
        NBTTagCompound product=new NBTTagCompound(); recipe.fluidOutputs[0].writeToNBT(product);
        NBTTagList pending=new NBTTagList(); pending.appendTag(product); saved.setTag("ChemicalPendingFluids",pending);
        server.readFromNBT(saved);
        NBTTagCompound roundtrip=new NBTTagCompound(); server.writeToNBT(roundtrip);
        require(server.progress==100 && roundtrip.getTagList("ChemicalPendingFluids",10).tagCount()==1, "saved prepared batch");
        require(FluidStack.loadFluidStackFromNBT(roundtrip.getTagList("ChemicalPendingFluids",10)
                .getCompoundTagAt(0)).amount==10000, "saved solution amount");
        java.lang.reflect.Field remote=World.class.getDeclaredField("isRemote"); remote.setAccessible(true); remote.setBoolean(world,true);
        MultiTileFlotationChamber client=new MultiTileFlotationChamber(); client.setWorldObj(world);
        roundtrip.setInteger("ChemicalRecipeIndex",Integer.MAX_VALUE);
        roundtrip.setBoolean("FlotationRunning",true); roundtrip.setString("Diagnostic","unrelated translated text");
        client.readFromNBT(roundtrip);
        require(client.isFlotationRunning() && client.hasFlotationCharge(), "render independent of client recipe index/diagnostic");
        roundtrip.setBoolean("FlotationRunning",false); client.readFromNBT(roundtrip);
        require(!client.isFlotationRunning(), "paused render");
        System.out.println("Flotation contracts PASS: recipe, geometry, saved batch, independent client activity");
    }
    private static void verifyStructure(Grid world) throws Exception {
        com.mod.htgt6.client.nei.structure.FlotationChamberNEIStructure plan =
                new com.mod.htgt6.client.nei.structure.FlotationChamberNEIStructure();
        require(plan.getWidth()==13 && plan.getDepth()==11 && plan.getHeight()==7,"reference footprint");
        require(plan.getControllerX()==10 && plan.getControllerY()==1 && plan.getControllerZ()==0,"front-right controller");
        int glass=0,ribs=0,motors=0,controllers=0;
        com.google.gson.JsonArray exported=new com.google.gson.JsonArray();
        for(int x=0;x<13;x++)for(int y=0;y<7;y++)for(int z=0;z<11;z++) {
            int element=MultiTileFlotationChamber.getElementAt(x,y,z);
            com.mod.htgt6.client.nei.structure.HTGT6MultiblockStructure.StructureBlock block=plan.getBlock(x,y,z);
            require((element==0)==(block==null),"NEI air/structure agreement");
            if(element==1)controllers++;
            if(element==4)glass++;
            if(element==5)ribs++;
            if(y==6&&element!=0){require(element==3&&z==5&&(x==3||x==6||x==9),"only three upper drive housings");motors++;}
            if(block!=null){com.google.gson.JsonArray row=new com.google.gson.JsonArray();row.add(new com.google.gson.JsonPrimitive(x));row.add(new com.google.gson.JsonPrimitive(y));row.add(new com.google.gson.JsonPrimitive(z));row.add(new com.google.gson.JsonPrimitive(element));exported.add(row);}
        }
        require(controllers==1&&glass==24&&ribs==57&&motors==3,"reference windows, ribs, bridges and drives");
        for(int x:new int[]{3,6,9})for(int z=3;z<8;z++) {
            require(MultiTileFlotationChamber.getElementAt(x,3,z)==0,"open vat volume");
            require(MultiTileFlotationChamber.getElementAt(x,5,z)==5,"structural motor bridge");
            require(MultiTileFlotationChamber.getElementAt(x+1,5,z)==0,"no solid roof");
        }
        for(int facing=2;facing<=5;facing++) {
            MultiTileFlotationChamber machine=new MultiTileFlotationChamber();machine.setWorldObj(world);
            machine.xCoord=100*facing;machine.yCoord=60;machine.zCoord=0;world.loaded=true;
            world.setBlock(machine.xCoord,60,0,FlotationChamberContent.controller,facing,2);
            for(int x=0;x<13;x++)for(int y=0;y<7;y++)for(int z=0;z<11;z++) {
                com.mod.htgt6.client.nei.structure.HTGT6MultiblockStructure.StructureBlock entry=plan.getBlock(x,y,z);
                int[] at=machine.localToWorld(x-plan.getControllerX(),y-plan.getControllerY(),z);
                net.minecraft.block.Block block=entry==null?net.minecraft.init.Blocks.air:entry.block;
                int metadata=entry==null?0:entry.block==FlotationChamberContent.controller?facing:entry.metadata;
                world.setBlock(at[0],at[1],at[2],block,metadata,2);
                if(block==com.mod.htgt6.common.mining.MiningUnitContent.port){
                    net.minecraft.tileentity.TileEntity gateway=new com.mod.htgt6.common.multiblock.gateway.TileEntityMultiblockGateway(metadata);
                    gateway.setWorldObj(world);gateway.xCoord=at[0];gateway.yCoord=at[1];gateway.zCoord=at[2];
                    world.tiles.put(at[0]+","+at[1]+","+at[2],gateway);
                }
            }
            com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition definition=machine.createStructureDefinition();
            require(definition.validate(machine).isFormed(),"NEI build validates in rotation "+facing);
            for(int[] hole:new int[][]{{3,2,2},{3,5,4},{3,6,5}}){
                int[] at=machine.localToWorld(hole[0]-10,hole[1]-1,hole[2]);
                net.minecraft.block.Block original=world.getBlock(at[0],at[1],at[2]);int meta=world.getBlockMetadata(at[0],at[1],at[2]);
                world.setBlock(at[0],at[1],at[2],net.minecraft.init.Blocks.air,0,2);
                require(!definition.validate(machine).isFormed(),"missing window/bridge/motor rejected");
                world.setBlock(at[0],at[1],at[2],original,meta,2);
            }
            int[] inlet=machine.localToWorld(1-10,1-1,0);
            String inletKey=inlet[0]+","+inlet[1]+","+inlet[2];
            net.minecraft.tileentity.TileEntity originalInlet=world.tiles.get(inletKey);
            world.setBlockMetadataWithNotify(inlet[0],inlet[1],inlet[2],com.mod.htgt6.api.hitech6.multiblock.GatewayType.FLUID_OUTPUT.metadata,2);
            world.tiles.put(inletKey,new com.mod.htgt6.common.multiblock.gateway.TileEntityMultiblockGateway(
                    com.mod.htgt6.api.hitech6.multiblock.GatewayType.FLUID_OUTPUT.metadata));
            require(!definition.validate(machine).isFormed(),"wrong gateway at reagent inlet rejected");
            world.setBlockMetadataWithNotify(inlet[0],inlet[1],inlet[2],com.mod.htgt6.api.hitech6.multiblock.GatewayType.FLUID_INPUT.metadata,2);
            world.tiles.put(inletKey,originalInlet);
            int[] optional=machine.localToWorld(12-10,0,8);
            world.setBlock(optional[0],optional[1],optional[2],FlotationChamberContent.casing,0,2);
            world.tiles.remove(optional[0]+","+optional[1]+","+optional[2]);
            require(definition.validate(machine).isFormed(),"optional item outlet may be casing");
        }
        java.nio.file.Path export=java.nio.file.Paths.get("build/flotation-layout.json");
        java.nio.file.Files.createDirectories(export.getParent());
        java.nio.file.Files.write(export,exported.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
