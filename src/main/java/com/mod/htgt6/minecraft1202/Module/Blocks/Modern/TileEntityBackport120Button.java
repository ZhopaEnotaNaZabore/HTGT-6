package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Stores the 12 modern face/facing combinations that do not fit beside powered in metadata. */
public final class TileEntityBackport120Button extends TileEntity {
    public static final int WALL = 0;
    public static final int FLOOR = 1;
    public static final int CEILING = 2;

    private int face = WALL;
    private int facing = 2;

    public int getFace() { return face; }
    public int getFacing() { return facing; }

    public void setOrientation(int newFace, int newFacing) {
        face = newFace < WALL || newFace > CEILING ? WALL : newFace;
        facing = newFacing & 3;
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("Face", (byte) face);
        nbt.setByte("Facing", (byte) facing);
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        face = Math.max(WALL, Math.min(CEILING, nbt.getByte("Face")));
        facing = nbt.getByte("Facing") & 3;
    }

    @Override public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 8, nbt);
    }

    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
}
