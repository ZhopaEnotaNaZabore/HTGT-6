package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Six attachment booleans cannot fit in legacy four-bit metadata. */
public final class TileEntityBackport120SculkVein extends TileEntity {
    private int faces;
    public int getFaces(){return faces&63;}
    public void setFaces(int value){faces=value&63;markDirty();if(worldObj!=null)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
    @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setByte("Faces",(byte)faces);}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);faces=n.getByte("Faces")&63;}
    @Override public Packet getDescriptionPacket(){NBTTagCompound n=new NBTTagCompound();writeToNBT(n);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,10,n);}
    @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());if(worldObj!=null)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
}
