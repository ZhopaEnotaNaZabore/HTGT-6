package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.nbt.NBTTagCompound;

/** Uses the native 1.7.10 sign text packet/editor while retaining a distinct save id. */
public final class TileEntityBackport120Sign extends TileEntitySign {
    private boolean hangingSignAttached;
    private int color;
    public int getColor(){return color;}
    public void setColor(int value){color=Math.max(0,Math.min(15,value));markDirty();if(worldObj!=null)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
    @Override public net.minecraft.network.Packet getDescriptionPacket(){NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,0,tag);}
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,net.minecraft.network.play.server.S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());}

    public boolean isHangingSignAttached(){return hangingSignAttached;}
    public void setHangingSignAttached(boolean attached){hangingSignAttached=attached;markDirty();}

    @Override public void writeToNBT(NBTTagCompound nbt){
        super.writeToNBT(nbt); nbt.setBoolean("FuturaHangingAttached",hangingSignAttached);
        nbt.setInteger("HTGT6SignColor",color);
    }
    @Override public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt); hangingSignAttached=nbt.getBoolean("FuturaHangingAttached");
        color=Math.max(0,Math.min(15,nbt.getInteger("HTGT6SignColor")));
    }
}
