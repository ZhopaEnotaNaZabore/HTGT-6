package com.mod.htgt6.common.TE;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import com.mod.htgt6.api.hitech6.fuelconfigs.FluidFuelAccounting;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityFluidGenerator;
import com.mod.htgt6.common.ui.MachineUiTheme;
import com.mod.htgt6.common.ui.SingleblockUiText;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
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
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Universal multi-fuel engine built on the shared fluid-generator API. */
public class UniversalEngineTE extends TileEntityFluidGenerator implements IGuiHolder {

    public enum FuelType {
        SEMIFUEL("Semifuel"), DIESEL("Diesel"), FAL("FAL Rocket"),
        CHEMICAL("Chemical"), GEOTHERMAL("GeoThermal"), SOLID("Solid");

        private final String displayName;
        FuelType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public static final Map<String, Integer> VALID_SEMIFUELS = new HashMap<String, Integer>();
    public static final Map<String, Integer> VALID_DIESEL_FUELS = new HashMap<String, Integer>();
    public static final Map<String, Integer> VALID_FAL_FUELS = new HashMap<String, Integer>();
    public static final Map<String, Integer> VALID_CHEMICAL_FUELS = new HashMap<String, Integer>();
    public static final Map<String, Integer> VALID_GEOTHERMAL_FUELS = new HashMap<String, Integer>();
    /** Registry key + metadata -> complete EU value for one consumed item. */
    public static final Map<String, Long> VALID_SOLID_FUELS = new HashMap<String, Long>();

    private static final Map<String, FuelType> FLUID_FUEL_TYPES = new HashMap<String, FuelType>();
    /** Display stacks retained for NEI; the key remains the authoritative lookup key. */
    private static final Map<String, ItemStack> SOLID_FUEL_DISPLAY_STACKS = new HashMap<String, ItemStack>();
    private long pendingSolidEnergy;
    private long activeSolidFuelTotalEnergy;
    /** Energy already taken from liquid fuel, emitted over subsequent ticks. */
    private long pendingFluidEnergy;
    private int pendingFluidFraction;
    private long currentEnergyProduction;
    private String activeSolidFuelName = "";
    private String activeFluidFuelName = "";
    private int activeFluidEuPerMb;
    /** Server-authoritative terminal text; client tanks are not a reliable text source. */
    private String syncedFuelDisplay = "EMPTY";
    private String syncedFuelOperation = "FUEL MODE: IDLE";
    private final IItemHandlerModifiable modularInventory = new EngineItemHandler(this);

    public UniversalEngineTE() {
        super(4, 128000, 1536870912D);
    }

    // Legacy configuration methods retained for existing configs and scripts.
    public static void addSemifuel(String fluidName, int euPerMb) { registerFluidFuel(FuelType.SEMIFUEL, fluidName, euPerMb); }
    public static void addDiesel(String fluidName, int euPerMb) { registerFluidFuel(FuelType.DIESEL, fluidName, euPerMb); }
    public static void addFalFuel(String fluidName, int euPerMb) { registerFluidFuel(FuelType.FAL, fluidName, euPerMb); }
    public static void addChemicalFuel(String fluidName, int euPerMb) { registerFluidFuel(FuelType.CHEMICAL, fluidName, euPerMb); }
    public static void addGeothermalFuel(String fluidName, int euPerMb) { registerFluidFuel(FuelType.GEOTHERMAL, fluidName, euPerMb); }

    /** GT6 FL entries resolve to FluidStack; their amount is not consumed during registration. */
    public static void addChemicalFuel(FluidStack fluid, int euPerMb) { registerFluidFuel(FuelType.CHEMICAL, fluid, euPerMb); }
    public static void addGeothermalFuel(FluidStack fluid, int euPerMb) { registerFluidFuel(FuelType.GEOTHERMAL, fluid, euPerMb); }
    public static void addSemifuel(FluidStack fluid, int euPerMb) { registerFluidFuel(FuelType.SEMIFUEL, fluid, euPerMb); }
    public static void addDiesel(FluidStack fluid, int euPerMb) { registerFluidFuel(FuelType.DIESEL, fluid, euPerMb); }
    public static void addFalFuel(FluidStack fluid, int euPerMb) { registerFluidFuel(FuelType.FAL, fluid, euPerMb); }

