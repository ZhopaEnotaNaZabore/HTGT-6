package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Data-driven wooden content family. One descriptor creates, wires, registers and recipes all
 * stateful 1.7 shells while preserving a distinct registry identity for every modern block.
 */
public final class Backport120WoodFamily {
    public final String name;
    public final Block planks,stairs,fence,fenceGate,trapdoor,pressurePlate,button;
    public final BlockBackport120Slab slab,doubleSlab;
    public final BlockBackport120Door doorBlock;
    public final Item door;
    public final BlockBackport120Sign standingSign,wallSign;
    public final Item sign;
    public final BlockBackport120HangingSign hangingSignBlock,wallHangingSignBlock;
    public final Item hangingSign;

    public Backport120WoodFamily(String name){
        this(name,new BlockBackport120Simple(Backport120BlockStat.WOOD,name+"_planks"),
                name+"_planks",name+"_planks");
    }
    public Backport120WoodFamily(String name,Block planks,String plankTexture,String fenceTexture){
        this.name=name;
        this.planks=planks;
        stairs=new BlockBackport120Stairs(planks,name+"_stairs",plankTexture,"axe",0);
        slab=new BlockBackport120Slab(false,name,plankTexture,planks,3.0F,
                planks.getMaterial(),Backport120BlockStat.WOOD.sound,"axe",0);
        doubleSlab=new BlockBackport120Slab(true,name,plankTexture,planks,3.0F,
                planks.getMaterial(),Backport120BlockStat.WOOD.sound,"axe",0);
        fence=new BlockBackport120Fence(name+"_fence",fenceTexture);
        fenceGate=new BlockBackport120FenceGate(name+"_fence_gate",fenceTexture);
        doorBlock=new BlockBackport120Door(name+"_door");
        door=new ItemBackport120Door(name+"_door",doorBlock);
        trapdoor=new BlockBackport120TrapDoor(name+"_trapdoor");
        pressurePlate=new BlockBackport120PressurePlate(name+"_pressure_plate",plankTexture);
        button=new BlockBackport120Button(name+"_button",plankTexture);
        standingSign=new BlockBackport120Sign(name+"_sign",true);
        wallSign=new BlockBackport120Sign(name+"_wall_sign",false);
        sign=new ItemBackport120Sign(name+"_sign",standingSign,wallSign);
        hangingSignBlock=new BlockBackport120HangingSign(name+"_hanging_sign",false);
        wallHangingSignBlock=new BlockBackport120HangingSign(name+"_wall_hanging_sign",true);
        hangingSign=new ItemBackport120HangingSign(name+"_hanging_sign",hangingSignBlock,wallHangingSignBlock);
        slab.setSingle(slab);doubleSlab.setSingle(slab);doorBlock.setDoorItem(door);
        standingSign.setSignItem(sign);wallSign.setSignItem(sign);
        hangingSignBlock.setSignItem(hangingSign);wallHangingSignBlock.setSignItem(hangingSign);
    }

    public void register(){
        block(planks,name+"_planks");block(stairs,name+"_stairs");
        GameRegistry.registerBlock(slab,ItemBlockBackport120Slab.class,"backport_1_20_"+name+"_slab",
                slab,doubleSlab,Boolean.FALSE);
        GameRegistry.registerBlock(doubleSlab,ItemBlockBackport120Technical.class,
                "backport_1_20_double_"+name+"_slab");
        block(fence,name+"_fence");block(fenceGate,name+"_fence_gate");
        GameRegistry.registerBlock(doorBlock,ItemBlockBackport120Technical.class,
                "backport_1_20_"+name+"_door_block");
        GameRegistry.registerItem(door,"backport_1_20_"+name+"_door");
        block(trapdoor,name+"_trapdoor");block(pressurePlate,name+"_pressure_plate");block(button,name+"_button");
        GameRegistry.registerBlock(standingSign,ItemBlockBackport120Technical.class,
                "backport_1_20_"+name+"_standing_sign");
        GameRegistry.registerBlock(wallSign,ItemBlockBackport120Technical.class,
                "backport_1_20_"+name+"_wall_sign");
        GameRegistry.registerItem(sign,"backport_1_20_"+name+"_sign");
        GameRegistry.registerBlock(hangingSignBlock,ItemBlockBackport120Technical.class,
                "backport_1_20_"+name+"_hanging_sign_block");
        GameRegistry.registerBlock(wallHangingSignBlock,ItemBlockBackport120Technical.class,
                "backport_1_20_"+name+"_wall_hanging_sign");
        GameRegistry.registerItem(hangingSign,"backport_1_20_"+name+"_hanging_sign");
    }

    public void addRecipes(Item chain,Block hangingMaterial,Block... plankSources){
        for(Block source:plankSources)GameRegistry.addShapelessRecipe(new ItemStack(planks,4),source);
        GameRegistry.addRecipe(new ItemStack(stairs,4),"P  ","PP ","PPP",'P',planks);
        GameRegistry.addRecipe(new ItemStack(slab,6),"PPP",'P',planks);
        GameRegistry.addRecipe(new ItemStack(fence,3),"PSP","PSP",'P',planks,'S',net.minecraft.init.Items.stick);
        GameRegistry.addRecipe(new ItemStack(fenceGate),"SPS","SPS",'P',planks,'S',net.minecraft.init.Items.stick);
        GameRegistry.addRecipe(new ItemStack(door,3),"PP","PP","PP",'P',planks);
        GameRegistry.addRecipe(new ItemStack(trapdoor,2),"PPP","PPP",'P',planks);
        GameRegistry.addRecipe(new ItemStack(pressurePlate),"PP",'P',planks);
        GameRegistry.addShapelessRecipe(new ItemStack(button),planks);
        GameRegistry.addRecipe(new ItemStack(sign,3),"PPP","PPP"," S ",'P',planks,'S',net.minecraft.init.Items.stick);
        GameRegistry.addRecipe(new ItemStack(hangingSign,6),"C C","LLL","LLL",'C',chain,'L',hangingMaterial);
    }
    private static void block(Block block,String name){GameRegistry.registerBlock(block,"backport_1_20_"+name);}
}
