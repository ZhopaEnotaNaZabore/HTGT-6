package com.mod.htgt6.minecraft1202.Module.World.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared water-column bridge for the 1.20.2 bubble-column contract.
 *
 * <p>1.7 has neither BubbleColumnBlock nor waterlogged block states, so the
 * transient column is resolved from the uninterrupted full-water column on
 * every server tick in all world types. This keeps existing water IDs intact;
 * it is a computed column state, not a separate BubbleColumnBlock.</p>
 */
public final class FuturaBubbleColumn120 {
    private static final int NONE=0,DOWN=-1,UP=1;

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onWorldTick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||event.world.isRemote)return;
        // Entity updates can remove entities from loadedEntityList.  Work on
        // a snapshot, matching the stable entity iteration of modern levels.
        List<Entity> entities=new ArrayList<Entity>((List<Entity>)event.world.loadedEntityList);
        for(Entity entity:entities)apply(event.world,entity);
    }

    static void apply(World world,Entity entity){
        if(entity==null||entity.isDead||entity.boundingBox==null)return;
        if(entity instanceof net.minecraft.entity.EntityLivingBase) {
            net.minecraft.entity.EntityLivingBase living=(net.minecraft.entity.EntityLivingBase)entity;
            // These entities skip decreaseAirSupply in the legacy base tick,
            // so the ASM hook cannot replenish their air there.
            boolean skipsAirLoss=living.canBreatheUnderwater()||living.isPotionActive(net.minecraft.potion.Potion.waterBreathing)
                    ||(living instanceof net.minecraft.entity.player.EntityPlayer
                    &&((net.minecraft.entity.player.EntityPlayer)living).capabilities.disableDamage);
            if(skipsAirLoss) {
                int air=airSupply(living,living.getAir());
                if(air!=Integer.MIN_VALUE)living.setAir(air);
            }
        }
        int x=MathHelper.floor_double(entity.posX);
        int y=MathHelper.floor_double(entity.boundingBox.minY+1.0E-3D);
        int z=MathHelper.floor_double(entity.posZ);
        int direction=directionAt(world,x,y,z);
        if(direction==NONE)return;
        // BubbleColumnBlock uses the stronger "block above is air" test;
        // flowing water or a solid cap still receives the inside constants.
        boolean top=world.isAirBlock(x,y+1,z);
        entity.motionY=velocityAfter(direction==DOWN,top,entity.motionY);
        entity.fallDistance=0.0F;
        entity.velocityChanged=true;
    }

    /** Integer.MIN_VALUE asks the ASM hook to retain ordinary breathing rules. */
    public static int airSupply(net.minecraft.entity.EntityLivingBase entity,int air) {
        World world=entity.worldObj;
        int x=MathHelper.floor_double(entity.posX),y=MathHelper.floor_double(entity.posY+entity.getEyeHeight()),z=MathHelper.floor_double(entity.posZ);
        return directionAt(world,x,y,z)==NONE?Integer.MIN_VALUE:Math.min(300,air+4);
    }

    public static int directionAt(World world,int x,int y,int z) {
        if(world==null||!world.blockExists(x,y,z)||!isFullWater(world,x,y,z))return NONE;
        return findSource(world,x,y,z);
    }

    /** Exact Entity.onAboveBubbleCol/onInsideBubbleColumn vertical velocity. */
    public static double velocityAfter(boolean dragDown,boolean top,double velocityY){
        if(top)return dragDown?Math.max(-0.9D,velocityY-0.03D):Math.min(1.8D,velocityY+0.1D);
        return dragDown?Math.max(-0.3D,velocityY-0.03D):Math.min(0.7D,velocityY+0.06D);
    }

    private static int findSource(World world,int x,int waterY,int z){
        for(int y=waterY-1;y>=0;y--){
            if(isFullWater(world,x,y,z))continue;
            Block source=world.getBlock(x,y,z);
            if(source==FrostburnContent.magmaBlock)return DOWN;
            if(source==Blocks.soul_sand)return UP;
            return NONE;
        }
        return NONE;
    }

    private static boolean isFullWater(World world,int x,int y,int z){
        if(y<0||y>=world.getActualHeight())return false;
        Block block=world.getBlock(x,y,z);
        if(block==null||block.getMaterial()!=Material.water)return false;
        // Only source water participates. Falling and flowing legacy water
        // must not be treated as full source water merely because it is water.
        return block==Backport120Content.aquiferWater||world.getBlockMetadata(x,y,z)==0;
    }

    public FuturaBubbleColumn120(){ }
}
