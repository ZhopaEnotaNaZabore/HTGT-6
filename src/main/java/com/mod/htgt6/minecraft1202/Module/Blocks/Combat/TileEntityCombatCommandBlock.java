package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityCommandBlock;

/** Persistent powered/automatic state absent from the 1.7 command-block tile. */
public final class TileEntityCombatCommandBlock extends TileEntityCommandBlock {
    private boolean powered;
    private boolean automatic;
    private long lastExecution=-1L;
    private boolean updateLastExecution=true,conditionMet,started;
    public boolean claimExecution(long tick) {
        if(lastExecution==tick)return false;
        lastExecution=updateLastExecution?tick:-1L;markDirty();return true;
    }
    public long getLastExecution(){return lastExecution;}
    /** Legacy CommandBlockLogic has no success-count setter. Preserve its other fields through NBT. */
    public void clearSuccessCount(){
        if(func_145993_a().func_145760_g()==0)return;
        NBTTagCompound data=new NBTTagCompound();func_145993_a().func_145758_a(data);
        data.setInteger("SuccessCount",0);func_145993_a().func_145759_b(data);markDirty();
    }
    public boolean isPowered() { return powered; }
    public void setPowered(boolean value) { powered = value; markDirty(); }
    public boolean isAutomatic() { return automatic; }
    public void setAutomatic(boolean value) { boolean changed=!automatic&&value;automatic = value; markDirty();if(changed)scheduleRepeating(); }
    public boolean isConditionMet(){return conditionMet;}
    public void captureCondition(){if(worldObj!=null)conditionMet=BlockCombatCommand.conditionMet(worldObj,xCoord,yCoord,zCoord);}
    private void scheduleRepeating(){
        if(worldObj!=null&&!worldObj.isRemote&&worldObj.getBlock(xCoord,yCoord,zCoord) instanceof BlockCombatCommand
                &&((BlockCombatCommand)worldObj.getBlock(xCoord,yCoord,zCoord)).getMode()==BlockCombatCommand.REPEATING
                &&(automatic||powered)){captureCondition();worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,worldObj.getBlock(xCoord,yCoord,zCoord),1);}
    }
    @Override public void updateEntity(){if(!started&&worldObj!=null&&!worldObj.isRemote){started=true;scheduleRepeating();}}
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); powered = tag.getBoolean("powered"); automatic = tag.getBoolean("auto");
        updateLastExecution=!tag.hasKey("UpdateLastExecution")||tag.getBoolean("UpdateLastExecution");
        lastExecution=updateLastExecution&&tag.hasKey("LastExecution",99)?tag.getLong("LastExecution"):-1L;
        conditionMet=tag.getBoolean("conditionMet");started=false;
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setBoolean("powered", powered); tag.setBoolean("auto", automatic);
        tag.setLong("LastExecution",lastExecution);
        tag.setBoolean("UpdateLastExecution",updateLastExecution);tag.setBoolean("conditionMet",conditionMet);
    }
}
