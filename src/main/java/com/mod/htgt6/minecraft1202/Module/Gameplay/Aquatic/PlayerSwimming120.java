package com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

/** Aquatic travel state, steering and water physics, shared with the server pose lifecycle. */
public final class PlayerSwimming120 {
    // Neither persisted into worlds nor shared by entity IDs between client and server.
    private static final Map<EntityPlayer,Boolean> SWIMMERS=Collections.synchronizedMap(new WeakHashMap<EntityPlayer,Boolean>());
    private static final Map<EntityPlayer,Boolean> REMOTE_PLAYERS=Collections.synchronizedMap(new WeakHashMap<EntityPlayer,Boolean>());
    private PlayerSwimming120() {}

    public static boolean isSwimming(EntityPlayer player) {
        return SWIMMERS.containsKey(player);
    }

    public static void setClientState(EntityPlayer player,boolean swimming,boolean localPlayer) {
        if(!player.worldObj.isRemote)return;
        if(localPlayer)REMOTE_PLAYERS.remove(player);else REMOTE_PLAYERS.put(player,Boolean.TRUE);
        setState(player,swimming);
    }

    public static void clear(EntityPlayer player) {
        SWIMMERS.remove(player);REMOTE_PLAYERS.remove(player);
        SwimmingPose120.clear(player);
    }

    private static void setState(EntityPlayer player,boolean swimming) {
        if(swimming)SWIMMERS.put(player,Boolean.TRUE);else SWIMMERS.remove(player);
    }

    /** Server ticks use this even when movement comes directly from C03 packets. */
    public static void updateState(EntityPlayer player) {
        if(!com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999){clear(player);return;}
        boolean swimming=player.isEntityAlive()&&!player.capabilities.isFlying&&player.ridingEntity==null
                &&player.isSprinting()&&player.isInWater()
                &&(isSwimming(player)||player.isInsideOfMaterial(Material.water));
        setState(player,swimming);
        SwimmingPose120.update(player);
    }

    public static void beforeTravel(EntityLivingBase entity,boolean jumping) {
        if(!com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999)return;
        if(!(entity instanceof EntityPlayer))return;
        EntityPlayer player=(EntityPlayer)entity;
        if(player.worldObj.isRemote&&REMOTE_PLAYERS.containsKey(player))return;
        updateState(player);
        if(player.isInWater()&&!player.capabilities.isFlying&&player.isSneaking())player.motionY-=.04D;
        if(!isSwimming(player))return;
        double lookY=player.getLookVec().yCoord;
        // Modern Player.travel only steers upward at the surface when jumping.
        boolean waterAbove=player.worldObj.getBlock(MathHelper.floor_double(player.posX),
                MathHelper.floor_double(player.boundingBox.minY+.9D),MathHelper.floor_double(player.posZ)).getMaterial()==Material.water;
        if(lookY<=0||jumping||waterAbove)
            player.motionY+=(lookY-player.motionY)*(lookY<-.2D?.085D:.06D);
    }

    public static float waterDrag(EntityLivingBase entity,float ordinary) {
        if(!com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999)return ordinary;
        return entity instanceof EntityPlayer&&!((EntityPlayer)entity).capabilities.isFlying
                &&entity.isSprinting()?.9F:ordinary;
    }
    public static double waterGravity(double legacy,EntityLivingBase entity) {
        if(!com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999)return legacy;
        if(!(entity instanceof EntityPlayer))return legacy;
        return entity.isSprinting()?0D:.005D;
    }
}
