package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Native 1.7.10 equivalent of the shared 1.20 warning tracker used by shriekers. */
public final class SculkWarningManager120 {
    private static final String DATA="HTGT6SculkWarning";
    public static final int WARNING_COOLDOWN=200,WARNING_DECAY=12000,MAX_WARNING=4,SHARED_RADIUS=16;

    /** Zero means no new warning: callers must not start another summoning shriek. */
    @SuppressWarnings("unchecked")
    public static int warn(EntityPlayer source,int x,int y,int z){
        World world=source.worldObj;
        if(!canSummon(world)||source.capabilities.disableDamage||hasWarden(world,x,y,z))return 0;
        List<EntityPlayer> nearby=world.getEntitiesWithinAABB(EntityPlayer.class,
                AxisAlignedBB.getBoundingBox(x-16,y-16,z-16,x+17,y+17,z+17));
        List<EntityPlayer> eligible=new ArrayList<EntityPlayer>();
        for(EntityPlayer player:nearby)if(player.isEntityAlive()&&!player.capabilities.disableDamage
                &&player.getDistanceSq(x+.5,y+.5,z+.5)<256)eligible.add(player);
        if(!eligible.contains(source))eligible.add(source);
        long now=world.getTotalWorldTime();int shared=0;
        for(EntityPlayer player:eligible){
            NBTTagCompound state=state(player);decay(state,now);
            if(state.hasKey("Last")&&now-state.getLong("Last")<WARNING_COOLDOWN)return 0;
            shared=Math.max(shared,state.getByte("Level"));
        }
        shared=Math.min(MAX_WARNING,shared+1);
        for(EntityPlayer player:eligible)write(player,shared,now);
        return shared;
    }

    public static boolean canSummon(World world){
        return !world.isRemote&&world.difficultySetting!=EnumDifficulty.PEACEFUL
                &&(!world.getGameRules().hasRule("doWardenSpawning")||world.getGameRules().getGameRuleBooleanValue("doWardenSpawning"));
    }
    public static int level(EntityPlayer player){NBTTagCompound state=state(player);decay(state,player.worldObj.getTotalWorldTime());return state.getByte("Level");}

    private static boolean hasWarden(World world,int x,int y,int z){
        // Vanilla AABB.ofSize(...,48,48,48): half-extent 24, not 48.
        return !world.getEntitiesWithinAABB(EntityWarden120.class,
                AxisAlignedBB.getBoundingBox(x+.5-24,y+.5-24,z+.5-24,x+.5+24,y+.5+24,z+.5+24)).isEmpty();
    }
    /** Called after the 90-tick shriek; warnings remain at four after a successful summon. */
    public static boolean spawnWarden(World world,int x,int y,int z){
        if(!canSummon(world))return false;
        for(int attempt=0;attempt<20;attempt++){
            int px=x+world.rand.nextInt(11)-5,pz=z+world.rand.nextInt(11)-5;
            for(int py=y+6;py>=y-6;--py){
                if(py<1||py+3>=world.getActualHeight()||!world.checkChunksExist(px-1,py-1,pz-1,px+1,py+3,pz+1))continue;
                if(!world.getBlock(px,py-1,pz).isSideSolid(world,px,py-1,pz,net.minecraftforge.common.util.ForgeDirection.UP)
                        ||world.getBlock(px,py,pz).getMaterial().isSolid())continue;
                EntityWarden120 warden=new EntityWarden120(world);
                warden.setLocationAndAngles(px+.5D,py,pz+.5D,world.rand.nextFloat()*360F,0F);
                if(world.checkNoEntityCollision(warden.boundingBox)&&world.getCollidingBoundingBoxes(warden,warden.boundingBox).isEmpty()
                        &&!world.isAnyLiquid(warden.boundingBox)){
                    warden.beginEmerging();
                    if(world.spawnEntityInWorld(warden))return true;
                }
                break;
            }
        }
        return false;
    }
    private static NBTTagCompound state(EntityPlayer player){
        NBTTagCompound root=player.getEntityData();
        if(!root.hasKey(DATA))root.setTag(DATA,new NBTTagCompound());
        return root.getCompoundTag(DATA);
    }
    private static void decay(NBTTagCompound state,long now){
        long last=state.hasKey("Decay")?state.getLong("Decay"):state.getLong("Last");int level=state.getByte("Level");
        if(state.hasKey("Last")&&now-last>=WARNING_DECAY){int periods=(int)((now-last)/WARNING_DECAY);
            state.setByte("Level",(byte)Math.max(0,level-periods));state.setLong("Decay",last+(long)periods*WARNING_DECAY);}
    }
    private static void write(EntityPlayer player,int level,long last){NBTTagCompound state=state(player);state.setByte("Level",(byte)level);state.setLong("Last",last);state.setLong("Decay",last);}
    private SculkWarningManager120(){ }
}
