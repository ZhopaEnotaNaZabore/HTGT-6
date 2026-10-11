package com.mod.htgt6.minecraft1202.Module.Entity.Exploration;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Distinct 1.20 trader-llama entity reusing the native 1.7 llama/caravan implementation. */
public final class EntityTraderLlama120 extends EntityLlama112 {
    public static final int DEFAULT_DESPAWN_DELAY=48000;
    private int despawnDelay=DEFAULT_DESPAWN_DELAY;
    public EntityTraderLlama120(World world){super(world);setTraderLlama(true);}
    public void setDespawnDelay(int ticks){despawnDelay=Math.max(0,ticks);}
    public int getDespawnDelay(){return despawnDelay;}
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();if(!worldObj.isRemote&&despawnDelay>0&&--despawnDelay==0)setDead();
    }
    @Override protected boolean canDespawn(){return false;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("DespawnDelay",despawnDelay);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);
        despawnDelay=tag.hasKey("DespawnDelay")?Math.max(0,tag.getInteger("DespawnDelay")):DEFAULT_DESPAWN_DELAY;setTraderLlama(true);}
}