    public static void addSolidFuel(Item item, int metadata, long totalEu) {
        if (item == null || totalEu <= 0L) return;
        String key = solidKey(item, metadata);
        VALID_SOLID_FUELS.put(key, totalEu);
        SOLID_FUEL_DISPLAY_STACKS.put(key, new ItemStack(item, 1, metadata));
    }

    /** Accepts GT6 OP-generated MetaItems without mutating the supplied stack. */
    public static void addSolidFuel(ItemStack stack, long totalEu) {
        if (stack != null) addSolidFuel(stack.getItem(), stack.getItemDamage(), totalEu);
    }

    private static void registerFluidFuel(FuelType type, String fluidName, int euPerMb) {
        if (fluidName == null || euPerMb <= 0) return;
        getFluidRegistry(type).put(fluidName, euPerMb);
        FLUID_FUEL_TYPES.put(fluidName, type);
    }

    private static void registerFluidFuel(FuelType type, FluidStack fluid, int euPerMb) {
        if (fluid != null && fluid.getFluid() != null) {
            registerFluidFuel(type, fluid.getFluid().getName(), euPerMb);
        }
    }

    private static Map<String, Integer> getFluidRegistry(FuelType type) {
        switch (type) {
            case SEMIFUEL: return VALID_SEMIFUELS;
            case DIESEL: return VALID_DIESEL_FUELS;
            case FAL: return VALID_FAL_FUELS;
            case CHEMICAL: return VALID_CHEMICAL_FUELS;
            case GEOTHERMAL: return VALID_GEOTHERMAL_FUELS;
            default: throw new IllegalArgumentException("Not a fluid fuel type: " + type);
        }
    }

    public static FuelType getFuelType(String fluidName) { return FLUID_FUEL_TYPES.get(fluidName); }
    public static int getFuelEuPerMb(String fluidName) {
        FuelType type = getFuelType(fluidName);
        if (type == null) return 0;
        Integer value = getFluidRegistry(type).get(fluidName);
        return value == null ? 0 : value;
    }

    private static String solidKey(Item item, int metadata) {
        Object registryName = Item.itemRegistry.getNameForObject(item);
        return String.valueOf(registryName) + "@" + metadata;
    }

    private static long getSolidFuelValue(ItemStack stack) {
        if (stack == null) return 0L;
        Long value = VALID_SOLID_FUELS.get(solidKey(stack.getItem(), stack.getItemDamage()));
        if (value == null) value = VALID_SOLID_FUELS.get(solidKey(stack.getItem(), 32767));
        return value == null ? 0L : value;
    }

    public static long getSolidFuelEu(ItemStack stack) {
        return getSolidFuelValue(stack);
    }

    public static boolean isSolidFuel(ItemStack stack) {
        return getSolidFuelValue(stack) > 0L;
    }

    /**
     * Returns the current API-configured fluid fuels for clients such as NEI.
     * The list is assembled on request so entries registered during init (or by
     * integrations) are never captured by an early client-side cache.
     */
    public static List<RegisteredFluidFuel> getRegisteredFluidFuels() {
        List<RegisteredFluidFuel> result = new ArrayList<RegisteredFluidFuel>();
        addRegisteredFluidFuels(result, FuelType.SEMIFUEL, VALID_SEMIFUELS);
        addRegisteredFluidFuels(result, FuelType.DIESEL, VALID_DIESEL_FUELS);
        addRegisteredFluidFuels(result, FuelType.FAL, VALID_FAL_FUELS);
        addRegisteredFluidFuels(result, FuelType.CHEMICAL, VALID_CHEMICAL_FUELS);
        addRegisteredFluidFuels(result, FuelType.GEOTHERMAL, VALID_GEOTHERMAL_FUELS);
        return result;
    }

    public static List<RegisteredSolidFuel> getRegisteredSolidFuels() {
        List<RegisteredSolidFuel> result = new ArrayList<RegisteredSolidFuel>();
        for (Map.Entry<String, Long> entry : VALID_SOLID_FUELS.entrySet()) {
            ItemStack stack = SOLID_FUEL_DISPLAY_STACKS.get(entry.getKey());
            if (stack != null) result.add(new RegisteredSolidFuel(stack.copy(), entry.getValue()));
        }
        return result;
    }

