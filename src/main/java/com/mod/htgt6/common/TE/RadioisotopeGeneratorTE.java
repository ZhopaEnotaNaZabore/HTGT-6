package com.mod.htgt6.common.TE;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseGenerator;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.future.IItemHandlerModifiable;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widget.Widget;
import com.mod.htgt6.common.ui.MachineUiTheme;
import com.mod.htgt6.common.ui.SingleblockUiText;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class RadioisotopeGeneratorTE extends TileEntityBaseGenerator implements IGuiHolder {

    public static final int FIXED_TIER = 5;
    private static final int INVENTORY_SIZE = 9;
    private static final double ENERGY_CAPACITY = 1_000_000_000D;

    public static class IsotopeFuel {
        public final int tier;
        public final int decayTime;
        public final int euPerTick;
        // Keep a copy of the input stack for NEI display reference
        public ItemStack inputStack;

        public final java.util.List<DecayOutput> outputs = new java.util.ArrayList<DecayOutput>();

        public IsotopeFuel(int tier, int decayTime, int euPerTick) {
            this.tier = tier;
            this.decayTime = decayTime;
            this.euPerTick = euPerTick;
        }

        public IsotopeFuel addOutput(ItemStack stack, double chance) {
            outputs.add(new DecayOutput(stack, chance));
            return this;
        }
    }

    public static class DecayOutput {
        public final ItemStack stack;
        public final double chance;

        public DecayOutput(ItemStack stack, double chance) {
            this.stack = stack.copy();
            this.chance = chance;
        }
    }

    // Key format: "item_registry_name@metadata" to support GT6 materials seamlessly
    public static final Map<String, IsotopeFuel> VALID_FUELS = new HashMap<String, IsotopeFuel>();

    public static String getFuelKey(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "";
        String name = Item.itemRegistry.getNameForObject(stack.getItem());
        return (name != null ? name : stack.getItem().getUnlocalizedName()) + "@" + stack.getItemDamage();
    }

    public static void addIsotopeFuel(ItemStack stack, IsotopeFuel fuel) {
        if (stack != null && stack.getItem() != null) {
            fuel.inputStack = stack.copy();
            VALID_FUELS.put(getFuelKey(stack), fuel);
        }
    }

    public int activeDecayProgress = 0;
    public int activeDecayMax = 0;
    public int currentFuelEUOutput = 0;
    public IsotopeFuel activeFuel = null;
    public int actualEnergyProduction;
    private java.util.List<ItemStack> pendingDecayOutputs;
    private final IItemHandlerModifiable modularInventory = new RadioisotopeInventory(this);

    public RadioisotopeGeneratorTE() {
        // Keep nine persisted slots for compatibility with existing worlds.
        // Only 0-6 are operational; the former modifier slots stay disabled.
        super(INVENTORY_SIZE, ENERGY_CAPACITY);
        machineTier = FIXED_TIER;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        machineTier = FIXED_TIER;

        if (worldObj == null || worldObj.isRemote) return;
        actualEnergyProduction = 0;
        if (!enabled) return;

        processIsotopeDecay();
    }

    private void processIsotopeDecay() {
        if (pendingDecayOutputs != null) {
            flushDecayOutputs();
            return;
        }
        if (activeDecayProgress <= 0) {
            if (inventory[0] != null) {
                String fuelKey = getFuelKey(inventory[0]);

                if (VALID_FUELS.containsKey(fuelKey)) {
                    IsotopeFuel fuel = VALID_FUELS.get(fuelKey);

                    if (machineTier >= fuel.tier) {
                        activeDecayProgress = fuel.decayTime;
                        activeDecayMax = fuel.decayTime;
                        currentFuelEUOutput = fuel.euPerTick;
                        activeFuel = fuel;

                        decrStackSize(0, 1);
                        markDirty();
                    }
                }
            }
        }

        if (activeDecayProgress > 0 && activeFuel != null) {
            if (energy + currentFuelEUOutput <= maxEnergy) {
                actualEnergyProduction = (int) addGeneratedEnergy(currentFuelEUOutput);
                activeDecayProgress--;
                markDirty();

                if (activeDecayProgress <= 0) {
                    pendingDecayOutputs = new java.util.ArrayList<ItemStack>();
                    for (DecayOutput output : activeFuel.outputs) {
                        if (output.stack != null && worldObj.rand.nextDouble() < output.chance) {
                            pendingDecayOutputs.add(output.stack.copy());
                        }
                    }
                    flushDecayOutputs();
                    markDirty();
                }
            }
        }
    }

    /** Simulate the entire batch before committing, so a full output never rerolls or duplicates it. */
    private void flushDecayOutputs() {
        ItemStack[] result = new ItemStack[6];
        for (int i = 0; i < result.length; i++) result[i] = inventory[i + 1] == null ? null : inventory[i + 1].copy();
        for (ItemStack output : pendingDecayOutputs) {
            int remaining = output.stackSize;
            for (int pass = 0; pass < 2 && remaining > 0; pass++) {
                for (int i = 0; i < result.length && remaining > 0; i++) {
                    ItemStack current = result[i];
                    if (pass == 0 && (current == null || !current.isItemEqual(output)
                            || !ItemStack.areItemStackTagsEqual(current, output))) continue;
                    if (pass == 1 && current != null) continue;
                    int moved = Math.min(remaining, Math.min(64, output.getMaxStackSize())
                            - (current == null ? 0 : current.stackSize));
                    if (moved <= 0) continue;
                    if (current == null) { result[i] = output.copy(); result[i].stackSize = moved; }
                    else current.stackSize += moved;
                    remaining -= moved;
                }
            }
            if (remaining > 0) return;
        }
        for (int i = 0; i < result.length; i++) inventory[i + 1] = result[i];
        pendingDecayOutputs = null;
        activeFuel = null;
        activeDecayMax = 0;
        currentFuelEUOutput = 0;
        markDirty();
    }

    @Override public String getInventoryName() { return "RadioisotopeGenerator"; }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0 && VALID_FUELS.containsKey(getFuelKey(stack));
    }

    @Override
    public ModularScreen createScreen(GuiCreationContext context, ModularPanel panel) {
        return new ModularScreen(panel);
    }

    @Override
    public ModularPanel buildUI(GuiCreationContext context, GuiSyncManager syncManager, boolean isClient) {
        syncManager.registerSlotGroup("radioisotope", 7);
        syncManager.syncValue("energy", SyncHandlers.longNumber(() -> (long) energy, value -> energy = value));
        syncManager.syncValue("decay_progress", SyncHandlers.intNumber(
                () -> activeDecayProgress, value -> activeDecayProgress = value));
        syncManager.syncValue("decay_max", SyncHandlers.intNumber(
                () -> activeDecayMax, value -> activeDecayMax = value));
        syncManager.syncValue("fuel_output", SyncHandlers.intNumber(
                () -> currentFuelEUOutput, value -> currentFuelEUOutput = value));
        syncManager.syncValue("actual_generation", SyncHandlers.intNumber(
                () -> actualEnergyProduction, value -> actualEnergyProduction = value));
        syncManager.syncValue("enabled", SyncHandlers.bool(() -> enabled, value -> enabled = value));

        MachineUiTheme theme = MachineUiTheme.getActive();
        ModularPanel panel = ModularPanel.defaultPanel("radioisotope_generator", 240, 267)
                .background(new Rectangle().setColor(theme.getPanelColor()));
        addPanelFrame(panel, theme.getFrameColor());

        panel.child(new TextWidget("RADIOISOTOPE GENERATOR").pos(13, 8).color(theme.getTitleColor()));
        panel.child(createInfoButton().pos(178, 6));
        if (isClient) {
            panel.child(com.mod.htgt6.client.GUI.RadioisotopeModularUiButtons
                    .createNeiButton(this).pos(196, 6));
        }

        // Keep the legacy RTG machine-slot geometry without depending on its old PNG GUI.
        Widget machineArea = new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        machineArea.pos(8, 24).size(224, 104);
        panel.child(machineArea);

        // Compact live decay terminal above the input/output work area.
        Widget decayTerminal = new Widget().background(new Rectangle().setColor(theme.getPanelColor()));
        decayTerminal.pos(14, 30).size(150, 16);
        panel.child(decayTerminal);
        panel.child(new TextWidget(IKey.dynamic(() -> activeDecayProgress > 0
                ? String.format("DECAY TIME: %.1f s", activeDecayProgress / 20.0D)
                : "DECAY TIME: IDLE"))
                .pos(18, 34).size(142, 9).color(theme.getPrimaryColor()));

        // Original PNG geometry, shifted down as one group to make room for the terminal.
        panel.child(new TextWidget("ISOTOPE").pos(21, 52).color(theme.getLabelColor()));
        panel.child(createItemSlot(0, true, true).pos(25, 67));
        panel.child(new TextWidget(IKey.str(">>")).pos(49, 72).color(theme.getProductionColor()));
        panel.child(new TextWidget("DECAY PRODUCTS").pos(70, 47).color(theme.getLabelColor()));
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int index = 1 + row * 3 + col;
                panel.child(createItemSlot(index, false, true).pos(70 + col * 18, 58 + row * 18));
            }
        }
        if (isClient) {
            panel.child(new com.mod.htgt6.client.GUI.RadioisotopeActivityWidget(this)
                    .pos(155, 50).size(58, 58));
        }

        Widget terminal = new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        terminal.pos(8, 134).size(224, 38);
        panel.child(terminal);
        panel.child(new TextWidget(IKey.dynamic(() -> "GENERATING: " + SingleblockUiText.number(actualEnergyProduction) + " EU/t"))
                .pos(14, 143).size(208, 11).color(theme.getProductionColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "BUFFER: " + SingleblockUiText.number((long) energy) + " EU"))
                .pos(14, 158).size(208, 11).color(theme.getEnergyColor()));

        panel.child(SlotGroupWidget.playerInventory().pos(39, 183));
        return panel;
    }

    private ItemSlot createItemSlot(int index, boolean canPut, boolean canTake) {
        ModularSlot slot = SyncHandlers.itemSlot(modularInventory, index)
                .slotGroup("radioisotope").accessibility(canPut, canTake);
        return new ItemSlot().slot(slot);
    }

    private ButtonWidget<?> createInfoButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(12, 12).background(GuiTextures.BUTTON).overlay(IKey.str("i"));
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine("Radioisotope Generator diagnostics");
            t.addLine(activeDecayProgress > 0
                    ? "Decay: " + activeDecayProgress + " / " + activeDecayMax + " ticks"
                    : "Decay: idle");
            t.addLine("Generation: " + actualEnergyProduction + " EU/t");
            t.addLine("Buffer: " + (long) energy + " / " + (long) maxEnergy + " EU");
            t.addLine("Fixed tier: " + FIXED_TIER + " (" + getOutputVoltage() + " EU/t)");
        });
        return button;
    }

    private static void addPanelFrame(ModularPanel panel, int color) {
        frame(panel, color, 0, 0, 240, 1);
        frame(panel, color, 0, 266, 240, 1);
        frame(panel, color, 0, 0, 1, 267);
        frame(panel, color, 239, 0, 1, 267);
    }

    private static void frame(ModularPanel panel, int color, int x, int y, int width, int height) {
        Widget edge = new Widget();
        edge.background(new Rectangle().setColor(color));
        edge.pos(x, y);
        edge.size(width, height);
        panel.child(edge);
    }

    private static final class RadioisotopeInventory implements IItemHandlerModifiable {
        private final RadioisotopeGeneratorTE generator;
        private RadioisotopeInventory(RadioisotopeGeneratorTE generator) { this.generator = generator; }
        @Override public int getSlots() { return 7; }
        @Override public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots() ? generator.inventory[slot] : null;
        }
        @Override public int getSlotLimit(int slot) { return generator.getInventoryStackLimit(); }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (slot >= 0 && slot < getSlots()) generator.setInventorySlotContents(slot, stack);
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && stack != null && VALID_FUELS.containsKey(getFuelKey(stack));
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) return stack;
            ItemStack current = getStackInSlot(slot);
            if (current != null && (!current.isItemEqual(stack)
                    || !ItemStack.areItemStackTagsEqual(current, stack))) return stack;
            int moved = Math.min(stack.stackSize, Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - (current == null ? 0 : current.stackSize));
            if (moved <= 0) return stack;
            if (!simulate) {
                if (current == null) {
                    ItemStack inserted = stack.copy(); inserted.stackSize = moved;
                    generator.setInventorySlotContents(slot, inserted);
                } else {
                    current.stackSize += moved; generator.markDirty();
                }
            }
            if (moved == stack.stackSize) return null;
            ItemStack remaining = stack.copy(); remaining.stackSize -= moved;
            return remaining;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack current = getStackInSlot(slot);
            if (current == null || amount <= 0) return null;
            ItemStack result = current.copy(); result.stackSize = Math.min(amount, current.stackSize);
            if (!simulate) {
                current.stackSize -= result.stackSize;
                if (current.stackSize <= 0) generator.setInventorySlotContents(slot, null);
                else generator.markDirty();
            }
            return result;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        actualEnergyProduction = 0;
        activeFuel = null;
        pendingDecayOutputs = null;
        if (nbt.hasKey("PendingDecayOutputs")) {
            pendingDecayOutputs = new java.util.ArrayList<ItemStack>();
            net.minecraft.nbt.NBTTagList list = nbt.getTagList("PendingDecayOutputs", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                ItemStack stack = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
                if (stack != null && stack.stackSize > 0) pendingDecayOutputs.add(stack);
            }
        }
        machineTier = FIXED_TIER;
        activeDecayProgress = nbt.getInteger("DecayProgress");
        activeDecayMax = nbt.getInteger("DecayMax");
        currentFuelEUOutput = nbt.getInteger("FuelEUOutput");

        // FIX: Look up active burning fuel by its saved NBT key identifier instead of slot 0
        if (activeDecayProgress > 0 && nbt.hasKey("ActiveFuelKey")) {
            String savedKey = nbt.getString("ActiveFuelKey");
            if (VALID_FUELS.containsKey(savedKey)) {
                activeFuel = VALID_FUELS.get(savedKey);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (pendingDecayOutputs != null) {
            net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
            for (ItemStack stack : pendingDecayOutputs) {
                NBTTagCompound entry = new NBTTagCompound();
                stack.writeToNBT(entry);
                list.appendTag(entry);
            }
            nbt.setTag("PendingDecayOutputs", list);
        } else nbt.removeTag("PendingDecayOutputs");
        nbt.setInteger("MachineTier", FIXED_TIER);
        nbt.setInteger("DecayProgress", activeDecayProgress);
        nbt.setInteger("DecayMax", activeDecayMax);
        nbt.setInteger("FuelEUOutput", currentFuelEUOutput);

        // FIX: Save the burning fuel's key if it exists
        if (activeFuel != null && activeFuel.inputStack != null) {
            nbt.setString("ActiveFuelKey", getFuelKey(activeFuel.inputStack));
        }

    }
}
