package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.*;
import net.minecraft.util.MovingObjectPosition;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Target;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityCrossbowRocket143;

/** Server-only side effects; no client-only classes, no new packet or entity identity. */
public final class TargetImpact116 {
    public static void onImpact(Entity projectile,MovingObjectPosition hit){
        if(projectile==null||projectile.worldObj==null||projectile.worldObj.isRemote||hit==null
                ||hit.typeOfHit!=MovingObjectPosition.MovingObjectType.BLOCK||hit.hitVec==null)return;
        if(projectile instanceof EntityThrownTrident&&!((EntityThrownTrident)projectile).acceptsTargetImpact116())return;
        if(!projectile.worldObj.blockExists(hit.blockX,hit.blockY,hit.blockZ))return;
        net.minecraft.block.Block block=projectile.worldObj.getBlock(hit.blockX,hit.blockY,hit.blockZ);
        if(!(block instanceof BlockBackport120Target))return;
        if(projectile instanceof EntityFishHook)anchorHook((EntityFishHook)projectile,hit,block);
        int strength=TargetSignal116.strength(hit.sideHit,hit.hitVec.xCoord,hit.hitVec.yCoord,hit.hitVec.zCoord);
        ((BlockBackport120Target)block).hit(projectile.worldObj,hit.blockX,hit.blockY,hit.blockZ,strength,
                TargetSignal116.pulseTicks(projectile instanceof EntityArrow||projectile instanceof EntityThrownTrident));
        Entity owner=owner(projectile);
        if(owner instanceof EntityPlayer&&strength==15){
            // The official target_hit predicate measures horizontal projectile/player distance.
            double dx=projectile.posX-owner.posX,dz=projectile.posZ-owner.posZ;
            if(dx*dx+dz*dz>=900D)com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete((EntityPlayer)owner,"bullseye");
        }
    }

    public static int embeddingMetadata(int actual,int saved,net.minecraft.block.Block embeddedBlock){
        return embeddedBlock instanceof BlockBackport120Target?saved:actual;
    }
    private static void anchorHook(EntityFishHook hook,MovingObjectPosition hit,net.minecraft.block.Block block){
        // Legacy bobbers set inGround on impact but do not populate the anchor there.
        // Restrict the correction to this target, preserving ordinary fishing behavior.
        cpw.mods.fml.common.ObfuscationReflectionHelper.setPrivateValue(EntityFishHook.class,hook,hit.blockX,"field_146037_g");
        cpw.mods.fml.common.ObfuscationReflectionHelper.setPrivateValue(EntityFishHook.class,hook,hit.blockY,"field_146048_h");
        cpw.mods.fml.common.ObfuscationReflectionHelper.setPrivateValue(EntityFishHook.class,hook,hit.blockZ,"field_146050_i");
        cpw.mods.fml.common.ObfuscationReflectionHelper.setPrivateValue(EntityFishHook.class,hook,block,"field_146046_j");
    }
    private static Entity owner(Entity projectile){
        if(projectile instanceof EntityArrow)return ((EntityArrow)projectile).shootingEntity;
        if(projectile instanceof EntityThrowable)return ((EntityThrowable)projectile).getThrower();
        if(projectile instanceof EntityFireball)return ((EntityFireball)projectile).shootingEntity;
        if(projectile instanceof EntityFishHook)return ((EntityFishHook)projectile).field_146042_b;
        if(projectile instanceof EntityCrossbowRocket143)return ((EntityCrossbowRocket143)projectile).getOwner();
        return null;
    }
    private TargetImpact116(){}
}