    private static void addRegisteredFluidFuels(List<RegisteredFluidFuel> target, FuelType type,
                                                Map<String, Integer> fuels) {
        for (Map.Entry<String, Integer> entry : fuels.entrySet()) {
            target.add(new RegisteredFluidFuel(entry.getKey(), entry.getValue(), type));
        }
    }

    public static final class RegisteredFluidFuel {
        public final String fluidName;
        public final int euPerMb;
        public final FuelType type;

        private RegisteredFluidFuel(String fluidName, int euPerMb, FuelType type) {
            this.fluidName = fluidName;
            this.euPerMb = euPerMb;
            this.type = type;
        }
    }

    public static final class RegisteredSolidFuel {
        public final ItemStack stack;
        public final long totalEu;

        private RegisteredSolidFuel(ItemStack stack, long totalEu) {
            this.stack = stack;
            this.totalEu = totalEu;
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;
        updateMachineTier();

        currentEnergyProduction = 0L;
        processFuelInput();
        if (enabled) {
            if (pendingFluidEnergy > 0L || fluidTank.getFluidAmount() > 0) {
                generateFluidEnergy();
            } else {
                generateSolidEnergy();
            }
        }
        refreshFuelDisplay();
    }

    /** Processes exactly one item per tick; the former implementation did it twice. */
    private void processFuelInput() {
        ItemStack input = inventory[0];
        if (input == null) return;

        if (input.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem container = (IFluidContainerItem) input.getItem();
            FluidStack contained = container.getFluid(input);
            if (contained == null || !isFluidValid(contained)) return;
            int accepted = fluidTank.fill(contained, false);
            if (accepted <= 0 || !canOutputEmptyContainer(input, accepted, container)) return;

            ItemStack drainedContainer = input.copy();
            drainedContainer.stackSize = 1;
            FluidStack drained = container.drain(drainedContainer, accepted, true);
            if (drained == null || drained.amount <= 0) return;
            fluidTank.fill(drained, true);
            moveInputToOutput(drainedContainer);
            return;
        }

        if (FluidContainerRegistry.isFilledContainer(input)) {
            FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(input);
            if (contained == null || !isFluidValid(contained)
                    || fluidTank.fill(contained, false) != contained.amount) return;
            ItemStack empty = FluidContainerRegistry.drainFluidContainer(input);
            if (empty == null || !canOutput(empty)) return;
            fluidTank.fill(contained, true);
            moveInputToOutput(empty);
        }
    }

    private boolean canOutputEmptyContainer(ItemStack input, int amount, IFluidContainerItem container) {
        ItemStack result = input.copy();
        result.stackSize = 1;
        container.drain(result, amount, true);
        return canOutput(result);
    }

    private boolean canOutput(ItemStack stack) {
        return inventory[1] == null || (inventory[1].isItemEqual(stack)
                && ItemStack.areItemStackTagsEqual(inventory[1], stack)
                && inventory[1].stackSize < inventory[1].getMaxStackSize());
    }

    private void moveInputToOutput(ItemStack output) {
        inventory[0].stackSize--;
        if (inventory[0].stackSize <= 0) inventory[0] = null;
        if (inventory[1] == null) inventory[1] = output.copy();
        else inventory[1].stackSize++;
        markDirty();
    }

    /**
     * Converts configured fuel energy into a small pending reserve, then emits
     * no more than this machine's tier voltage per tick. This preserves the
     * full value of high-tier fuel in a low-tier engine: e.g. 512 EU/mB at LV
     * produces 32 EU/t for 16 ticks instead of an invalid 512 EU/t burst.
     */
    private void generateFluidEnergy() {
        long freeBuffer = (long) Math.floor(maxEnergy - energy);
        long outputThisTick = Math.min((long) getOutputVoltage(), freeBuffer);
        if (outputThisTick <= 0L) return;

        FluidStack fuel = fluidTank.getFluid();
        int euPerMb = fuel == null ? 0 : getFuelEuPerMb(fuel.getFluid().getName());
        FluidFuelAccounting.Step step = FluidFuelAccounting.plan(
                pendingFluidEnergy * 100L + pendingFluidFraction,
                fuel == null ? 0 : fuel.amount, euPerMb,
                100,
                getOutputVoltage(), freeBuffer);
        if (step.consumedMb > 0) {
            activeFluidFuelName = fuel.getLocalizedName();
            activeFluidEuPerMb = euPerMb;
            fluidTank.drain(step.consumedMb, true);
        }
        pendingFluidEnergy = step.reserveHundredths / 100L;
        pendingFluidFraction = (int) (step.reserveHundredths % 100L);
        currentEnergyProduction = (long) addGeneratedEnergy(step.generatedEu);
        if (step.consumedMb > 0 || step.generatedEu > 0) markDirty();
    }

