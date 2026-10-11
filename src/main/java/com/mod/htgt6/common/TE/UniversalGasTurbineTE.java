package com.mod.htgt6.common.TE;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import com.mod.htgt6.api.hitech6.fuelconfigs.FluidFuelAccounting;

import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityFluidGenerator;
import com.mod.htgt6.common.ui.MachineUiTheme;
import com.mod.htgt6.common.ui.SingleblockUiText;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.future.IItemHandlerModifiable;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class UniversalGasTurbineTE
        extends TileEntityFluidGenerator
        implements IGuiHolder {

    // ==========================================
    // GAS FUEL REGISTRY
    // ==========================================

    /** Linked order keeps the ModularUI/NEI fuel list deterministic. */
    public static final Map<String, Integer> VALID_GAS_FUELS = new LinkedHashMap<String, Integer>();

    public static void addGasFuel(String fluidName, int euPerMb) {
        if (fluidName == null || euPerMb <= 0) return;
        String normalizedName = fluidName.trim();
        if (!normalizedName.isEmpty()) {
            VALID_GAS_FUELS.put(normalizedName, euPerMb);
        }
    }

    public static void clearGasFuels() {
        VALID_GAS_FUELS.clear();
    }

    // ==========================================
    // INVENTORY
    // ==========================================

    /**
     * ModularUI adapter.  It deliberately writes to the original inventory so
     * automation, NBT and the turbine processing code retain one source of truth.
     */
    private final IItemHandlerModifiable modularInventory = new TurbineItemHandler(this);

    // ==========================================
    // FLUID TANK (128,000 mB)
    // ==========================================

    /**
     * The native FluidSlot works with IFluidTank directly.  This view keeps its
     * convenient canister interaction while applying the same gas whitelist as
     * pipes and the machine's IFluidHandler implementation.
     */
    private final IFluidTank modularGasTank = new ValidatingGasTank(this);

    // ==========================================
    // ENERGY
    // ==========================================

    /** Measured server-side every tick and mirrored to the status terminal. */
    private long currentEnergyProduction;
    private long fuelReserveHundredths;
    private int currentFuelConsumption;
    private int currentFuelValue;
    /** Explicit terminal values make the fuel display independent of UI cache timing. */
    private String syncedFuelName = "EMPTY";
    private int syncedFuelAmount;

    // ==========================================
    // MACHINE
    // ==========================================

    public UniversalGasTurbineTE() {
        super(4, 128000, 1000000000D);
    }

    // ==========================================
    // UPDATE LOGIC
    // ==========================================

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (worldObj == null || worldObj.isRemote) return;
        updateMachineTier();

        processFluidCells();
        currentEnergyProduction = 0;
        currentFuelConsumption = 0;
        if (enabled) {
            generateEnergy();
        }
        refreshTerminalFuel();
    }

    private void processFluidCells() {
        if (inventory[0] == null) return;

        ItemStack inputCell = inventory[0];
        FluidStack fluidInCell = null;
        ItemStack emptyCell = null;
        int amountToDrain = 0;

        // 1. Check if the item implements Forge's IFluidContainerItem (like your Universal Canister)
        if (inputCell.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem containerItem = (IFluidContainerItem) inputCell.getItem();
            fluidInCell = containerItem.getFluid(inputCell);

            if (fluidInCell != null && VALID_GAS_FUELS.containsKey(fluidInCell.getFluid().getName())) {
                // Simulate the real tank fill: free capacity alone is not enough when
                // another gas is already stored in the tank.
                amountToDrain = fluidTank.fill(fluidInCell, false);

                if (amountToDrain > 0) {
                    // Create a simulated single item copy to drain from safely
                    emptyCell = inputCell.copy();
                    emptyCell.stackSize = 1;
                    fluidInCell = containerItem.drain(emptyCell, amountToDrain, true);
                    if (fluidInCell == null || fluidInCell.amount <= 0) return;
                }
            }
        }
        // 2. Fallback to standard FluidContainerRegistry (for standard buckets/cells)
        else if (FluidContainerRegistry.isFilledContainer(inputCell)) {
            fluidInCell = FluidContainerRegistry.getFluidForFilledItem(inputCell);

            if (fluidInCell != null && VALID_GAS_FUELS.containsKey(fluidInCell.getFluid().getName())) {
                // Registry items usually drain fully or not at all
                if (fluidTank.fill(fluidInCell, false) == fluidInCell.amount) {
                    emptyCell = FluidContainerRegistry.drainFluidContainer(inputCell);
                }
            }
        }

        // 3. Process the output if a valid fluid transaction was calculated
        if (fluidInCell != null && emptyCell != null) {
            // Validate output slot availability
            if (inventory[1] == null || (inventory[1].isItemEqual(emptyCell) && ItemStack.areItemStackTagsEqual(inventory[1], emptyCell) && inventory[1].stackSize < inventory[1].getMaxStackSize())) {

                // Fill internal tank
                fluidTank.fill(fluidInCell, true);

                // Consume 1 item from input slot
                inventory[0].stackSize--;
                if (inventory[0].stackSize <= 0) {
                    inventory[0] = null;
                }

                // Add to output slot
                if (inventory[1] == null) {
                    inventory[1] = emptyCell.copy();
                } else {
                    inventory[1].stackSize++;
                }

                markDirty();
            }
        }
    }

    private void generateEnergy() {
        FluidStack fuel = fluidTank.getFluid();
        Integer value = fuel == null ? null : VALID_GAS_FUELS.get(fuel.getFluid().getName());
        FluidFuelAccounting.Step step = FluidFuelAccounting.plan(fuelReserveHundredths,
                fuel == null ? 0 : fuel.amount, value == null ? 0 : value,
                100, getOutputVoltage(), (long) Math.floor(maxEnergy - energy));
        if (step.consumedMb > 0) {
            currentFuelValue = value;
            fluidTank.drain(step.consumedMb, true);
        }
        fuelReserveHundredths = step.reserveHundredths;
        currentFuelConsumption = step.consumedMb;
        currentEnergyProduction = (long) addGeneratedEnergy(step.generatedEu);
        if (step.consumedMb > 0 || step.generatedEu > 0) markDirty();
    }

    @Override
    public void readFromNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        fuelReserveHundredths = FluidFuelAccounting.sanitize(nbt.getLong("FuelReserveHundredths"));
        currentFuelValue = Math.max(0, nbt.getInteger("ReservedFuelValue"));
    }

    @Override
    public void writeToNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("FuelReserveHundredths", fuelReserveHundredths);
        nbt.setInteger("ReservedFuelValue", currentFuelValue);
    }

    private void refreshTerminalFuel() {
        FluidStack fuel = fluidTank.getFluid();
        syncedFuelName = fuel == null ? "EMPTY" : fuel.getLocalizedName();
        syncedFuelAmount = fluidTank.getFluidAmount();
        Integer value = fuel == null ? null : VALID_GAS_FUELS.get(fuel.getFluid().getName());
        if (value != null) currentFuelValue = value;
        else if (fuelReserveHundredths == 0) currentFuelValue = 0;
    }
    private boolean isEnergyStorageNearby() {
        if (this.worldObj == null) return false;

        // Loop through North, South, East, West, Up, and Down
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity adjacentTE = this.worldObj.getTileEntity(
                    this.xCoord + dir.offsetX,
                    this.yCoord + dir.offsetY,
                    this.zCoord + dir.offsetZ
            );

            // Check if the neighbor is an IC2 Energy Storage block (BatBox, MFE, MFSU)
            if (adjacentTE instanceof ic2.api.tile.IEnergyStorage) {
                return true;
            }
        }
        return false;
    }

    // ==========================================
    // IC2 ENERGY SOURCE INTERFACE
    // ==========================================

    @Override
    public double getOfferedEnergy() {
        // If an energy storage block is directly touching this turbine, stop emitting energy
        if (isEnergyStorageNearby()) {
            return 0;
        }
        return Math.min(energy, getOutputVoltage());
    }

    // ==========================================
    // MACHINE TIER
    // ==========================================

    private void updateMachineTier() {
        ItemStack transformer = inventory[3];
        ItemStack module = inventory[2];
        machineTier = MachineTierSystem.resolveMachineTier(module, transformer);
    }

    @Override
    protected boolean isFluidValid(FluidStack resource) {
        return resource != null && resource.getFluid() != null
                && VALID_GAS_FUELS.containsKey(resource.getFluid().getName());
    }

    // ==========================================
    // MODULARUI 2
    // ==========================================

    @Override
    public ModularScreen createScreen(GuiCreationContext context, ModularPanel panel) {
        return new ModularScreen(panel);
    }

    @Override
    public ModularPanel buildUI(GuiCreationContext context, GuiSyncManager syncManager, boolean isClient) {
        com.mod.htgt6.common.network.MachineUiTransfers.bind(syncManager, this, isClient);
        syncManager.syncValue("enabled", SyncHandlers.bool(() -> enabled, value -> enabled = value));
        syncManager.registerSlotGroup("turbine", 4);

        FluidSlotSyncHandler gasTank = SyncHandlers.fluidSlot(modularGasTank)
                .canFillSlot(true)
                .canDrainSlot(true);
        syncManager.syncValue("gas_tank", gasTank);
        syncManager.syncValue("energy", SyncHandlers.longNumber(
                () -> (long) energy,
                value -> energy = value));
        syncManager.syncValue("tier", SyncHandlers.intNumber(
                () -> machineTier,
                value -> machineTier = value));
        syncManager.syncValue("current_production", SyncHandlers.longNumber(
                () -> currentEnergyProduction,
                value -> currentEnergyProduction = value));
        syncManager.syncValue("current_consumption", SyncHandlers.intNumber(
                () -> currentFuelConsumption,
                value -> currentFuelConsumption = value));
        syncManager.syncValue("fuel_value", SyncHandlers.intNumber(
                () -> currentFuelValue,
                value -> currentFuelValue = value));
        syncManager.syncValue("fuel_reserve", SyncHandlers.longNumber(
                () -> fuelReserveHundredths,
                value -> fuelReserveHundredths = value));
        syncManager.syncValue("fuel_name", SyncHandlers.string(
                () -> syncedFuelName,
                value -> syncedFuelName = value));
        syncManager.syncValue("fuel_amount", SyncHandlers.intNumber(
                () -> syncedFuelAmount,
                value -> syncedFuelAmount = value));

        ModularPanel panel = ModularPanel.defaultPanel("universal_gas_turbine", 240, 267);
        MachineUiTheme theme = MachineUiTheme.getActive();
        panel.background(new Rectangle().setColor(theme.getPanelColor()));
        addPanelFrame(panel);

        // GT5-style status terminal: concise operational information, with
        // detailed diagnostics deliberately moved back behind the [i] button.
        Widget terminal = new Widget();
        terminal.background(new Rectangle().setColor(theme.getTerminalColor()));
        terminal.pos(8, 24);
        terminal.size(224, 104);
        panel.child(terminal);
        panel.child(new TextWidget("UNIVERSAL GAS TURBINE").pos(13, 8).color(theme.getTitleColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> SingleblockUiText.label("FUEL: " + getGasDisplayName())))
                .pos(14, 31).size(214, 11).color(theme.getPrimaryColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "TANK: " + getDisplayedFuelAmount() + " / " + fluidTank.getCapacity() + " mB"))
                .pos(14, 47).size(208, 11).color(theme.getPrimaryColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "PRODUCTION: " + SingleblockUiText.number(currentEnergyProduction) + " EU/t"))
                .pos(14, 63).size(208, 11).color(theme.getProductionColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "BUFFER: " + SingleblockUiText.number((long) energy) + " EU"))
                .pos(14, 79).size(208, 11).color(theme.getEnergyColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "CONSUMPTION: " + currentFuelConsumption + " mB/t"))
                .pos(14, 95).size(208, 11).color(theme.getLabelColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "TARGET FLOW: " + getTargetFlow() + " mB/t"))
                .pos(14, 111).size(208, 11).color(theme.getLabelColor()));

        panel.child(createInfoButton().pos(178, 6));
        if (isClient) {
            panel.child(com.mod.htgt6.client.GUI.TurbineModularUiButtons.createNeiButton(this).pos(196, 6));
        }

        // Only controls that are meaningful for a turbine are exposed: one
        // direct fluid input and the two transformer/tier modifier slots.
        panel.child(new TextWidget("FUEL INPUT").pos(16, 134).color(theme.getLabelColor()));
        if (isClient) {
            com.mod.htgt6.client.GUI.TurbineFluidSlot fluidSlot =
                    new com.mod.htgt6.client.GUI.TurbineFluidSlot(this);
            fluidSlot.syncHandler("gas_tank");
            fluidSlot.background(GuiTextures.SLOT_DARK);
            fluidSlot.pos(16, 148);
            fluidSlot.size(26, 26);
            panel.child(fluidSlot);
        } else {
            panel.child(new FluidSlot().syncHandler("gas_tank").pos(16, 148).size(26, 26));
        }
        panel.child(new TextWidget("IN / OUT").pos(75, 134).color(theme.getLabelColor()));
        panel.child(createItemSlot(0).pos(77, 148));
        panel.child(createItemSlot(1).pos(101, 148));
        panel.child(new TextWidget("MODIFIERS").pos(184, 134).color(theme.getLabelColor()));
        panel.child(createItemSlot(2).pos(186, 148));
        panel.child(createItemSlot(3).pos(208, 148));
        panel.child(SlotGroupWidget.playerInventory().pos(39, 183));
        return panel;
    }

    /** A subtle GT-style frame, kept outside the usable UI area. */
    private static void addPanelFrame(ModularPanel panel) {
        final int color = MachineUiTheme.getActive().getFrameColor();
        addFrameEdge(panel, color, 0, 0, 240, 1);
        addFrameEdge(panel, color, 0, 266, 240, 1);
        addFrameEdge(panel, color, 0, 0, 1, 267);
        addFrameEdge(panel, color, 239, 0, 1, 267);
    }

    private static void addFrameEdge(ModularPanel panel, int color, int x, int y, int width, int height) {
        Widget edge = new Widget();
        edge.background(new Rectangle().setColor(color));
        edge.pos(x, y);
        edge.size(width, height);
        panel.child(edge);
    }

    private ButtonWidget<?> createInfoButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(12, 12);
        button.background(GuiTextures.BUTTON);
        button.overlay(IKey.str("i"));
        button.tooltip(tooltip -> {
            tooltip.setUpdateTooltipEveryTick(true);
            tooltip.addLine("Turbine diagnostics");
            tooltip.addLine("Fuel: " + getGasDisplayName());
            tooltip.addLine("Stored: " + getDisplayedFuelAmount() + " / " + fluidTank.getCapacity() + " mB");
            tooltip.addLine("Energy: " + (long) energy + " / " + (long) maxEnergy + " EU");
            tooltip.addLine("Output: " + getOutputVoltage() + " EU/t");
            tooltip.addLine("Machine tier: " + machineTier);
            tooltip.addLine("Target flow: " + getTargetFlow() + " mB/t");
            tooltip.addLine("Conversion: 100% of registered fuel value");
            tooltip.addLine("Burned fuel reserve: " + (fuelReserveHundredths / 100L) + " EU");
        });
        return button;
    }

    private String getTargetFlow() {
        return currentFuelValue <= 0 ? "-" : String.format(java.util.Locale.US, "%.3f",
                getOutputVoltage() / (double) currentFuelValue);
    }

    private String getGasDisplayName() {
        if (worldObj != null && worldObj.isRemote) return syncedFuelName;
        FluidStack fluid = fluidTank.getFluid();
        return fluid == null ? "EMPTY" : fluid.getLocalizedName();
    }

    private int getDisplayedFuelAmount() {
        return worldObj != null && worldObj.isRemote ? syncedFuelAmount : fluidTank.getFluidAmount();
    }

    private ItemSlot createItemSlot(int index) {
        ModularSlot slot = SyncHandlers.itemSlot(modularInventory, index)
                .slotGroup("turbine")
                .accessibility(index != 1, true);
        return new ItemSlot().slot(slot);
    }

    @Override
    public String getInventoryName() {
        return "UniversalGasTurbine";
    }

    /** Bridges the generator inventory to ModularUI's synced item slots. */
    private static final class TurbineItemHandler implements IItemHandlerModifiable {
        private final UniversalGasTurbineTE turbine;

        private TurbineItemHandler(UniversalGasTurbineTE turbine) {
            this.turbine = turbine;
        }

        @Override
        public int getSlots() {
            return turbine.inventory.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots() ? turbine.inventory[slot] : null;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack == null || !isItemValid(slot, stack)) return stack;

            ItemStack existing = getStackInSlot(slot);
            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            if (existing != null) {
                if (!existing.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(existing, stack)) {
                    return stack;
                }
                limit -= existing.stackSize;
            }
            if (limit <= 0) return stack;

            int moved = Math.min(limit, stack.stackSize);
            if (!simulate) {
                if (existing == null) {
                    ItemStack inserted = stack.copy();
                    inserted.stackSize = moved;
                    turbine.setInventorySlotContents(slot, inserted);
                } else {
                    existing.stackSize += moved;
                    turbine.markDirty();
                }
            }
            if (moved == stack.stackSize) return null;
            ItemStack remaining = stack.copy();
            remaining.stackSize -= moved;
            return remaining;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack existing = getStackInSlot(slot);
            if (existing == null || amount <= 0) return null;

            int extracted = Math.min(amount, existing.stackSize);
            ItemStack result = existing.copy();
            result.stackSize = extracted;
            if (!simulate) {
                if (existing.stackSize == extracted) {
                    turbine.setInventorySlotContents(slot, null);
                } else {
                    existing.stackSize -= extracted;
                    turbine.markDirty();
                }
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 2 ? 1 : turbine.getInventoryStackLimit();
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (slot >= 0 && slot < getSlots()) {
                turbine.setInventorySlotContents(slot, stack);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack == null || slot < 0 || slot >= getSlots()) return false;
            if (slot == 0) {
                return stack.getItem() instanceof IFluidContainerItem
                        || FluidContainerRegistry.isContainer(stack);
            }
            if (slot == 1) return false;
            if (slot == 3) return isTransformer(stack);
            return slot == 2 && isTierModuleAllowed(stack);
        }

        private boolean isTierModuleAllowed(ItemStack stack) {
            ItemStack transformer = turbine.inventory[3];
            if (transformer == null) return false;
            int moduleTier = MachineTierSystem.moduleTier(stack.getItem());
            return moduleTier > 0 && moduleTier <= MachineTierSystem.transformerMaxTier(transformer.getItem());
        }

        private boolean isTransformer(ItemStack stack) {
            Item item = stack.getItem();
            return item == Tmodules.LowLevelTransformer
                    || item == Tmodules.MediumLevelTransformer
                    || item == Tmodules.HighLevelTransformer
                    || item == Tmodules.UltimateLevelTransformer
                    || item == Tmodules.OmegaTransformer;
        }

        private int getModuleTier(Item item) {
            if (item == Tmodules.TierModuleMV) return 2;
            if (item == Tmodules.TierModuleHV) return 3;
            if (item == Tmodules.TierModuleEV) return 4;
            if (item == Tmodules.TierModuleIV) return 5;
            if (item == Tmodules.TierModuleLuV) return 6;
            if (item == Tmodules.TierModuleZPM) return 7;
            if (item == Tmodules.TierModuleUV) return 8;
            if (item == Tmodules.TierModulePUV1) return 9;
            if (item == Tmodules.TierModuleUX) return 10;
            if (item == Tmodules.TierModuleOLV) return 11;
            if (item == Tmodules.TierModuleOMV) return 12;
            if (item == Tmodules.TierModuleOHV) return 13;
            if (item == Tmodules.TierModuleOEV) return 14;
            if (item == Tmodules.TierModuleOIV) return 15;
            if (item == Tmodules.TierModuleMAX) return 16;
            return 0;
        }

    }

    private static final class ValidatingGasTank implements IFluidTank {
        private final UniversalGasTurbineTE turbine;

        private ValidatingGasTank(UniversalGasTurbineTE turbine) {
            this.turbine = turbine;
        }

        @Override
        public FluidStack getFluid() {
            return turbine.fluidTank.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return turbine.fluidTank.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return turbine.fluidTank.getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return turbine.fluidTank.getInfo();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return resource != null && resource.getFluid() != null
                    && VALID_GAS_FUELS.containsKey(resource.getFluid().getName())
                    ? turbine.fluidTank.fill(resource, doFill) : 0;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return turbine.fluidTank.drain(maxDrain, doDrain);
        }
    }
}
