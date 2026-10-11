package com.mod.htgt6.common.crusher;

import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Non-ticking, persisted ownership for an invisible mechanical collision cell. */
public final class TileCrusherCollision extends TileEntity {
    private int ownerX, ownerY, ownerZ, u, y, v, facing;
    private boolean bound;
    private List<double[]> boxes = Collections.emptyList();

    @Override public boolean canUpdate() { return false; }
    public boolean isBound() { return bound; }

    public void bind(MultiTileIndustrialCrusher owner, int localU, int localY, int localV) {
        ownerX = owner.xCoord; ownerY = owner.yCoord; ownerZ = owner.zCoord;
        u = localU; y = localY; v = localV;
        facing = worldObj.getBlockMetadata(ownerX, ownerY, ownerZ);
        bound = true;
        boxes = IndustrialCrusherCollision.cell(u, y, v, facing);
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean belongsTo(MultiTileIndustrialCrusher owner) {
        return bound && owner.xCoord == ownerX && owner.yCoord == ownerY && owner.zCoord == ownerZ;
    }

    public boolean ownerLoaded() {
        return bound && worldObj != null && worldObj.blockExists(ownerX, ownerY, ownerZ);
    }

    public MultiTileIndustrialCrusher owner() {
        if (!ownerLoaded()) return null;
        TileEntity tile = worldObj.getTileEntity(ownerX, ownerY, ownerZ);
        if (!(tile instanceof MultiTileIndustrialCrusher) || tile.isInvalid()) return null;
        MultiTileIndustrialCrusher owner = (MultiTileIndustrialCrusher) tile;
        int[] p = owner.localToWorld(u, y, v);
        return p[0] == xCoord && p[1] == yCoord && p[2] == zCoord
                && worldObj.getBlockMetadata(ownerX, ownerY, ownerZ) == facing ? owner : null;
    }

    public List<double[]> getBoxes() {
        MultiTileIndustrialCrusher owner = owner();
        return owner != null && owner.isStructureFormed() ? boxes : Collections.<double[]>emptyList();
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Bound", bound);
        tag.setIntArray("Owner", new int[] {ownerX, ownerY, ownerZ, u, y, v, facing});
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        int[] data = tag.getIntArray("Owner");
        bound = tag.getBoolean("Bound") && data.length == 7;
        if (bound) {
            ownerX=data[0]; ownerY=data[1]; ownerZ=data[2]; u=data[3]; y=data[4]; v=data[5]; facing=data[6];
            boxes = IndustrialCrusherCollision.cell(u, y, v, facing);
        } else boxes = Collections.emptyList();
    }

    @Override public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