    private void generateSolidEnergy() {
        if (energy >= maxEnergy) return;
        if (pendingSolidEnergy <= 0L) {
            long fuelValue = getSolidFuelValue(inventory[0]);
            if (fuelValue <= 0L) return;
            activeSolidFuelName = inventory[0].getDisplayName();
            activeSolidFuelTotalEnergy = fuelValue;
            pendingSolidEnergy = fuelValue;
            inventory[0].stackSize--;
            if (inventory[0].stackSize <= 0) inventory[0] = null;
        }

        long freeBuffer = (long) Math.floor(maxEnergy - energy);
        long produced = Math.min(pendingSolidEnergy, Math.min((long) getOutputVoltage(), freeBuffer));
        if (produced <= 0L) return;
        pendingSolidEnergy -= produced;
        addGeneratedEnergy(produced);
        currentEnergyProduction = produced;
        markDirty();
    }

    private void updateMachineTier() {
        ItemStack transformer = inventory[3];
        ItemStack module = inventory[2];
        machineTier = MachineTierSystem.resolveMachineTier(module, transformer);
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

    @Override
    protected boolean isFluidValid(FluidStack resource) {
        return resource != null && resource.getFluid() != null
                && getFuelEuPerMb(resource.getFluid().getName()) > 0;
    }

    @Override public String getInventoryName() { return "UniversalEngine"; }

    @Override
    public ModularScreen createScreen(GuiCreationContext context, ModularPanel panel) { return new ModularScreen(panel); }

    @Override
    public ModularPanel buildUI(GuiCreationContext context, GuiSyncManager syncManager, boolean isClient) {
        com.mod.htgt6.common.network.MachineUiTransfers.bind(syncManager, this, isClient);
        syncManager.syncValue("enabled", SyncHandlers.bool(() -> enabled, value -> enabled = value));
        syncManager.registerSlotGroup("engine", 4);
        FluidSlotSyncHandler tank = SyncHandlers.fluidSlot(fluidTank).canFillSlot(true).canDrainSlot(true);
        syncManager.syncValue("fuel_tank", tank);
        syncManager.syncValue("energy", SyncHandlers.longNumber(() -> (long) energy, value -> energy = value));
        syncManager.syncValue("tier", SyncHandlers.intNumber(() -> machineTier, value -> machineTier = value));
        syncManager.syncValue("current_generation", SyncHandlers.longNumber(
                () -> currentEnergyProduction, value -> currentEnergyProduction = value));
        syncManager.syncValue("fuel_display", SyncHandlers.string(
                () -> syncedFuelDisplay, value -> syncedFuelDisplay = value));
        syncManager.syncValue("fuel_operation", SyncHandlers.string(
                () -> syncedFuelOperation, value -> syncedFuelOperation = value));

        ModularPanel panel = ModularPanel.defaultPanel("universal_engine", 240, 267);
        MachineUiTheme theme = MachineUiTheme.getActive();
        panel.background(new Rectangle().setColor(theme.getPanelColor()));
        addPanelFrame(panel);
        Widget terminal = new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        terminal.pos(8, 24).size(224, 88);
        panel.child(terminal);
        panel.child(new TextWidget("UNIVERSAL ENGINE").pos(13, 8).color(theme.getTitleColor()));
        panel.child(createInfoButton().pos(178, 6));
        if (isClient) panel.child(com.mod.htgt6.client.GUI.EngineModularUiButtons.createNeiButton(this).pos(196, 6));
        panel.child(new TextWidget(IKey.dynamic(() -> SingleblockUiText.label("FUEL: " + getFuelDisplay()))).pos(14, 34).size(208, 11).color(theme.getPrimaryColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "BUFFER: " + SingleblockUiText.number((long) energy) + " EU")).pos(14, 55).size(208, 11).color(theme.getEnergyColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "GENERATING: " + SingleblockUiText.number(currentEnergyProduction) + " EU/t")).pos(14, 76).size(208, 11).color(theme.getProductionColor()));
        panel.child(new TextWidget(IKey.dynamic(this::getFuelOperationDisplay)).pos(14, 93).size(208, 11).color(theme.getLabelColor()));

