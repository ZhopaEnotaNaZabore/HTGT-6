package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Invisible networked second-passenger anchor for the single-rider 1.7 Entity API. */
public final class EntityCamelSeat120 extends Entity {
    private static final int CAMEL_ID_WATCHER=20;
    private int missingCamelTicks;
    public EntityCamelSeat120(World world){super(world);noClip=true;setSize(.01F,.01F);}
    public EntityCamelSeat120(World world,EntityCamel120 camel){this(world);setCamel(camel);follow(camel);}
    @Override protected void entityInit(){dataWatcher.addObject(CAMEL_ID_WATCHER,Integer.valueOf(-1));}
    public void setCamel(EntityCamel120 camel){dataWatcher.updateObject(CAMEL_ID_WATCHER,Integer.valueOf(camel==null?-1:camel.getEntityId()));}
    public EntityCamel120 getCamel(){Entity value=worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(CAMEL_ID_WATCHER));return value instanceof EntityCamel120?(EntityCamel120)value:null;}
    @Override public void onUpdate(){super.onUpdate();EntityCamel120 camel=getCamel();
        if(camel==null||camel.isDead){if(++missingCamelTicks>40||!worldObj.isRemote)setDead();return;}
        missingCamelTicks=0;follow(camel);if(!worldObj.isRemote&&riddenByEntity==null&&ticksExisted>20)setDead();}
    private void follow(EntityCamel120 camel){double yaw=Math.toRadians(camel.rotationYaw);
        setPosition(camel.posX+Math.sin(yaw)*.7D,camel.posY+camel.getMountedYOffset(),camel.posZ-Math.cos(yaw)*.7D);rotationYaw=camel.rotationYaw;}
    @Override public void updateRiderPosition(){if(riddenByEntity!=null)riddenByEntity.setPosition(posX,posY+.35D,posZ);}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean writeToNBTOptional(NBTTagCompound tag){return false;}
    @Override protected void writeEntityToNBT(NBTTagCompound tag){}
    @Override protected void readEntityFromNBT(NBTTagCompound tag){}
}
