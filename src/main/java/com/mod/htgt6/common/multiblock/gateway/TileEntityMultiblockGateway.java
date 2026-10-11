package com.mod.htgt6.common.multiblock.gateway;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.future.IItemHandlerModifiable;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.FluidGatewayVariant;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGatewayController;
import com.mod.htgt6.common.ui.MachineUiTheme;
import cpw.mods.fml.common.Optional;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

/** Capability proxy that can be linked to any IMultiblockGatewayController. */
@Optional.InterfaceList({
        @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
        @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2")
})
public class TileEntityMultiblockGateway extends TileEntity
        implements ISidedInventory, IFluidHandler, IEnergySink, IEnergySource,
        IGuiHolder, IMultiblockGateway {
    private static final int[] LOCAL_ITEM_SLOTS = createLocalSlots();
    private static final int[] NO_SLOTS = new int[0];
    private int controllerX;
    private int controllerY;
    private int controllerZ;
    private boolean linked;
    private boolean addedToEnergyNet;
    private int energyNetTier = -1;
    private long energyNetVoltage = -1;
    private IMultiblockGatewayController energyNetController;
    private boolean autoOutput;
    private int autoOutputTicker;
    private int gatewayMetadata = -1;
    private int visualSide = 1;
    /** Virtual GT6 Selector Tag used by configurable fluid-input machines. */
    private int selectorTag = 1;
    private final IItemHandlerModifiable uiInventory = new GatewayItemHandler(this);
    private final ItemStack[] clientItemCache = new ItemStack[32];
    private final String[] syncedFluidNames = { "", "", "", "", "", "", "", "" };
    private final int[] syncedFluidAmounts = new int[8];
    private final int[] syncedFluidCapacities = new int[8];
    private boolean syncedConnected;
    private transient IMultiblockGatewayController cachedController;
    private transient IInventory cachedInventory;
    private transient int[] cachedMappedSlots = NO_SLOTS;
    private transient long capabilityCacheTick = Long.MIN_VALUE;

    public TileEntityMultiblockGateway() { }

    public TileEntityMultiblockGateway(int metadata) {
        gatewayMetadata = Math.max(0, Math.min(15, metadata));
    }

    public GatewayType getGatewayType() {
        if (gatewayMetadata < 0 && worldObj != null) {
            gatewayMetadata = Math.max(0, Math.min(15, worldObj.getBlockMetadata(xCoord, yCoord, zCoord)));
        }
        return FluidGatewayVariant.type(Math.max(0, gatewayMetadata));
    }

    @Override public int getFluidTankCount() {
        getGatewayType(); // Resolve metadata before constructing matching client/server UI handlers.
        return FluidGatewayVariant.tankCount(Math.max(0, gatewayMetadata));
    }

    public boolean link(TileEntity controller) {
        if (!(controller instanceof IMultiblockGatewayController)) return false;
        if (linked && controllerX == controller.xCoord && controllerY == controller.yCoord
                && controllerZ == controller.zCoord) return true;
        controllerX = controller.xCoord;
        controllerY = controller.yCoord;
        controllerZ = controller.zCoord;
        linked = true;
        clearCapabilityCache();
        markGatewayDirtyAndSync();
        return true;
    }

    public void unlink() {
        if (!linked) return;
        linked = false;
        clearCapabilityCache();
        markGatewayDirtyAndSync();
    }
    public boolean isLinked() { return linked; }

    public int getInheritedTier() {
        IMultiblockGatewayController controller = getController();
        return controller == null ? 1 : Math.max(1, controller.getGatewayTier());
    }

    public void setVisualSide(int side) {
        visualSide = Math.max(0, Math.min(5, side));
        markGatewayDirtyAndSync();
    }
    public int getVisualSide() { return visualSide; }
    public int getSelectorTag() { return Math.max(1, Math.min(24, selectorTag)); }
    public void setSelectorTag(int value) {
        selectorTag = Math.max(1, Math.min(24, value));
        markGatewayDirtyAndSync();
    }
    public int getFunctionalSide() {
        IMultiblockGatewayController controller = getController();
        if (controller instanceof TileEntity) {
            TileEntity tile = (TileEntity) controller;
            int facing = worldObj.getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord);
            if (facing >= 2 && facing <= 5) return facing;
        }
        return visualSide;
    }
    public Block getInheritedCasingBlock() {
        IMultiblockGatewayController controller = getController();
        return controller == null ? null : controller.getGatewayCasingBlock();
    }
    public int getInheritedCasingMetadata() {
        IMultiblockGatewayController controller = getController();
        return controller == null ? 0 : controller.getGatewayCasingMetadata();
    }

    public long getInheritedMaxInputVoltage() {
        IMultiblockGatewayController controller = getController();
        return controller == null ? 0L : Math.max(0L, controller.getGatewayMaxInputVoltage());
    }

    public long getInheritedMaxOutputVoltage() {
        IMultiblockGatewayController controller = getController();
        return controller == null ? 0L : Math.max(0L, controller.getGatewayMaxOutputVoltage());
    }

    public IMultiblockGatewayController getController() {
        if (!linked || worldObj == null) return null;
        long tick = worldObj.getTotalWorldTime();
        if (capabilityCacheTick == tick) {
            return cachedController != null && cachedController.isGatewayControllerFormed()
                    ? cachedController : null;
        }
        capabilityCacheTick = tick;
        cachedController = null;
        cachedInventory = null;
        cachedMappedSlots = NO_SLOTS;
        TileEntity tile = worldObj.getTileEntity(controllerX, controllerY, controllerZ);
        if (!(tile instanceof IMultiblockGatewayController)) {
            // A capability lookup can occur while the controller chunk or its
            // client TileEntity is not available. Only structure validation owns
            // the persisted link state; a transient lookup must not destroy it.
            return null;
        }
        IMultiblockGatewayController controller = (IMultiblockGatewayController) tile;
        if (!controller.isGatewayControllerFormed()) return null;
        cachedController = controller;
        return controller;
    }

    private IInventory inventory() {
        IMultiblockGatewayController controller = getController();
        if (controller == null) return null;
        if (cachedInventory == null) cachedInventory = controller.getGatewayItemInventory(getGatewayType());
        return cachedInventory;
    }

    private int[] slots() {
        IMultiblockGatewayController controller = getController();
        if (controller == null) return NO_SLOTS;
        if (cachedMappedSlots == NO_SLOTS) {
            int[] slots = controller.getGatewayItemSlots(getGatewayType());
            cachedMappedSlots = slots == null ? NO_SLOTS : slots;
        }
        return cachedMappedSlots;
    }

    private int mapSlot(int localSlot) {
        int[] slots = slots();
        return localSlot >= 0 && localSlot < slots.length ? slots[localSlot] : -1;
    }

    private IFluidHandler fluid() {
        IMultiblockGatewayController controller = getController();
        return controller == null ? null : controller.getGatewayFluidHandler(
                getGatewayType(), xCoord, yCoord, zCoord);
    }

    public IFluidHandler getFluidTankHandler(int tank) {
        IMultiblockGatewayController controller = getController();
        return controller == null || tank < 0 || tank >= getFluidTankCount() ? null
                : controller.getGatewayFluidTankHandler(getGatewayType(), xCoord, yCoord, zCoord, tank);
    }

    private IEnergySink energy() {
        IMultiblockGatewayController controller = getController();
        return controller == null ? null : controller.getGatewayEnergyHandler(getGatewayType());
    }

    private IEnergySource energyOutput() {
        IMultiblockGatewayController controller = getController();
        return controller == null ? null
                : controller.getGatewayEnergyOutputHandler(getGatewayType());
    }

    @Override public int getSizeInventory() {
        GatewayType type = getGatewayType();
        return type == GatewayType.ITEM_INPUT || type == GatewayType.ITEM_OUTPUT ? 32 : 0;
    }
    @Override public ItemStack getStackInSlot(int slot) {
        IInventory inv = inventory(); int mapped = mapSlot(slot);
        return inv == null || mapped < 0 ? null : inv.getStackInSlot(mapped);
    }
    @Override public ItemStack decrStackSize(int slot, int amount) {
        IInventory inv = inventory(); int mapped = mapSlot(slot);
        return inv == null || mapped < 0 ? null : inv.decrStackSize(mapped, amount);
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) {
        IInventory inv = inventory(); int mapped = mapSlot(slot);
        return inv == null || mapped < 0 ? null : inv.getStackInSlotOnClosing(mapped);
    }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        IInventory inv = inventory(); int mapped = mapSlot(slot);
        if (inv != null && mapped >= 0) inv.setInventorySlotContents(mapped, stack);
    }
    @Override public String getInventoryName() { return "HTGT6 Multiblock Gateway"; }
    @Override public boolean hasCustomInventoryName() { return true; }
    @Override public int getInventoryStackLimit() { IInventory inv = inventory(); return inv == null ? 64 : inv.getInventoryStackLimit(); }
    @Override public boolean isUseableByPlayer(EntityPlayer player) { return getController() != null; }
    @Override public void openInventory() { IInventory inv = inventory(); if (inv != null) inv.openInventory(); }
    @Override public void closeInventory() { IInventory inv = inventory(); if (inv != null) inv.closeInventory(); }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (worldObj != null && worldObj.isRemote) {
            return getGatewayType() == GatewayType.ITEM_INPUT && slot >= 0 && slot < 32;
        }
        IInventory inv = inventory(); int mapped = mapSlot(slot);
        return getGatewayType() == GatewayType.ITEM_INPUT && inv != null && mapped >= 0 && inv.isItemValidForSlot(mapped, stack);
    }
    @Override public int[] getAccessibleSlotsFromSide(int side) {
        return getSizeInventory() == 32 ? LOCAL_ITEM_SLOTS : NO_SLOTS;
    }
    @Override public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return getGatewayType() == GatewayType.ITEM_INPUT && isItemValidForSlot(slot, stack);
    }
    @Override public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return getGatewayType() == GatewayType.ITEM_OUTPUT && mapSlot(slot) >= 0;
    }

    @Override public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        IFluidHandler handler = fluid();
        if (getGatewayType() != GatewayType.FLUID_INPUT || handler == null || resource == null) return 0;
        int remaining = getRemainingTierCapacity(handler, from);
        if (remaining <= 0) return 0;
        FluidStack limited = resource.copy();
        limited.amount = Math.min(limited.amount, remaining);
        return handler.fill(from, limited, doFill);
    }
    @Override public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        IFluidHandler handler = fluid();
        return getGatewayType() == GatewayType.FLUID_OUTPUT && handler != null ? handler.drain(from, resource, doDrain) : null;
    }
    @Override public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        IFluidHandler handler = fluid();
        return getGatewayType() == GatewayType.FLUID_OUTPUT && handler != null ? handler.drain(from, maxDrain, doDrain) : null;
    }
    @Override public boolean canFill(ForgeDirection from, Fluid fluid) {
        IFluidHandler handler = fluid();
        return getGatewayType() == GatewayType.FLUID_INPUT && handler != null && handler.canFill(from, fluid);
    }
    @Override public boolean canDrain(ForgeDirection from, Fluid fluid) {
        IFluidHandler handler = fluid();
        return getGatewayType() == GatewayType.FLUID_OUTPUT && handler != null && handler.canDrain(from, fluid);
    }
    @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        IFluidHandler handler = fluid();
        if (handler == null) return new FluidTankInfo[0];
        FluidTankInfo[] source = handler.getTankInfo(from);
        if (source == null || source.length == 0) return new FluidTankInfo[0];
        int capacity = getTierFluidCapacity();
        FluidTankInfo[] limited = new FluidTankInfo[source.length];
        for (int i = 0; i < source.length; i++) {
            FluidTankInfo info = source[i];
            limited[i] = info == null ? null : new FluidTankInfo(
                    info.fluid == null ? null : info.fluid.copy(), capacity);
        }
        return limited;
    }

    private int getRemainingTierCapacity(IFluidHandler handler, ForgeDirection from) {
        long stored = 0L;
        long capacity = 0L;
        FluidTankInfo[] tanks = handler.getTankInfo(from);
        if (tanks != null) for (FluidTankInfo tank : tanks) {
            if (tank == null) continue;
            capacity += Math.max(0, tank.capacity);
            if (tank.fluid != null) stored += Math.max(0, tank.fluid.amount);
        }
        // Multi-gateway controllers expose one tank per installed hatch. A
        // single-tank machine still reports exactly its inherited tier capacity.
        if (capacity <= 0L) capacity = getTierFluidCapacity();
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, capacity - stored));
    }

    private int getTierFluidCapacity() {
        IMultiblockGatewayController controller = getController();
        long capacity = controller == null ? 0L : controller.getGatewayFluidCapacity(getGatewayType());
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, capacity));
    }

    @Override public void validate() {
        super.validate();
    }
    @Override public void updateEntity() {
        // Metadata/world lookup is deliberately deferred until the first tick.
        // validate() runs while AnvilChunkLoader is still attaching TEs and a
        // world metadata lookup there recursively loads the same chunk.
        GatewayType type = getGatewayType();
        if (worldObj != null && !worldObj.isRemote
                && (type == GatewayType.ENERGY_INPUT || type == GatewayType.ENERGY_OUTPUT)) {
            IMultiblockGatewayController controller = getController();
            boolean available = type == GatewayType.ENERGY_INPUT ? energy() != null : energyOutput() != null;
            int tier = type == GatewayType.ENERGY_INPUT ? getSinkTier() : getSourceTier();
            long voltage = type == GatewayType.ENERGY_INPUT ? getInheritedMaxInputVoltage() : getInheritedMaxOutputVoltage();
            if (addedToEnergyNet && (!available || energyNetController != controller
                    || energyNetTier != tier || energyNetVoltage != voltage)) unloadEnergyNet();
            if (available && !addedToEnergyNet) {
                energyNetController = controller;
                energyNetTier = tier;
                energyNetVoltage = voltage;
                loadEnergyNet();
            }
        }
        if (worldObj != null && !worldObj.isRemote && autoOutput && ++autoOutputTicker >= 10) {
            autoOutputTicker = 0;
            exportToAdjacentStorage();
        }
    }
    private void loadEnergyNet() {
        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this)); addedToEnergyNet = true;
    }
    private void unloadEnergyNet() {
        if (worldObj != null && !worldObj.isRemote && addedToEnergyNet)
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        addedToEnergyNet = false;
        energyNetController = null;
        energyNetTier = -1;
        energyNetVoltage = -1;
    }
    @Override public void onChunkUnload() {
        unloadEnergyNet(); clearCapabilityCache(); super.onChunkUnload();
    }
    @Override public void invalidate() {
        unloadEnergyNet(); clearCapabilityCache(); super.invalidate();
    }
    @Override public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return getGatewayType() == GatewayType.ENERGY_INPUT && energy() != null;
    }
    @Override public double getDemandedEnergy() { IEnergySink sink = energy(); return sink == null ? 0 : sink.getDemandedEnergy(); }
    @Override public int getSinkTier() { IEnergySink sink = energy(); return sink == null ? 1 : sink.getSinkTier(); }
    @Override public double injectEnergy(ForgeDirection direction, double amount, double voltage) {
        IEnergySink sink = energy(); return sink == null ? amount : sink.injectEnergy(direction, amount, voltage);
    }

    @Override public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
        IEnergySource source = energyOutput();
        return getGatewayType() == GatewayType.ENERGY_OUTPUT && source != null
                && source.emitsEnergyTo(receiver, direction);
    }
    @Override public double getOfferedEnergy() {
        IEnergySource source = energyOutput();
        return getGatewayType() == GatewayType.ENERGY_OUTPUT && source != null
                ? source.getOfferedEnergy() : 0D;
    }
    @Override public void drawEnergy(double amount) {
        IEnergySource source = energyOutput();
        if (getGatewayType() == GatewayType.ENERGY_OUTPUT && source != null) source.drawEnergy(amount);
    }
    @Override public int getSourceTier() {
        IEnergySource source = energyOutput();
        return getGatewayType() == GatewayType.ENERGY_OUTPUT && source != null
                ? source.getSourceTier() : getInheritedTier();
    }

    private void exportToAdjacentStorage() {
        GatewayType type = getGatewayType();
        if (type != GatewayType.ITEM_OUTPUT && type != GatewayType.FLUID_OUTPUT) return;
        // Nearest-first bounded search. Four blocks is enough to bridge the
        // casing around a hatch while keeping automation cost predictable.
        for (int distance = 1; distance <= 4; distance++) {
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    for (int dz = -distance; dz <= distance; dz++) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != distance) continue;
                        int x = xCoord + dx, y = yCoord + dy, z = zCoord + dz;
                        if (y < 0 || y >= worldObj.getHeight() || !worldObj.blockExists(x, y, z)) continue;
                        TileEntity target = worldObj.getTileEntity(x, y, z);
                        if (target == null || target instanceof TileEntityMultiblockGateway
                                || x == controllerX && y == controllerY && z == controllerZ) continue;
                        ForgeDirection targetSide = directionTowardGateway(dx, dy, dz);
                        if (type == GatewayType.ITEM_OUTPUT && target instanceof IInventory
                                && exportItems((IInventory) target, targetSide.ordinal())) return;
                        if (type == GatewayType.FLUID_OUTPUT && target instanceof IFluidHandler
                                && exportFluid((IFluidHandler) target, targetSide)) return;
                    }
                }
            }
        }
    }

    private boolean exportItems(IInventory destination, int destinationSide) {
        boolean changed = false;
        for (int sourceSlot = 0; sourceSlot < getSizeInventory(); sourceSlot++) {
            ItemStack source = getStackInSlot(sourceSlot);
            if (source == null || source.stackSize <= 0) continue;
            ItemStack remainder = source.copy();
            int before = remainder.stackSize;
            insertIntoInventory(destination, destinationSide, remainder);
            int moved = before - remainder.stackSize;
            if (moved > 0) {
                decrStackSize(sourceSlot, moved);
                changed = true;
            }
        }
        if (changed) {
            destination.markDirty();
            markDirty();
        }
        return changed;
    }

    private static void insertIntoInventory(IInventory inventory, int side, ItemStack stack) {
        int[] slots;
        if (inventory instanceof ISidedInventory) {
            slots = ((ISidedInventory) inventory).getAccessibleSlotsFromSide(side);
        } else {
            slots = new int[inventory.getSizeInventory()];
            for (int i = 0; i < slots.length; i++) slots[i] = i;
        }
        for (int slot : slots) {
            if (stack.stackSize <= 0 || slot < 0 || slot >= inventory.getSizeInventory()) break;
            ItemStack present = inventory.getStackInSlot(slot);
            boolean accepts = inventory.isItemValidForSlot(slot, stack)
                    && (!(inventory instanceof ISidedInventory)
                    || ((ISidedInventory) inventory).canInsertItem(slot, stack, side));
            if (!accepts || present == null || !present.isItemEqual(stack)
                    || !ItemStack.areItemStackTagsEqual(present, stack)) continue;
            int limit = Math.min(inventory.getInventoryStackLimit(), present.getMaxStackSize());
            int moved = Math.min(stack.stackSize, Math.max(0, limit - present.stackSize));
            if (moved > 0) {
                present.stackSize += moved;
                stack.stackSize -= moved;
            }
        }
        for (int slot : slots) {
            if (stack.stackSize <= 0 || slot < 0 || slot >= inventory.getSizeInventory()) break;
            boolean accepts = inventory.isItemValidForSlot(slot, stack)
                    && (!(inventory instanceof ISidedInventory)
                    || ((ISidedInventory) inventory).canInsertItem(slot, stack, side));
            if (!accepts || inventory.getStackInSlot(slot) != null) continue;
            int moved = Math.min(stack.stackSize,
                    Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize()));
            ItemStack inserted = stack.copy();
            inserted.stackSize = moved;
            inventory.setInventorySlotContents(slot, inserted);
            stack.stackSize -= moved;
        }
    }

    private boolean exportFluid(IFluidHandler destination, ForgeDirection direction) {
        FluidStack offered = drain(direction, 4000, false);
        if (offered == null || offered.amount <= 0) return false;
        int accepted = destination.fill(direction, offered, false);
        if (accepted <= 0) return false;
        FluidStack drained = drain(direction, Math.min(accepted, offered.amount), true);
        if (drained == null || drained.amount <= 0) return false;
        return destination.fill(direction, drained, true) > 0;
    }

    private static ForgeDirection directionTowardGateway(int dx, int dy, int dz) {
        // For non-adjacent targets there is no physical pipe face, so use the
        // dominant axis as the target's logical insertion side.
        int ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
        if (ay >= ax && ay >= az) return dy > 0 ? ForgeDirection.DOWN : ForgeDirection.UP;
        if (ax >= az) return dx > 0 ? ForgeDirection.WEST : ForgeDirection.EAST;
        return dz > 0 ? ForgeDirection.NORTH : ForgeDirection.SOUTH;
    }

    public FluidStack getDisplayedFluid() { return getDisplayedFluid(0); }
    public FluidStack getDisplayedFluid(int tankIndex) {
        if (worldObj != null && worldObj.isRemote) {
            if (tankIndex < 0 || tankIndex >= syncedFluidNames.length) return null;
            Fluid fluid = FluidRegistry.getFluid(syncedFluidNames[tankIndex]);
            return fluid == null || syncedFluidAmounts[tankIndex] <= 0
                    ? null : new FluidStack(fluid, syncedFluidAmounts[tankIndex]);
        }
        FluidTankInfo[] info = getTankInfo(ForgeDirection.UNKNOWN);
        return tankIndex < 0 || tankIndex >= info.length || info[tankIndex] == null
                || info[tankIndex].fluid == null ? null : info[tankIndex].fluid.copy();
    }

    public int getDisplayedFluidCapacity() { return getDisplayedFluidCapacity(0); }
    public int getDisplayedFluidCapacity(int tankIndex) {
        if (worldObj != null && worldObj.isRemote) return tankIndex < 0 || tankIndex >= syncedFluidCapacities.length
                ? 0 : syncedFluidCapacities[tankIndex];
        FluidTankInfo[] info = getTankInfo(ForgeDirection.UNKNOWN);
        return tankIndex < 0 || tankIndex >= info.length || info[tankIndex] == null ? 0 : info[tankIndex].capacity;
    }

    private String getServerFluidName(int tank) {
        FluidStack fluid = getDisplayedFluid(tank);
        return fluid == null || fluid.getFluid() == null ? "" : fluid.getFluid().getName();
    }

    private int getServerFluidAmount(int tank) {
        FluidStack fluid = getDisplayedFluid(tank);
        return fluid == null ? 0 : fluid.amount;
    }

    @Override public ModularScreen createScreen(GuiCreationContext context, ModularPanel panel) {
        return new ModularScreen(panel);
    }

    @Override
    public ModularPanel buildUI(GuiCreationContext context, GuiSyncManager sync, boolean isClient) {
        sync.syncValue("gateway_linked", SyncHandlers.bool(this::isServerConnected,
                value -> syncedConnected = value));
        sync.syncValue("gateway_auto_output", SyncHandlers.bool(() -> autoOutput, value -> autoOutput = value));
        sync.syncValue("gateway_selector", SyncHandlers.intNumber(
                this::getSelectorTag, value -> selectorTag = Math.max(1, Math.min(24, value))));
        for (int tank = 0; tank < getFluidTankCount(); tank++) {
            final int tankIndex = tank;
            sync.syncValue("gateway_fluid_name_" + tank, SyncHandlers.string(
                    () -> getServerFluidName(tankIndex), value -> syncedFluidNames[tankIndex] = value == null ? "" : value));
            sync.syncValue("gateway_fluid_amount_" + tank, SyncHandlers.intNumber(
                    () -> getServerFluidAmount(tankIndex), value -> syncedFluidAmounts[tankIndex] = Math.max(0, value)));
            sync.syncValue("gateway_fluid_capacity_" + tank, SyncHandlers.intNumber(
                    () -> getDisplayedFluidCapacity(tankIndex), value -> syncedFluidCapacities[tankIndex] = Math.max(0, value)));
        }
        sync.syncValue("gateway_auto_action",
                new InteractionSyncHandler().setOnMouseTapped(data -> toggleAutoOutput()));
        sync.syncValue("gateway_selector_action",
                new InteractionSyncHandler().setOnMouseTapped(data -> cycleSelectorTag()));
        GatewayType type = getGatewayType();
        return type == GatewayType.ITEM_INPUT || type == GatewayType.ITEM_OUTPUT
                ? buildItemUI(sync, type) : buildFluidUI(sync, type, isClient);
    }

    private ModularPanel buildItemUI(GuiSyncManager sync, GatewayType type) {
        sync.registerSlotGroup("gateway_items", 32);
        MachineUiTheme theme = MachineUiTheme.getActive();
        ModularPanel panel = ModularPanel.defaultPanel("multiblock_item_gateway", 198, 276)
                .background(new Rectangle().setColor(theme.getPanelColor()));
        addFrame(panel, theme.getFrameColor(), 198, 276);
        panel.child(new TextWidget(IKey.dynamic(this::getLocalizedGatewayName)).pos(10, 8).color(theme.getTitleColor()));
        panel.child(new TextWidget(IKey.dynamic(this::getConnectionStatus)).pos(10, 22).color(theme.getPrimaryColor()));
        boolean input = type == GatewayType.ITEM_INPUT;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 8; column++) {
                int slotIndex = row * 8 + column;
                ModularSlot slot = SyncHandlers.itemSlot(uiInventory, slotIndex)
                        .slotGroup("gateway_items").accessibility(true, true);
                panel.child(new ItemSlot().slot(slot).background(GuiTextures.SLOT_DARK)
                        .pos(27 + column * 18, 42 + row * 18));
            }
        }
        if (!input) panel.child(createAutoOutputButton().pos(158, 7));
        panel.child(new TextWidget(IKey.lang("ui.htgt6.mining_unit.player_inventory"))
                .pos(18, 126).color(theme.getLabelColor()));
        panel.child(SlotGroupWidget.playerInventory().pos(18, 142));
        return panel;
    }

    private ModularPanel buildFluidUI(GuiSyncManager sync, GatewayType type, boolean isClient) {
        MachineUiTheme theme = MachineUiTheme.getActive();
        ModularPanel panel = ModularPanel.defaultPanel("multiblock_fluid_gateway", 198, 244)
                .background(new Rectangle().setColor(theme.getPanelColor()));
        addFrame(panel, theme.getFrameColor(), 198, 244);
        panel.child(new TextWidget(IKey.dynamic(this::getLocalizedGatewayName)).pos(10, 8).color(theme.getTitleColor()));
        panel.child(new TextWidget(IKey.dynamic(this::getConnectionStatus)).pos(10, 22).color(theme.getPrimaryColor()));
        int tankCount = getFluidTankCount();
        int tankWidth = tankCount == 1 ? 54 : tankCount == 4 ? 32 : 18;
        int gap = 2;
        int startX = (198 - (tankCount * tankWidth + (tankCount - 1) * gap)) / 2;
        for (int tankIndex = 0; tankIndex < tankCount; tankIndex++) {
            Widget tank;
            if (isClient) {
                tank = new com.mod.htgt6.client.GUI.GatewayFluidWidget(this, tankIndex);
            } else {
                tank = new Widget().background(GuiTextures.SLOT_DARK);
            }
            tank.background(GuiTextures.SLOT_DARK);
            tank.pos(startX + tankIndex * (tankWidth + gap), 42).size(tankWidth, 62);
            panel.child(tank);
        }
        panel.child(new TextWidget(IKey.dynamic(this::getFluidAmountText)).pos(20, 109).size(158, 11)
                .color(theme.getProductionColor()));
        if (type == GatewayType.FLUID_INPUT) panel.child(createSelectorButton().pos(148, 7));
        if (type == GatewayType.FLUID_OUTPUT) panel.child(createAutoOutputButton().pos(158, 7));
        panel.child(new TextWidget(IKey.lang("ui.htgt6.mining_unit.player_inventory"))
                .pos(18, 130).color(theme.getLabelColor()));
        panel.child(SlotGroupWidget.playerInventory().pos(18, 146));
        return panel;
    }

    private ButtonWidget<?> createAutoOutputButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(30, 22).background(GuiTextures.BUTTON)
                .overlay(IKey.dynamic(() -> (autoOutput ? EnumChatFormatting.GREEN + "ON"
                        : EnumChatFormatting.RED + "OFF")));
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(StatCollector.translateToLocal(autoOutput
                    ? "ui.htgt6.gateway.auto_output_on" : "ui.htgt6.gateway.auto_output_off"));
        });
        // Update the visible state immediately, then return false so MUI still
        // forwards the same click to the authoritative server handler.
        button.onMouseTapped(mouseButton -> {
            if (worldObj != null && worldObj.isRemote) autoOutput = !autoOutput;
            return false;
        });
        button.syncHandler("gateway_auto_action");
        return button;
    }

    private ButtonWidget<?> createSelectorButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(40, 22).background(GuiTextures.BUTTON)
                .overlay(IKey.dynamic(() -> "S:" + getSelectorTag()));
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(StatCollector.translateToLocalFormatted(
                    "ui.htgt6.gateway.selector", getSelectorTag()));
            t.addLine(StatCollector.translateToLocal("ui.htgt6.gateway.selector_hint"));
        });
        button.onMouseTapped(mouseButton -> {
            if (worldObj != null && worldObj.isRemote) selectorTag = selectorTag >= 24 ? 1 : selectorTag + 1;
            return false;
        });
        button.syncHandler("gateway_selector_action");
        return button;
    }

    private void cycleSelectorTag() {
        setSelectorTag(getSelectorTag() >= 24 ? 1 : getSelectorTag() + 1);
    }

    private void toggleAutoOutput() {
        autoOutput = !autoOutput;
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private String getLocalizedGatewayName() {
        return StatCollector.translateToLocal("tile.mining_unit_port."
                + FluidGatewayVariant.localizationSuffix(Math.max(0, gatewayMetadata)) + ".name");
    }

    private String getConnectionStatus() {
        boolean connected = worldObj != null && worldObj.isRemote ? syncedConnected : isServerConnected();
        return StatCollector.translateToLocal(connected
                ? "ui.htgt6.gateway.connected" : "ui.htgt6.gateway.disconnected");
    }

    private boolean isServerConnected() {
        return linked && getController() != null;
    }

    private String getFluidAmountText() {
        long amount = 0L, capacity = 0L;
        int used = 0;
        for (int tank = 0; tank < getFluidTankCount(); tank++) {
            FluidStack fluid = getDisplayedFluid(tank);
            if (fluid != null) { amount += fluid.amount; used++; }
            capacity += getDisplayedFluidCapacity(tank);
        }
        return used + "/" + getFluidTankCount() + "  " + amount + " / " + capacity + " L";
    }

    private static void addFrame(ModularPanel panel, int color, int width, int height) {
        frame(panel, color, 0, 0, width, 2);
        frame(panel, color, 0, height - 2, width, 2);
        frame(panel, color, 0, 0, 2, height);
        frame(panel, color, width - 2, 0, 2, height);
    }

    private static void frame(ModularPanel panel, int color, int x, int y, int width, int height) {
        Widget edge = new Widget().background(new Rectangle().setColor(color));
        edge.pos(x, y).size(width, height);
        panel.child(edge);
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        clearCapabilityCache();
        super.readFromNBT(nbt); linked = nbt.getBoolean("Linked");
        controllerX = nbt.getInteger("ControllerX"); controllerY = nbt.getInteger("ControllerY"); controllerZ = nbt.getInteger("ControllerZ");
        gatewayMetadata = nbt.hasKey("GatewayMetadata") ? nbt.getInteger("GatewayMetadata") : -1;
        visualSide = nbt.hasKey("VisualSide") ? Math.max(0, Math.min(5, nbt.getInteger("VisualSide"))) : 1;
        autoOutput = nbt.getBoolean("AutoOutput");
        selectorTag = nbt.hasKey("SelectorTag")
                ? Math.max(1, Math.min(24, nbt.getInteger("SelectorTag"))) : 1;
    }
    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt); nbt.setBoolean("Linked", linked);
        nbt.setInteger("ControllerX", controllerX); nbt.setInteger("ControllerY", controllerY); nbt.setInteger("ControllerZ", controllerZ);
        nbt.setBoolean("AutoOutput", autoOutput);
        nbt.setInteger("VisualSide", visualSide);
        nbt.setInteger("SelectorTag", getSelectorTag());
        if (gatewayMetadata >= 0) nbt.setInteger("GatewayMetadata", gatewayMetadata);
    }

    @Override public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }

    private static final class GatewayItemHandler implements IItemHandlerModifiable {
        private final TileEntityMultiblockGateway gateway;
        private GatewayItemHandler(TileEntityMultiblockGateway gateway) { this.gateway = gateway; }
        @Override public int getSlots() { return 32; }
        @Override public ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= 32) return null;
            return gateway.worldObj != null && gateway.worldObj.isRemote
                    ? gateway.clientItemCache[slot] : gateway.getStackInSlot(slot);
        }
        @Override public int getSlotLimit(int slot) { return gateway.getInventoryStackLimit(); }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (slot < 0 || slot >= 32) return;
            if (gateway.worldObj != null && gateway.worldObj.isRemote) {
                gateway.clientItemCache[slot] = stack == null ? null : stack.copy();
            } else {
                gateway.setInventorySlotContents(slot, stack);
            }
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return gateway.isItemValidForSlot(slot, stack); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack == null || !isItemValid(slot, stack)) return stack;
            ItemStack present = getStackInSlot(slot);
            if (present != null && (!present.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(present, stack))) return stack;
            int presentSize = present == null ? 0 : present.stackSize;
            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            int moved = Math.min(stack.stackSize, Math.max(0, limit - presentSize));
            if (moved <= 0) return stack;
            if (!simulate) {
                ItemStack inserted = present == null ? stack.copy() : present.copy();
                inserted.stackSize = presentSize + moved;
                setStackInSlot(slot, inserted);
            }
            if (moved == stack.stackSize) return null;
            ItemStack remainder = stack.copy(); remainder.stackSize -= moved; return remainder;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack present = getStackInSlot(slot);
            if (present == null || amount <= 0) return null;
            ItemStack extracted = present.copy(); extracted.stackSize = Math.min(amount, present.stackSize);
            if (!simulate) {
                // Reads and writes must use the same inventory on both sides. The
                // client predicts clicks against its UI cache, not the world TE.
                ItemStack remainder = present.copy();
                remainder.stackSize -= extracted.stackSize;
                setStackInSlot(slot, remainder.stackSize <= 0 ? null : remainder);
            }
            return extracted;
        }
    }

    private void clearCapabilityCache() {
        capabilityCacheTick = Long.MIN_VALUE;
        cachedController = null;
        cachedInventory = null;
        cachedMappedSlots = NO_SLOTS;
    }

    private void markGatewayDirtyAndSync() {
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private static int[] createLocalSlots() {
        int[] slots = new int[32];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        return slots;
    }
}