        panel.child(new TextWidget("FLUID INPUT").pos(16, 119).color(theme.getLabelColor()));
        if (isClient) {
            com.mod.htgt6.client.GUI.EngineFluidSlot fluidSlot = new com.mod.htgt6.client.GUI.EngineFluidSlot(this);
            fluidSlot.syncHandler("fuel_tank");
            fluidSlot.background(GuiTextures.SLOT_DARK).pos(16, 133).size(26, 26);
            panel.child(fluidSlot);
        } else {
            panel.child(new FluidSlot().syncHandler("fuel_tank").pos(16, 133).size(26, 26));
        }
        panel.child(new TextWidget("INPUT").pos(55, 119).color(theme.getLabelColor()));
        panel.child(createItemSlot(0, true, true).pos(66, 133));
        panel.child(new TextWidget("OUTPUT").pos(112, 119).color(theme.getLabelColor()));
        panel.child(createItemSlot(1, false, true).pos(121, 133));
        panel.child(new TextWidget("MODIFIERS").pos(184, 119).color(theme.getLabelColor()));
        panel.child(createItemSlot(2, true, true).pos(186, 133));
        panel.child(createItemSlot(3, true, true).pos(208, 133));
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

    private String getFuelDisplay() {
        if (worldObj != null && worldObj.isRemote) return syncedFuelDisplay;
        return createFuelDisplay();
    }

    private void refreshFuelDisplay() {
        syncedFuelDisplay = createFuelDisplay();
        syncedFuelOperation = createFuelOperationDisplay();
    }

    private String getFuelOperationDisplay() {
        return worldObj != null && worldObj.isRemote ? syncedFuelOperation : createFuelOperationDisplay();
    }

    private String createFuelDisplay() {
        FluidStack fuel = fluidTank.getFluid();
        if (fuel != null) return fuel.getLocalizedName() + " " + fuel.amount + "/" + fluidTank.getCapacity() + " mB";
        if (pendingFluidEnergy > 0L) return activeFluidFuelName + " (" + pendingFluidEnergy + " EU reserve)";
        return pendingSolidEnergy > 0L
                ? activeSolidFuelName + " (" + pendingSolidEnergy + " EU remaining)"
                : "EMPTY";
    }

    private String createFuelOperationDisplay() {
        FluidStack fluid = fluidTank.getFluid();
        int euPerMb = fluid == null ? activeFluidEuPerMb : getFuelEuPerMb(fluid.getFluid().getName());
        if (euPerMb > 0 && (fluid != null || pendingFluidEnergy > 0L)) {
            double optimalFlow = getOutputVoltage() / (double) euPerMb;
            return String.format(java.util.Locale.US, "OPTIMAL FLOW: %.3f mB/t", optimalFlow);
        }
        if (pendingSolidEnergy > 0L) {
            long euPerSecond = Math.max(1L, (long) getOutputVoltage() * 20L);
            long remainingSeconds = (pendingSolidEnergy + euPerSecond - 1L) / euPerSecond;
            long totalSeconds = (activeSolidFuelTotalEnergy + euPerSecond - 1L) / euPerSecond;
            return "BURN TIME: " + remainingSeconds + "/" + totalSeconds + " s";
        }
        return "FUEL MODE: IDLE";
    }

