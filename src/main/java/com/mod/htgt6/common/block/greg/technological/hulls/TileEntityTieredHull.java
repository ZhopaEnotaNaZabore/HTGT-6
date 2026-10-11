package com.mod.htgt6.common.block.greg.technological.hulls;

import gregapi.code.TagData;
import gregapi.data.TD;
import gregapi.tileentity.energy.ITileEntityEnergy;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

/** One dynamic TE for all hull tiers. Output at the ring, input on the other five faces, 1 A. */
public final class TileEntityTieredHull extends TileEntity implements IEnergySink, IEnergySource, ITileEntityEnergy {
    private final HullEnergyBuffer buffer = new HullEnergyBuffer(0);
    private boolean registered, sending, unloaded, exploding;
    private boolean restorePending;
    private double savedEnergy, savedVoltage;
    private int networkFacing = -1;

    private BlockTieredHull hull() {
        return worldObj != null && worldObj.getBlock(xCoord,yCoord,zCoord) instanceof BlockTieredHull
                ? (BlockTieredHull) worldObj.getBlock(xCoord,yCoord,zCoord) : null;
    }
    public long maximumVoltage() { BlockTieredHull b = hull(); return b == null ? 0 : b.getVoltage(); }
    private int front() { return BlockTieredHull.front(getBlockMetadata()); }
    private long tick() { return worldObj.getTotalWorldTime(); }
    private boolean ready() {
        if (worldObj == null || worldObj.isRemote || isDead() || restorePending) return false;
        BlockTieredHull block=hull();
        if (block == null) return false;
        buffer.setCapacity(block.getEnergyCapacity());
        return true;
    }

