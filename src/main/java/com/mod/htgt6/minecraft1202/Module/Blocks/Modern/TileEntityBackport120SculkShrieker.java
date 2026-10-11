package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/** Delays a shriek until its vibration has travelled from the source. */
public final class TileEntityBackport120SculkShrieker extends TileEntity {
    private int warningLevel;
    public void setWarningLevel(int level){warningLevel=level;markDirty();}
    public int takeWarningLevel(){int value=warningLevel;warningLevel=0;markDirty();return value;}
    private int pendingTicks=-1,sourceId=-1;private String sourceName="";
    public boolean receive(int distance,Entity source){
        if(worldObj==null||worldObj.isRemote||pendingTicks>=0)return false;
        EntityPlayer player=source instanceof EntityPlayer?(EntityPlayer)source:null;
        if(player==null)return false;
        pendingTicks=Math.max(1,distance);sourceId=player.getEntityId();sourceName=player.getCommandSenderName();markDirty();return true;
    }
    @Override public void updateEntity(){
        if(worldObj==null||worldObj.isRemote||pendingTicks<0)return;
        if(--pendingTicks>0)return;
        pendingTicks=-1;Entity source=worldObj.getEntityByID(sourceId);if(!(source instanceof EntityPlayer)&&!sourceName.isEmpty())source=worldObj.getPlayerEntityByName(sourceName);sourceId=-1;sourceName="";markDirty();
        if(source instanceof EntityPlayer&&getBlockType() instanceof BlockBackport120SculkShrieker)
            ((BlockBackport120SculkShrieker)getBlockType()).shriek(worldObj,xCoord,yCoord,zCoord,(EntityPlayer)source);
    }
    @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setInteger("WarningLevel",warningLevel);n.setShort("Pending",(short)pendingTicks);n.setString("Source",sourceName);}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);warningLevel=n.getInteger("WarningLevel");pendingTicks=n.hasKey("Pending")?n.getShort("Pending"):-1;sourceName=n.getString("Source");sourceId=-1;}
}