    /** Detailed live values stay behind [i], keeping the terminal compact. */
    private ButtonWidget<?> createInfoButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(12, 12);
        button.background(GuiTextures.BUTTON);
        button.overlay(IKey.str("i"));
        button.tooltip(tooltip -> {
            tooltip.setUpdateTooltipEveryTick(true);
            tooltip.addLine("Universal Engine status");
            tooltip.addLine("Fuel: " + getFuelDisplay());
            tooltip.addLine("Generating: " + currentEnergyProduction + " EU/t");
            tooltip.addLine("Conversion: 100% of registered fuel value");
            tooltip.addLine("Buffer: " + (long) energy + " / " + (long) maxEnergy + " EU");
            tooltip.addLine("Machine tier: " + machineTier + " (" + getOutputVoltage() + " EU/t)");
            tooltip.addLine(getFuelOperationDisplay());
        });
        return button;
    }

    private ItemSlot createItemSlot(int index, boolean canTake, boolean canPut) {
        ModularSlot slot = SyncHandlers.itemSlot(modularInventory, index).slotGroup("engine").accessibility(canTake, canPut);
        return new ItemSlot().slot(slot);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        pendingSolidEnergy = nbt.getLong("PendingSolidEnergy");
        activeSolidFuelTotalEnergy = nbt.getLong("ActiveSolidFuelTotalEnergy");
        pendingFluidEnergy = Math.max(0L, Math.min(FluidFuelAccounting.MAX_RESERVE / 100L, nbt.getLong("PendingFluidEnergy")));
        pendingFluidFraction = Math.max(0, Math.min(99, nbt.getInteger("PendingFluidFraction")));
        activeSolidFuelName = nbt.getString("ActiveSolidFuelName");
        activeFluidFuelName = nbt.getString("ActiveFluidFuelName");
        activeFluidEuPerMb = nbt.getInteger("ActiveFluidEuPerMb");
        refreshFuelDisplay();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("PendingSolidEnergy", pendingSolidEnergy);
        nbt.setLong("ActiveSolidFuelTotalEnergy", activeSolidFuelTotalEnergy);
        nbt.setLong("PendingFluidEnergy", pendingFluidEnergy);
        nbt.setInteger("PendingFluidFraction", pendingFluidFraction);
        nbt.setString("ActiveSolidFuelName", activeSolidFuelName);
        nbt.setString("ActiveFluidFuelName", activeFluidFuelName);
        nbt.setInteger("ActiveFluidEuPerMb", activeFluidEuPerMb);
    }

    /** Inventory bridge for ModularUI; slot 0 is deliberately solid-fuel only. */
    private static final class EngineItemHandler implements IItemHandlerModifiable {
        private final UniversalEngineTE engine;
        private EngineItemHandler(UniversalEngineTE engine) { this.engine = engine; }
        @Override public int getSlots() { return engine.inventory.length; }
        @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < getSlots() ? engine.inventory[slot] : null; }
        @Override public int getSlotLimit(int slot) { return slot >= 2 ? 1 : engine.getInventoryStackLimit(); }
        @Override public void setStackInSlot(int slot, ItemStack stack) { if (slot >= 0 && slot < getSlots()) engine.setInventorySlotContents(slot, stack); }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (stack == null) return false;
            if (slot == 0) return UniversalEngineTE.isSolidFuel(stack);
            if (slot == 1) return false;
            return slot == 2 || slot == 3;
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) return stack;
            ItemStack current = getStackInSlot(slot);
            if (current != null && (!current.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(current, stack))) return stack;
            int moved = Math.min(stack.stackSize, Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - (current == null ? 0 : current.stackSize));
            if (moved <= 0) return stack;
            if (!simulate) {
                if (current == null) { ItemStack copy = stack.copy(); copy.stackSize = moved; engine.setInventorySlotContents(slot, copy); }
                else { current.stackSize += moved; engine.markDirty(); }
            }
            if (moved == stack.stackSize) return null;
            ItemStack remaining = stack.copy(); remaining.stackSize -= moved; return remaining;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack current = getStackInSlot(slot);
            if (current == null || amount <= 0) return null;
            ItemStack result = current.copy(); result.stackSize = Math.min(amount, current.stackSize);
            if (!simulate) {
                current.stackSize -= result.stackSize;
                if (current.stackSize <= 0) engine.setInventorySlotContents(slot, null); else engine.markDirty();
            }
            return result;
        }
    }
}
