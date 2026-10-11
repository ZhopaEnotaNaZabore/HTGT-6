package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.*;
import net.minecraft.dispenser.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.common.MinecraftForge;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;

public final class BuzzyBees115 {
    public static final BlockBeehive115 beeNest=new BlockBeehive115(true),beehive=new BlockBeehive115(false);
    private static boolean registered;
    public static void register(){
        if(registered)return;registered=true;
        GameRegistry.registerBlock(beeNest,"backport_1_20_bee_nest");GameRegistry.registerBlock(beehive,"backport_1_20_beehive");GameRegistry.registerTileEntity(TileEntityBeehive115.class,"htgt6:beehive_115");
        GameRegistry.addRecipe(new net.minecraftforge.oredict.ShapedOreRecipe(new ItemStack(beehive),"PPP","HHH","PPP",'P',"plankWood",'H',EtFVanillaGapContent120.honeycomb));
        BuzzyBees115 events=new BuzzyBees115();MinecraftForge.EVENT_BUS.register(events);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(events);
        com.mod.htgt6.minecraft1202.Module.World.Modern.BeeSaplings115 saplings=new com.mod.htgt6.minecraft1202.Module.World.Modern.BeeSaplings115();MinecraftForge.TERRAIN_GEN_BUS.register(saplings);cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(saplings);
        com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.Parity115 parity=new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.Parity115();MinecraftForge.EVENT_BUS.register(parity);cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(parity);GameRegistry.registerFuelHandler(parity);
        dispenser(Items.glass_bottle,false);dispenser(Items.shears,true);
        // Vanilla pistons are extended by StandalonePistonsASM115; no external solver.

    }
    private static void dispenser(final Item item,final boolean shears){
        final IBehaviorDispenseItem previous=(IBehaviorDispenseItem)BlockDispenser.dispenseBehaviorRegistry.getObject(item);
        BlockDispenser.dispenseBehaviorRegistry.putObject(item,new IBehaviorDispenseItem(){
            public ItemStack dispense(IBlockSource src,ItemStack stack){
                EnumFacing face=BlockDispenser.func_149937_b(src.getBlockMetadata());int x=src.getXInt()+face.getFrontOffsetX(),y=src.getYInt()+face.getFrontOffsetY(),z=src.getZInt()+face.getFrontOffsetZ();World w=src.getWorld();
                ItemStack result=BlockBeehive115.harvest(w,x,y,z,shears,null);
                if(result==null&&!shears&&w.getBlock(x,y,z).getMaterial()==net.minecraft.block.material.Material.water)result=new ItemStack(Items.potionitem,1,0);
                if(result==null)return previous.dispense(src,stack);
                if(shears){if(stack.attemptDamageItem(1,w.rand))stack.stackSize--;}
                else if(--stack.stackSize<=0){w.playAuxSFX(1000,src.getXInt(),src.getYInt(),src.getZInt(),0);return result;}
                net.minecraft.tileentity.TileEntityDispenser tile=(net.minecraft.tileentity.TileEntityDispenser)src.getBlockTileEntity();
                if(tile.func_146019_a(result)<0)new BehaviorDefaultDispenseItem().dispense(src,result);
                w.playAuxSFX(1000,src.getXInt(),src.getYInt(),src.getZInt(),0);return stack;
            }
        });
    }
    private static boolean onHoney(Entity e){return e.worldObj.getBlock(MathHelper.floor_double(e.posX),MathHelper.floor_double(e.boundingBox.minY-.2),MathHelper.floor_double(e.posZ))==EtFVanillaGapContent120.honeyBlock;}
    @SubscribeEvent public void fall(LivingFallEvent e){if(!onHoney(e.entityLiving))return;net.minecraft.potion.PotionEffect jump=e.entityLiving.getActivePotionEffect(net.minecraft.potion.Potion.jump);float threshold=3+(jump==null?0:jump.getAmplifier()+1);e.distance=threshold+Math.max(0,e.distance-threshold)*.2F;e.entityLiving.playSound("htgt6:block.honey_block.slide",1,1);}
    @SubscribeEvent public void jump(LivingEvent.LivingJumpEvent e){if(onHoney(e.entityLiving))e.entityLiving.motionY*=.5;}
    @SubscribeEvent public void worldTick(cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent e){if(e.phase==cpw.mods.fml.common.gameevent.TickEvent.Phase.END&&!e.world.isRemote&&e.world.getTotalWorldTime()%20==0)com.mod.htgt6.minecraft1202.Module.World.Modern.BeeRecovery115.tick(e.world);}
}
