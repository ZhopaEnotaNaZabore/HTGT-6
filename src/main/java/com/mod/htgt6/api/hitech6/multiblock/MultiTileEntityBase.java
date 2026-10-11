package com.mod.htgt6.api.hitech6.multiblock;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;

/**
 * Shared controller base for HTGT6 multiblocks. It combines the HTGT6 module tier
 * system with declarative structure validation and detachable gateway capabilities.
 */
public abstract class MultiTileEntityBase extends TileEntityBaseMachine
        implements IMultiblockGatewayController {
    private static final long[] FLUID_GATEWAY_CAPACITY_BY_TIER = {
            16_000L, 32_000L, 64_000L, 128_000L,
            256_000L, 512_000L, 1_024_000L, 2_048_000L,
            4_096_000L, 8_192_000L, 16_384_000L, 32_768_000L,
            65_536_000L, 131_072_000L, 262_144_000L, 524_288_000L
    };
    private final IEnergySink inheritedEnergyInput = new InheritedEnergyInput();
    private final IEnergySource inheritedEnergyOutput = new InheritedEnergyOutput();
    /** Fractional IC2 withdrawals already delivered but not yet debited from the long buffer. */
    private double energyOutputDebt;
    private final EnumMap<GatewayType, IFluidHandler> fluidGatewayHandlers =
            new EnumMap<GatewayType, IFluidHandler>(GatewayType.class);
    private final EnumMap<GatewayType, IInventory> itemGatewayInventories =
            new EnumMap<GatewayType, IInventory>(GatewayType.class);
    private final EnumMap<GatewayType, int[]> itemGatewaySlots =
            new EnumMap<GatewayType, int[]>(GatewayType.class);
    private final EnumMap<GatewayType, IEnergySink> energyGatewayHandlers =
            new EnumMap<GatewayType, IEnergySink>(GatewayType.class);
    private final EnumMap<GatewayType, IEnergySource> energyOutputGatewayHandlers =
            new EnumMap<GatewayType, IEnergySource>(GatewayType.class);
    private MultiblockStructureDefinition structureDefinition;
    private MultiblockStructureDefinition.Result lastStructureResult;
    protected boolean structureFormed;
    protected String diagnostic = "STRUCTURE: NOT CHECKED";
    private int structureCheckTimer;

    protected MultiTileEntityBase(int inventorySize) {
        super(inventorySize);
    }

    /** Registers a detachable fluid gateway endpoint for this controller type. */
    protected final void configureFluidGateway(GatewayType type, IFluidHandler handler) {
        requireGatewayType(type);
        if (handler == null) fluidGatewayHandlers.remove(type);
        else fluidGatewayHandlers.put(type, handler);
    }

    /** Registers a detachable item gateway and the controller slots exposed through it. */
    protected final void configureItemGateway(GatewayType type, IInventory inventory, int[] slots) {
        requireGatewayType(type);
        if (inventory == null || slots == null) {
            itemGatewayInventories.remove(type);
            itemGatewaySlots.remove(type);
            return;
        }
        itemGatewayInventories.put(type, inventory);
        itemGatewaySlots.put(type, slots.clone());
    }

    /** Overrides the shared inherited-tier energy endpoint for a gateway type. */
    protected final void configureEnergyGateway(GatewayType type, IEnergySink handler) {
        requireGatewayType(type);
        if (handler == null) energyGatewayHandlers.remove(type);
        else energyGatewayHandlers.put(type, handler);
    }

    /** Overrides the inherited controller buffer exposed by an energy-output gateway. */
    protected final void configureEnergyOutputGateway(GatewayType type, IEnergySource handler) {
        requireGatewayType(type);
        if (handler == null) energyOutputGatewayHandlers.remove(type);
        else energyOutputGatewayHandlers.put(type, handler);
    }

    protected abstract MultiblockStructureDefinition createStructureDefinition();
    public MultiblockEnergyRole getEnergyRole() { return MultiblockEnergyRole.CONSUMER; }

    protected final MultiblockStructureDefinition getStructureDefinition() {
        if (structureDefinition == null) {
            structureDefinition = createStructureDefinition();
            if (structureDefinition == null) throw new IllegalStateException("Multiblock definition cannot be null");
        }
        return structureDefinition;
    }

    protected int getStructureCheckInterval() { return 40; }

    @Override
    public void updateEntity() {
        if (worldObj != null && !worldObj.isRemote && ++structureCheckTimer >= getStructureCheckInterval()) {
            structureCheckTimer = 0;
            checkStructure();
        }
        super.updateEntity();
    }

    public final boolean checkStructure() {
        if (worldObj == null || worldObj.isRemote) return structureFormed;
        if (!getStructureDefinition().areChunksLoaded(this)) return structureFormed;
        boolean wasFormed = structureFormed;
        String oldDiagnostic = diagnostic;
        MultiblockStructureDefinition.Result previousResult = lastStructureResult;
        MultiblockStructureDefinition.Result result = getStructureDefinition().validate(this);
        lastStructureResult = result;
        structureFormed = result.isFormed();
        diagnostic = result.getDiagnostic();
        if (structureFormed) {
            // Keep gateways that remain in a valid structure continuously linked.
            // Slot and pipe operations must never observe a transient unlink while
            // the periodic structure validation runs.
            if (previousResult != null) {
                for (IMultiblockGateway previous : previousResult.getGateways()) {
                    if (!result.getGateways().contains(previous)) previous.unlink();
                }
            }
            for (IMultiblockGateway gateway : result.getGateways()) gateway.link(this);
            onStructureFormed(!wasFormed, result);
        } else {
            if (previousResult != null) {
                for (IMultiblockGateway gateway : previousResult.getGateways()) gateway.unlink();
            }
            for (IMultiblockGateway gateway : getStructureDefinition().findGateways(this)) gateway.unlink();
            onStructureInvalidated(wasFormed, result);
        }
        if (wasFormed != structureFormed || !same(oldDiagnostic, diagnostic)) markControllerDirtyAndSync();
        return structureFormed;
    }

    protected void onStructureFormed(boolean newlyFormed, MultiblockStructureDefinition.Result result) { }
    protected void onStructureInvalidated(boolean wasFormed, MultiblockStructureDefinition.Result result) { }

    public final boolean isStructureFormed() { return structureFormed; }
    public final String getStructureDiagnostic() { return diagnostic; }
    public final int getStructureElementCount(String key) {
        return lastStructureResult == null ? 0 : lastStructureResult.getElementCount(key);
    }
    public final int getStructureGatewayCount(GatewayType type) {
        return lastStructureResult == null ? 0 : lastStructureResult.getGatewayCount(type);
    }
    public final int getStructureFluidTankCount(GatewayType type) {
        if (lastStructureResult == null) return 0;
        int count = 0;
        for (IMultiblockGateway gateway : lastStructureResult.getGateways()) {
            if (gateway.getGatewayType() == type) count += Math.max(0, gateway.getFluidTankCount());
        }
        return count;
    }
    public final Map<String, Integer> getStructureElementCounts() {
        return lastStructureResult == null ? Collections.<String, Integer>emptyMap()
                : lastStructureResult.getElementCounts();
    }

    /** Stable validation-order view used by controllers that attach per-gateway settings. */
    protected final List<IMultiblockGateway> getStructureGateways() {
        return lastStructureResult == null ? Collections.<IMultiblockGateway>emptyList()
                : lastStructureResult.getGateways();
    }

    /** Default horizontal transform: controller at local (0,0,0), positive V behind its face. */
    public int[] localToWorld(int u, int y, int v) {
        int facing = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if (facing == 3) return new int[] { xCoord - u, yCoord + y, zCoord - v };
        if (facing == 4) return new int[] { xCoord + v, yCoord + y, zCoord - u };
        if (facing == 5) return new int[] { xCoord - v, yCoord + y, zCoord + u };
        return new int[] { xCoord + u, yCoord + y, zCoord + v };
    }

    protected IFluidHandler resolveFluidGateway(GatewayType type) { return fluidGatewayHandlers.get(type); }
    protected IFluidHandler resolveFluidGateway(GatewayType type, int gatewayX, int gatewayY, int gatewayZ) {
        return resolveFluidGateway(type);
    }
    protected IInventory resolveItemGatewayInventory(GatewayType type) { return itemGatewayInventories.get(type); }
    protected int[] resolveItemGatewaySlots(GatewayType type) { return itemGatewaySlots.get(type); }
    protected IEnergySink resolveEnergyGateway(GatewayType type) {
        IEnergySink configured = energyGatewayHandlers.get(type);
        return configured != null ? configured : type == GatewayType.ENERGY_INPUT ? inheritedEnergyInput : null;
    }

    protected IEnergySource resolveEnergyOutputGateway(GatewayType type) {
        if (getEnergyRole() != MultiblockEnergyRole.GENERATOR || type != GatewayType.ENERGY_OUTPUT) return null;
        IEnergySource configured = energyOutputGatewayHandlers.get(type);
        return configured != null ? configured
                : type == GatewayType.ENERGY_OUTPUT ? inheritedEnergyOutput : null;
    }

    protected long resolveFluidGatewayCapacity(GatewayType type) {
        return type == GatewayType.FLUID_INPUT || type == GatewayType.FLUID_OUTPUT
                ? getFluidGatewayCapacityForTier(machineTier) : 0L;
    }

    /** Override when the controller front is not also the multiblock's structural skin. */
    protected Block resolveGatewayCasingBlock() {
        return worldObj == null ? null : worldObj.getBlock(xCoord, yCoord, zCoord);
    }
    protected int resolveGatewayCasingMetadata() { return 0; }

    /** LV starts at 16,000 L; every following tier doubles with long saturation. */
    public static long getFluidGatewayCapacityForTier(int tier) {
        int normalizedTier = Math.max(1, tier);
        if (normalizedTier <= FLUID_GATEWAY_CAPACITY_BY_TIER.length) {
            return FLUID_GATEWAY_CAPACITY_BY_TIER[normalizedTier - 1];
        }
        long capacity = FLUID_GATEWAY_CAPACITY_BY_TIER[FLUID_GATEWAY_CAPACITY_BY_TIER.length - 1];
        for (int current = FLUID_GATEWAY_CAPACITY_BY_TIER.length; current < normalizedTier; current++) {
            if (capacity > Long.MAX_VALUE / 2L) return Long.MAX_VALUE;
            capacity *= 2L;
        }
        return capacity;
    }

    @Override public final boolean isGatewayControllerFormed() { return structureFormed; }
    @Override public final int getGatewayTier() { return machineTier; }
    @Override public final long getGatewayMaxInputVoltage() { return getMaxInputVoltage(); }
    @Override public final long getGatewayMaxOutputVoltage() { return getMaxInputVoltage(); }
    @Override public final long getGatewayFluidCapacity(GatewayType type) { return resolveFluidGatewayCapacity(type); }
    @Override public final IFluidHandler getGatewayFluidHandler(GatewayType type) { return resolveFluidGateway(type); }
    @Override public final IFluidHandler getGatewayFluidHandler(
            GatewayType type, int gatewayX, int gatewayY, int gatewayZ) {
        return resolveFluidGateway(type, gatewayX, gatewayY, gatewayZ);
    }
    @Override public final IInventory getGatewayItemInventory(GatewayType type) { return resolveItemGatewayInventory(type); }
    @Override public final int[] getGatewayItemSlots(GatewayType type) { return resolveItemGatewaySlots(type); }
    @Override public final IEnergySink getGatewayEnergyHandler(GatewayType type) { return resolveEnergyGateway(type); }
    @Override public final IEnergySource getGatewayEnergyOutputHandler(GatewayType type) {
        return getEnergyRole() == MultiblockEnergyRole.GENERATOR && type == GatewayType.ENERGY_OUTPUT
                ? resolveEnergyOutputGateway(type) : null;
    }
    @Override public final Block getGatewayCasingBlock() { return resolveGatewayCasingBlock(); }
    @Override public final int getGatewayCasingMetadata() { return resolveGatewayCasingMetadata(); }

    /** Returns [offset, length] for a physical fluid gateway in validation order. */
    protected final int[] getStructureFluidTankRange(GatewayType type, int x, int y, int z) {
        if (lastStructureResult == null) return new int[] { 0, 0 };
        int offset = 0;
        for (IMultiblockGateway gateway : lastStructureResult.getGateways()) {
            if (gateway.getGatewayType() != type) continue;
            int length = Math.max(0, gateway.getFluidTankCount());
            if (gateway instanceof TileEntity) {
                TileEntity tile = (TileEntity) gateway;
                if (tile.xCoord == x && tile.yCoord == y && tile.zCoord == z) {
                    return new int[] { offset, length };
                }
            }
            offset += length;
        }
        return new int[] { 0, 0 };
    }

    // Multiblock controllers receive IC2 power only through their inherited-tier energy gateway.
    @Override public final boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection side) { return false; }
    @Override public final double getDemandedEnergy() { return 0; }
    @Override public final double injectEnergy(ForgeDirection side, double amount, double voltage) { return amount; }

    protected final void markControllerDirtyAndSync() {
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private static void requireGatewayType(GatewayType type) {
        if (type == null) throw new IllegalArgumentException("Gateway type cannot be null");
    }

    private static boolean same(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energyOutputDebt = nbt.getDouble("EnergyOutputDebt");
        if (!Double.isFinite(energyOutputDebt) || energyOutputDebt < 0D || energyOutputDebt >= 1D)
            energyOutputDebt = 0D;
        structureFormed = nbt.getBoolean("StructureFormed");
        if (nbt.hasKey("Diagnostic")) diagnostic = nbt.getString("Diagnostic");
    }

    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setDouble("EnergyOutputDebt", energyOutputDebt);
        nbt.setBoolean("StructureFormed", structureFormed);
        nbt.setString("Diagnostic", diagnostic == null ? "" : diagnostic);
    }

    /** Keeps formed state, tier and structural skin available to client-side gateway rendering. */
    @Override public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }

    private final class InheritedEnergyInput implements IEnergySink {
        @Override public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) { return structureFormed; }
        @Override public double getDemandedEnergy() { return structureFormed ? Math.max(0D, maxEnergy - energy) : 0D; }
        @Override public int getSinkTier() { return Math.max(1, machineTier); }
        @Override public double injectEnergy(ForgeDirection direction, double amount, double voltage) {
            if (!structureFormed) return amount;
            if (voltage > getMaxInputVoltage()) {
                explodeMachine();
                return 0D;
            }
            long accepted = (long) Math.min(amount, maxEnergy - energy);
            energy += accepted;
            if (accepted > 0) markControllerDirtyAndSync();
            return amount - accepted;
        }
    }

    /** Default generator endpoint: exports the controller's long energy buffer at its inherited tier. */
    private final class InheritedEnergyOutput implements IEnergySource {
        @Override public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
            return getEnergyRole().canExport(structureFormed, enabled);
        }
        @Override public double getOfferedEnergy() {
            return getEnergyRole().canExport(structureFormed, enabled)
                    ? Math.min(Math.max(0D, energy - energyOutputDebt), getGatewayMaxOutputVoltage()) : 0D;
        }
        @Override public void drawEnergy(double amount) {
            if (!getEnergyRole().canExport(structureFormed, enabled) || !Double.isFinite(amount) || amount <= 0D) return;
            double debit = Math.min(amount, getOfferedEnergy()) + energyOutputDebt;
            long drawn = (long) debit;
            energy -= drawn;
            energyOutputDebt = debit - drawn;
            markControllerDirtyAndSync();
        }
        @Override public int getSourceTier() { return Math.max(1, machineTier); }
    }
}