    @Override public void updateEntity() {
        if (worldObj == null || worldObj.isRemote || isDead()) return;
        if (restorePending) {
            if (hull() == null) return;
            buffer.setCapacity(hull().getEnergyCapacity());
            buffer.restore(maximumVoltage(), savedEnergy, savedVoltage);
            restorePending = false;
        }
        if (registered && networkFacing != front()) leaveNetwork();
        if (!registered) {
            networkFacing = front();
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            registered = true;
        }
        // IC2 pulls through its network. Only native GT6 neighbors are pushed here.
        double offered = internalOffer();
        long packet = (long) buffer.voltage();
        if (packet <= 0 || packet != buffer.voltage() || offered < packet) return;
        ForgeDirection d = ForgeDirection.getOrientation(front());
        int x=xCoord+d.offsetX, y=yCoord+d.offsetY, z=zCoord+d.offsetZ;
        if (!worldObj.blockExists(x,y,z)) return;
        TileEntity target = worldObj.getTileEntity(x,y,z);
        if (!(target instanceof gregapi.tileentity.ITileEntityEnergy)) return;
        sending = true;
        long used;
        try { used = ITileEntityEnergy.Util.emitEnergyToSide(TD.Energy.EU, (byte)front(), packet, 1, this); }
        finally { sending = false; }
        if (used > 0) { buffer.consume(tick(), packet); markDirty(); }
    }
    @Override public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection side) { return side != null && HullAppearance.accepts(front(),side.ordinal()); }
    @Override public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection side) {
        return side != null && HullAppearance.emits(front(),side.ordinal()) && !(receiver instanceof gregapi.tileentity.ITileEntityEnergy);
    }
    @Override public double getDemandedEnergy() { return ready() && buffer.canReceive(tick()) ? maximumVoltage() : 0; }
    @Override public int getSinkTier() { BlockTieredHull b=hull(); return b == null ? 0 : b.getTier(); }
    @Override public double injectEnergy(ForgeDirection side, double amount, double voltage) {
        if (!ready() || side == null || !HullAppearance.accepts(front(),side.ordinal())) return amount;
        if (Double.isFinite(amount) && amount > 0 && Double.isFinite(voltage) && voltage > maximumVoltage()) {
            explodeHull();
            return 0;
        }
        double accepted=buffer.receive(tick(), maximumVoltage(), amount, voltage, true);
        if (accepted > 0) markDirty();
        return amount-accepted;
    }
    private double internalOffer() { return ready() && !sending ? buffer.offer(tick()) : 0; }
    private boolean nativeOutput() {
        ForgeDirection d=ForgeDirection.getOrientation(front());
        int x=xCoord+d.offsetX,y=yCoord+d.offsetY,z=zCoord+d.offsetZ;
        return worldObj.blockExists(x,y,z)
                && worldObj.getTileEntity(x,y,z) instanceof gregapi.tileentity.ITileEntityEnergy;
    }
    @Override public double getOfferedEnergy() {
        // Never advertise the same packet to IC2 while GT6 owns the output edge.
        double offered=internalOffer();
        return offered > 0 && !nativeOutput() ? offered : 0;
    }
    @Override public void drawEnergy(double amount) {
        if (getOfferedEnergy() > 0) { buffer.consume(tick(),amount); markDirty(); }
    }
    @Override public int getSourceTier() {
        double voltage = buffer.voltage();
        int tier = 0;
        for (long limit=8; limit < voltage && tier < 16; limit *= 4) tier++;
        return tier;
    }
    @Override public boolean isDead() { return isInvalid() || unloaded || exploding; }
    private void explodeHull() {
        if (!ready()) return;
        float strength=2F + getSinkTier() * .4F;
        exploding=true;
        leaveNetwork();
        worldObj.setBlockToAir(xCoord,yCoord,zCoord);
        worldObj.createExplosion(null,xCoord+.5,yCoord+.5,zCoord+.5,strength,true);
    }
    @Override public void validate() { super.validate(); unloaded=false; }
    private void leaveNetwork() {
        if (registered && worldObj != null && !worldObj.isRemote)
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        registered=false;
    }
    @Override public void invalidate() { leaveNetwork(); super.invalidate(); }
    @Override public void onChunkUnload() { leaveNetwork(); unloaded=true; super.onChunkUnload(); }

    @Override public boolean isEnergyType(TagData type, byte side, boolean emitting) { return type == TD.Energy.EU; }
    @Override public Collection<TagData> getEnergyTypes(byte side) { return Collections.singleton(TD.Energy.EU); }
    @Override public boolean isEnergyAcceptingFrom(TagData type, byte side, boolean theoretical) {
        return type == TD.Energy.EU && HullAppearance.accepts(front(),side) && (theoretical || getDemandedEnergy() > 0);
    }
    @Override public boolean isEnergyEmittingTo(TagData type, byte side, boolean theoretical) {
        return type == TD.Energy.EU && side == front() && (theoretical || internalOffer() > 0);
    }
    private long magnitude(long size) { return size == Long.MIN_VALUE ? 0 : Math.abs(size); }
    @Override public long doEnergyInjection(TagData type, byte side, long size, long amps, boolean commit) {
        if (!ready() || amps <= 0 || type != TD.Energy.EU || !HullAppearance.accepts(front(),side)) return 0;
        long voltage = magnitude(size);
        if (voltage > maximumVoltage()) {
            if (commit) { explodeHull(); return 1; }
            return 0;
        }
        // GT6 returns packet counts, so never acknowledge a partially stored packet.
        if (buffer.receive(tick(),maximumVoltage(),voltage,voltage,false) != voltage || voltage <= 0) return 0;
        double accepted=buffer.receive(tick(),maximumVoltage(),voltage,voltage,commit);
        if (accepted <= 0) return 0;
        if (commit) markDirty();
        return 1;
    }
    @Override public long getEnergyDemanded(TagData type, byte side, long size) {
        return doEnergyInjection(type,side,size,1,false);
    }
    @Override public long getEnergyOffered(TagData type, byte side, long size) {
        long voltage=magnitude(size);
        return type == TD.Energy.EU && side == front() && voltage > 0
                && voltage == buffer.voltage() && internalOffer() >= voltage ? 1 : 0;
    }
    @Override public long doEnergyExtraction(TagData type, byte side, long size, long amps, boolean commit) {
        long offered=amps > 0 ? getEnergyOffered(type,side,size) : 0;
        if (commit && offered > 0) { buffer.consume(tick(),magnitude(size)); markDirty(); }
        return offered;
    }
    @Override public long getEnergySizeInputMin(TagData type, byte side) { return 1; }
    @Override public long getEnergySizeOutputMin(TagData type, byte side) { return 1; }
    @Override public long getEnergySizeInputRecommended(TagData type, byte side) { return maximumVoltage(); }
    @Override public long getEnergySizeOutputRecommended(TagData type, byte side) {
        return buffer.voltage() > 0 ? (long)buffer.voltage() : maximumVoltage();
    }
    @Override public long getEnergySizeInputMax(TagData type, byte side) { return maximumVoltage(); }
    @Override public long getEnergySizeOutputMax(TagData type, byte side) { return maximumVoltage(); }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("HullEnergy", restorePending ? savedEnergy : buffer.energy());
        tag.setDouble("HullVoltage", restorePending ? savedVoltage : buffer.voltage());
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        savedEnergy=tag.getDouble("HullEnergy"); savedVoltage=tag.getDouble("HullVoltage");
        restorePending=true;
    }
}
