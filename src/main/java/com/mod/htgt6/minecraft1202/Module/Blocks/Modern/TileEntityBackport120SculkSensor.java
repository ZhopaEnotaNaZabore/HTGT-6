package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120SculkHandler;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/** Persistent 1.20 sensor phase, frequency, travel delay and calibrated orientation. */
public final class TileEntityBackport120SculkSensor extends TileEntity {
    public static final int INACTIVE=0,ACTIVE=1,COOLDOWN=2;
    private int phase,power,lastFrequency,facing=2,pendingTicks=-1,pendingFrequency;

    public int getPhase(){return phase;}
    public int getPower(){return phase==ACTIVE?power:0;}
    public int getLastFrequency(){return lastFrequency;}
    public int getFacing(){return facing;}
    public void setFacing(int side){facing=side<2||side>5?2:side;changed();}

    public boolean receive(int frequency,int distance){
        if(worldObj==null||worldObj.isRemote||phase!=INACTIVE||frequency<1||frequency>15)return false;
        if(block()!=null&&block().isCalibrated()){
            int filter=calibrationSignal();
            if(filter>0&&filter!=frequency)return false;
        }
        int delay=Math.max(1,distance);
        if(pendingTicks>=0&&pendingTicks<=delay)return false;
        pendingTicks=delay;pendingFrequency=frequency;changed();return true;
    }

    @Override public void updateEntity(){
        if(worldObj==null||worldObj.isRemote)return;
        if(pendingTicks>=0&&--pendingTicks<=0){pendingTicks=-1;activate(pendingFrequency);}
    }

    private void activate(int frequency){
        if(phase!=INACTIVE)return;
        phase=ACTIVE;power=lastFrequency=Math.max(1,Math.min(15,frequency));
        syncPower();worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,getBlockType(),30);
        worldObj.playSoundEffect(xCoord+.5D,yCoord+.5D,zCoord+.5D,"random.click",.8F,.55F+power*.025F);
        for(ForgeDirection side:ForgeDirection.VALID_DIRECTIONS){
            int x=xCoord+side.offsetX,y=yCoord+side.offsetY,z=zCoord+side.offsetZ;
            if(worldObj.getBlock(x,y,z)==Backport120Content.amethystBlock)
                Backport120SculkHandler.emit(worldObj,x,y,z,power,null,true);
        }
    }

    public void scheduledTick(){
        if(worldObj==null||worldObj.isRemote)return;
        if(phase==ACTIVE){phase=COOLDOWN;power=0;syncPower();worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,getBlockType(),10);}
        else if(phase==COOLDOWN){phase=INACTIVE;syncPower();}
    }

    private int calibrationSignal(){
        ForgeDirection rear=ForgeDirection.getOrientation(opposite(facing));
        int x=xCoord+rear.offsetX,y=yCoord+rear.offsetY,z=zCoord+rear.offsetZ;
        return Math.max(0,Math.min(15,worldObj.getIndirectPowerLevelTo(x,y,z,facing)));
    }
    private static int opposite(int side){return side==2?3:side==3?2:side==4?5:4;}
    private BlockBackport120SculkSensor block(){return getBlockType() instanceof BlockBackport120SculkSensor?(BlockBackport120SculkSensor)getBlockType():null;}
    private void syncPower(){
        changed();worldObj.notifyBlocksOfNeighborChange(xCoord,yCoord,zCoord,getBlockType());
        worldObj.notifyBlocksOfNeighborChange(xCoord,yCoord-1,zCoord,getBlockType());
        worldObj.func_147453_f(xCoord,yCoord,zCoord,getBlockType());
    }
    private void changed(){markDirty();if(worldObj!=null)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}

    @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setByte("Phase",(byte)phase);n.setByte("Power",(byte)power);n.setByte("Frequency",(byte)lastFrequency);n.setByte("Facing",(byte)facing);n.setShort("Pending",(short)pendingTicks);n.setByte("PendingFrequency",(byte)pendingFrequency);}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);phase=n.getByte("Phase");power=n.getByte("Power");lastFrequency=n.getByte("Frequency");facing=n.hasKey("Facing")?n.getByte("Facing"):2;pendingTicks=n.hasKey("Pending")?n.getShort("Pending"):-1;pendingFrequency=n.getByte("PendingFrequency");}
    @Override public Packet getDescriptionPacket(){NBTTagCompound n=new NBTTagCompound();writeToNBT(n);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,9,n);}
    @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());if(worldObj!=null)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
}
