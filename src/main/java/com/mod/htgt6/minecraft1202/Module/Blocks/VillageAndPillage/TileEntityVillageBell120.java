package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Synchronized ringing state for the 1.20 bell body renderer. */
public final class TileEntityVillageBell120 extends TileEntity {
    private static final int RING_DURATION = 50;
    private boolean ringing;
    private int ringTicks;
    private int ringDirection;
    // All four metadata bits are occupied by facing/attachment. Persist POWERED here.
    private boolean powered;
    public boolean updatePower(boolean next) {
        if(powered==next)return false;
        powered=next;markDirty();return next;
    }

    public void ring(int side, int fallbackDirection) {
        ringDirection = directionFromSide(side, fallbackDirection);
        ringTicks = 0;
        ringing = true;
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void updateEntity() {
        if (!ringing) return;
        if (++ringTicks >= RING_DURATION) {
            ringing = false;
            ringTicks = 0;
            markDirty();
        }
    }

    public boolean isRinging() { return ringing; }
    public int getRingDirection() { return ringDirection; }
    public float getRingTicks(float partialTicks) { return ringing ? ringTicks + partialTicks : 0.0F; }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Powered120",powered);
    }

    private void writeBellState(NBTTagCompound tag) {
        tag.setBoolean("Ringing120", ringing);
        tag.setInteger("RingTicks120", ringTicks);
        tag.setByte("RingDirection120", (byte)ringDirection);
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        powered=tag.getBoolean("Powered120");
        // Ringing is transient in 1.20; old saved animation tags must not restart it.
        ringing=false;ringTicks=0;ringDirection=0;
    }

    private void readBellState(NBTTagCompound tag) {
        ringing = tag.getBoolean("Ringing120");
        ringTicks = Math.max(0, Math.min(RING_DURATION - 1, tag.getInteger("RingTicks120")));
        ringDirection = tag.getByte("RingDirection120") & 3;
    }

    @Override public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeBellState(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
        readBellState(packet.func_148857_g());
    }

    static int directionFromSide(int side, int fallbackDirection) {
        if (side == 2) return 0;
        if (side == 5) return 1;
        if (side == 3) return 2;
        if (side == 4) return 3;
        return fallbackDirection & 3;
    }

    public static void assertRoundTripFixture() {
        TileEntityVillageBell120 source = new TileEntityVillageBell120();
        source.ring(5, 0);
        NBTTagCompound tag = new NBTTagCompound();
        source.writeBellState(tag);
        TileEntityVillageBell120 copy = new TileEntityVillageBell120();
        copy.readBellState(tag);
        if (!copy.isRinging() || copy.getRingDirection() != 1
                || directionFromSide(2, 3) != 0 || directionFromSide(1, 3) != 3)
            throw new IllegalStateException("bell ringing state/direction round-trip failed");
    }
}
