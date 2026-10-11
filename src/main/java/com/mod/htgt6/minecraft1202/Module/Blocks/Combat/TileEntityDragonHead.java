package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public final class TileEntityDragonHead extends TileEntity {
    private int rotation;
    private int animationTicks;
    private boolean animated;
    @Override public void updateEntity() {
        animated = worldObj != null && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        if (animated) ++animationTicks;
    }
    public float getAnimation(float partialTick) {
        return animationTicks + (animated ? partialTick : 0.0F);
    }
    public int getRotation() { return rotation & 15; }
    public void setRotation(int value) { rotation = value & 15; markDirty(); }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); rotation = tag.getByte("Rot") & 15; }
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setByte("Rot", (byte) rotation); }
    @Override public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound(); writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 5, tag);
    }
    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
