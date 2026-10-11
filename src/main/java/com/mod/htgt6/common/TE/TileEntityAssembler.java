package com.mod.htgt6.common.TE;

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
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityFluidMachine;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import com.mod.htgt6.common.ui.MachineUiTheme;
import com.mod.htgt6.common.ui.SingleblockUiText;
import ic2.api.item.IElectricItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/** Standalone assembler block backed by the shared FluidMachine API. */
public class TileEntityAssembler extends TileEntityFluidMachine implements IGuiHolder {

    public static final int TOTAL_SLOTS = 20;
    public static final int SLOT_OUTPUT = 9;
    public static final int SLOT_FLUID = 10;
    public static final int SLOT_MODULE = 11;
    public static final int SLOT_TRANSFORMER = 12;
    public static final int TANK_CAPACITY = 128000;

    public MachineRecipe currentRecipe;
    public long recipeEUt;
    private int idleSearchDelay;
    private ItemStack syncedRecipePreview;
    private final IItemHandlerModifiable modularInventory = new AssemblerInventory(this);
    private final IItemHandlerModifiable recipePreview = new RecipePreviewInventory(this);

    public TileEntityAssembler() {
        super(TOTAL_SLOTS, 1, TANK_CAPACITY);
        // Preserve the capacity of worlds created before the API migration.
        this.maxEnergy = 1_100_000_000L;
    }

    @Override
    public int getModuleSlot() {
        return SLOT_MODULE;
    }

    @Override
    public int getTransformerSlot() {
        return SLOT_TRANSFORMER;
    }

    @Override
    public int getFluidSlotStart() {
        return SLOT_FLUID;
    }

    @Override
    public int getFluidSlotCount() {
        return 1;
    }

    @Override
    protected void processMachineLogic() {
        drainFluidInputSlots();

        if (currentRecipe != null || progress > 0) {
            processActiveRecipe();
            return;
        }

        // Bound expensive full recipe scans while idle; active progress still updates every tick.
        if (idleSearchDelay > 0) { idleSearchDelay--; return; }
        idleSearchDelay = 4;
        MachineRecipe matched = AssemblerRecipeHandler.INSTANCE.findRecipe(inventory, tanks);
        if (matched == null || machineTier < matched.minTier) {
            progress = 0;
            recipeEUt = 0;
            return;
        }

        long startEUt = AssemblerRecipeHandler.getOverclockedEUt(matched, machineTier);
        if (startEUt <= 0 || energy < startEUt) return;

        currentRecipe = matched;
        maxProgress = AssemblerRecipeHandler.getOverclockedDuration(matched, machineTier);
        recipeEUt = startEUt;
        AssemblerRecipeHandler.INSTANCE.consumeInputs(matched, inventory);
        consumeFluids(matched.fluidInputs);
        energy -= startEUt;
        progress = 1;
        markDirty();

        if (progress >= maxProgress) finishActiveRecipe();
    }

    private void processActiveRecipe() {
        // Old saves may contain progress without the new active-recipe tag.
        // Inputs in those saves were not consumed, so resetting is lossless.
        if (currentRecipe == null) {
            progress = 0;
            maxProgress = 200;
            recipeEUt = 0;
            markDirty();
            return;
        }
        if (!AssemblerRecipeHandler.INSTANCE.canOutput(inventory[SLOT_OUTPUT], currentRecipe.outputs[0])) return;
        if (recipeEUt <= 0) {
            recipeEUt = AssemblerRecipeHandler.getOverclockedEUt(currentRecipe, machineTier);
        }
        if (recipeEUt <= 0 || energy < recipeEUt) return;

        energy -= recipeEUt;
        progress++;
        markDirty();
        if (progress >= maxProgress) finishActiveRecipe();
    }

    private void finishActiveRecipe() {
        AssemblerRecipeHandler.INSTANCE.outputRecipe(currentRecipe, inventory);
        currentRecipe = null;
        idleSearchDelay = 0;
        progress = 0;
        recipeEUt = 0;
        markDirty();
    }

    private MachineRecipe findSavedRecipe(NBTTagCompound nbt) {
        java.util.List<MachineRecipe> recipes = AssemblerRecipeHandler.INSTANCE.getRecipeList();
        int index = nbt.hasKey("ActiveRecipeIndex") ? nbt.getInteger("ActiveRecipeIndex") : -1;
        if (index >= 0 && index < recipes.size()) {
            MachineRecipe recipe = recipes.get(index);
            if (!nbt.hasKey("ActiveRecipeOutput")) return recipe;
            ItemStack saved = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("ActiveRecipeOutput"));
            if (saved != null && recipe.outputs[0].isItemEqual(saved)
                    && ItemStack.areItemStackTagsEqual(recipe.outputs[0], saved)) return recipe;
        }
        if (nbt.hasKey("ActiveRecipeOutput")) {
            ItemStack saved = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("ActiveRecipeOutput"));
            for (MachineRecipe recipe : recipes) {
                if (saved != null && recipe.outputs[0].isItemEqual(saved)
                        && ItemStack.areItemStackTagsEqual(recipe.outputs[0], saved)) return recipe;
            }
        }
        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT) return false;
        if (slot == SLOT_FLUID) {
            return stack != null && (stack.getItem() instanceof IFluidContainerItem
                    || FluidContainerRegistry.isContainer(stack));
        }
        if (slot == SLOT_MODULE) return isTierModule(stack);
        if (slot == SLOT_TRANSFORMER) return isTransformerModule(stack);
        if (slot == 13) return stack != null && stack.getItem() instanceof IElectricItem;
        return slot >= 0 && slot < 9 && super.isItemValidForSlot(slot, stack);
    }

    private static boolean isTierModule(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        return item == Tmodules.TierModuleMV || item == Tmodules.TierModuleHV
                || item == Tmodules.TierModuleEV || item == Tmodules.TierModuleIV
                || item == Tmodules.TierModuleLuV || item == Tmodules.TierModuleZPM
                || item == Tmodules.TierModuleUV || item == Tmodules.TierModulePUV1
                || item == Tmodules.TierModuleUX || item == Tmodules.TierModuleOLV
                || item == Tmodules.TierModuleOMV || item == Tmodules.TierModuleOHV
                || item == Tmodules.TierModuleOEV || item == Tmodules.TierModuleOIV
                || item == Tmodules.TierModuleMAX;
    }

    private static boolean isTransformerModule(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        return item == Tmodules.LowLevelTransformer || item == Tmodules.MediumLevelTransformer
                || item == Tmodules.HighLevelTransformer || item == Tmodules.UltimateLevelTransformer
                || item == Tmodules.OmegaTransformer;
    }

    @Override
    public String getInventoryName() {
        return "Assembler";
    }

    public String getTierName() {
        switch (machineTier) {
            case 2: return "MV";
            case 3: return "HV";
            case 4: return "EV";
            case 5: return "IV";
            case 6: return "LuV";
            case 7: return "ZPM";
            case 8: return "UV";
            case 9: return "PUV1";
            case 10: return "UX";
            case 11: return "OLV";
            case 12: return "OMV";
            case 13: return "OHV";
            case 14: return "OEV";
            case 15: return "OIV";
            case 16: return "MAX";
            default: return "LV";
        }
    }

    public int getTierColor() {
        switch (machineTier) {
            case 2: return 0x00AAFF;
            case 3: return 0xFFFF00;
            case 4: return 0xFF8800;
            case 5: return 0xFF0000;
            case 6: return 0xAA00FF;
            case 7: return 0xFF00FF;
            case 8: return 0x00FFFF;
            default: return 0xAAAAAA;
        }
    }

    @Override
    public ModularScreen createScreen(GuiCreationContext context, ModularPanel panel) {
        return new ModularScreen(panel);
    }

    @Override
    public ModularPanel buildUI(GuiCreationContext context, GuiSyncManager syncManager, boolean isClient) {
        com.mod.htgt6.common.network.MachineUiTransfers.bind(syncManager, this, isClient);
        syncManager.registerSlotGroup("assembler", 14);
        syncManager.registerSlotGroup("assembler_preview", 1);
        syncManager.syncValue("assembler_energy", SyncHandlers.longNumber(() -> energy, value -> energy = value));
        syncManager.syncValue("assembler_progress", SyncHandlers.intNumber(() -> progress, value -> progress = value));
        syncManager.syncValue("assembler_max_progress", SyncHandlers.intNumber(() -> maxProgress, value -> maxProgress = value));
        syncManager.syncValue("assembler_tier", SyncHandlers.intNumber(() -> machineTier, value -> machineTier = value));
        syncManager.syncValue("assembler_enabled", SyncHandlers.bool(() -> enabled, value -> enabled = value));
        FluidSlotSyncHandler tankSync = SyncHandlers.fluidSlot(tanks[0])
                .canFillSlot(true).canDrainSlot(true);
        syncManager.syncValue("assembler_tank", tankSync);

        MachineUiTheme theme = MachineUiTheme.getActive();
        ModularPanel panel = ModularPanel.defaultPanel("assembler", 240, 295)
                .background(new Rectangle().setColor(theme.getPanelColor()));
        addPanelFrame(panel, theme.getFrameColor());

        panel.child(new TextWidget("ASSEMBLER").pos(13, 8).color(theme.getTitleColor()));
        panel.child(createInfoButton().pos(178, 6));
        if (isClient) {
            panel.child(com.mod.htgt6.client.GUI.AssemblerModularUiButtons
                    .createNeiButton(this).pos(196, 6));
        }

        // Live production terminal requested for the PNG replacement.
        Widget productionTerminal = new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        productionTerminal.pos(8, 24).size(224, 42);
        panel.child(productionTerminal);
        panel.child(createPreviewSlot().pos(14, 36));
        panel.child(new TextWidget(IKey.dynamic(this::getPreviewName))
                .pos(39, 31).size(187, 11).color(theme.getPrimaryColor()));
        panel.child(new TextWidget(IKey.dynamic(this::getRemainingTimeText))
                .pos(39, 48).size(187, 11).color(theme.getProductionColor()));

        // Slot geometry follows the old PNG: shaped 3x3 matrix, arrow, output,
        // then fluid and machine upgrade controls on the right.
        Widget machineArea = new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        machineArea.pos(8, 72).size(224, 82);
        panel.child(machineArea);
        panel.child(new TextWidget("INPUT MATRIX").pos(16, 78).color(theme.getLabelColor()));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = row * 3 + col;
                panel.child(createItemSlot(slot, true, true).pos(16 + col * 18, 94 + row * 18));
            }
        }
        panel.child(new TextWidget(IKey.str(">>")).pos(75, 113).color(theme.getProductionColor()));
        panel.child(new TextWidget("OUTPUT").pos(92, 78).color(theme.getLabelColor()));
        panel.child(createItemSlot(SLOT_OUTPUT, false, true).pos(101, 112));

        panel.child(new TextWidget("TANK / MODIFIERS").pos(128, 78).color(theme.getLabelColor()));
        if (isClient) {
            com.mod.htgt6.client.GUI.AssemblerFluidSlot tank =
                    new com.mod.htgt6.client.GUI.AssemblerFluidSlot(this);
            tank.syncHandler("assembler_tank");
            tank.background(GuiTextures.SLOT_DARK).pos(128, 92).size(26, 44);
            panel.child(tank);
        } else {
            panel.child(new FluidSlot().syncHandler("assembler_tank").pos(128, 92).size(26, 44));
        }
        panel.child(createMachineSlot(SLOT_FLUID, "CANISTER").pos(158, 108));
        panel.child(createMachineSlot(SLOT_MODULE, "TIER MODULE").pos(176, 108));
        panel.child(createMachineSlot(SLOT_TRANSFORMER, "TRANSFORMER").pos(194, 108));
        panel.child(createMachineSlot(13, "ENERGY ITEM").pos(212, 108));
        panel.child(new TextWidget("CAN").pos(158, 132).color(theme.getLabelColor()));
        panel.child(new TextWidget("T").pos(182, 132).color(theme.getPrimaryColor()));
        panel.child(new TextWidget("TR").pos(197, 132).color(theme.getProductionColor()));
        panel.child(new TextWidget("EU").pos(214, 132).color(theme.getEnergyColor()));

        Widget statusTerminal = new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        statusTerminal.pos(8, 160).size(224, 38);
        panel.child(statusTerminal);
        panel.child(new TextWidget(IKey.dynamic(() -> "BUFFER: " + SingleblockUiText.number(energy) + " / " + SingleblockUiText.number(maxEnergy) + " EU"))
                .pos(14, 168).size(212, 11).color(theme.getEnergyColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> (enabled ? "STATUS: ONLINE" : "STATUS: OFFLINE")
                + "   TIER: " + getTierName()))
                .pos(14, 183).size(212, 11).color(theme.getLabelColor()));

        panel.child(SlotGroupWidget.playerInventory().pos(39, 207));
        return panel;
    }

    private String getPreviewName() {
        ItemStack preview = getRecipePreview();
        return preview == null ? "OUTPUT: IDLE" : SingleblockUiText.label("OUTPUT: " + preview.getDisplayName());
    }

    private String getRemainingTimeText() {
        if (currentRecipe == null && progress <= 0) return "TIME LEFT: IDLE";
        int remaining = Math.max(0, maxProgress - progress);
        return String.format("TIME LEFT: %.1f s", remaining / 20.0D);
    }

    private ItemStack getRecipePreview() {
        if (worldObj != null && worldObj.isRemote) return syncedRecipePreview;
        return currentRecipe == null || currentRecipe.outputs.length == 0 ? null : currentRecipe.outputs[0];
    }

    private ItemSlot createPreviewSlot() {
        ModularSlot slot = SyncHandlers.itemSlot(recipePreview, 0)
                .slotGroup("assembler_preview").accessibility(false, false);
        return new ItemSlot().slot(slot);
    }

    private ItemSlot createItemSlot(int index, boolean canPut, boolean canTake) {
        ModularSlot slot = SyncHandlers.itemSlot(modularInventory, index)
                .slotGroup("assembler").accessibility(canPut, canTake);
        return new ItemSlot().slot(slot).background(GuiTextures.SLOT_DARK);
    }

    private ItemSlot createMachineSlot(int index, final String label) {
        ItemSlot slot = createItemSlot(index, true, true);
        slot.tooltip(t -> {
            t.addLine(label);
            if (index == SLOT_MODULE) t.addLine("Controls the machine recipe tier");
            if (index == SLOT_TRANSFORMER) t.addLine("Unlocks the tier module range");
        });
        return slot;
    }

    private ButtonWidget<?> createInfoButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(12, 12).background(GuiTextures.BUTTON).overlay(IKey.str("i"));
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine("Assembler diagnostics");
            t.addLine(getRemainingTimeText());
            t.addLine("Energy: " + energy + " / " + maxEnergy + " EU");
            t.addLine("Input: " + getMaxInputVoltage() + " EU/t");
            t.addLine("Tier: " + machineTier + " (" + getTierName() + ")");
            t.addLine("Tank: " + tanks[0].getFluidAmount() + " / " + tanks[0].getCapacity() + " mB");
        });
        return button;
    }

    private static void addPanelFrame(ModularPanel panel, int color) {
        frame(panel, color, 0, 0, 240, 1);
        frame(panel, color, 0, 294, 240, 1);
        frame(panel, color, 0, 0, 1, 295);
        frame(panel, color, 239, 0, 1, 295);
    }

    private static void frame(ModularPanel panel, int color, int x, int y, int width, int height) {
        Widget edge = new Widget().background(new Rectangle().setColor(color));
        edge.pos(x, y).size(width, height);
        panel.child(edge);
    }

    private static final class AssemblerInventory implements IItemHandlerModifiable {
        private final TileEntityAssembler assembler;
        private AssemblerInventory(TileEntityAssembler assembler) { this.assembler = assembler; }
        @Override public int getSlots() { return 14; }
        @Override public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots() ? assembler.inventory[slot] : null;
        }
        @Override public int getSlotLimit(int slot) {
            return slot == SLOT_MODULE || slot == SLOT_TRANSFORMER || slot == 13
                    ? 1 : assembler.getInventoryStackLimit();
        }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (slot >= 0 && slot < getSlots()) assembler.setInventorySlotContents(slot, stack);
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < getSlots() && assembler.isItemValidForSlot(slot, stack);
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack == null || !isItemValid(slot, stack)) return stack;
            ItemStack current = getStackInSlot(slot);
            if (current != null && (!current.isItemEqual(stack)
                    || !ItemStack.areItemStackTagsEqual(current, stack))) return stack;
            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - (current == null ? 0 : current.stackSize);
            int moved = Math.min(stack.stackSize, Math.max(0, limit));
            if (moved <= 0) return stack;
            if (!simulate) {
                if (current == null) {
                    ItemStack inserted = stack.copy(); inserted.stackSize = moved;
                    setStackInSlot(slot, inserted);
                } else {
                    current.stackSize += moved; assembler.markDirty();
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
                if (current.stackSize <= 0) setStackInSlot(slot, null);
                else assembler.markDirty();
            }
            return result;
        }
    }

    private static final class RecipePreviewInventory implements IItemHandlerModifiable {
        private final TileEntityAssembler assembler;
        private RecipePreviewInventory(TileEntityAssembler assembler) { this.assembler = assembler; }
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? assembler.getRecipePreview() : null; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (slot == 0) assembler.syncedRecipePreview = stack == null ? null : stack.copy();
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return null; }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        recipeEUt = nbt.hasKey("RecipeEUtLong") ? nbt.getLong("RecipeEUtLong") : nbt.getInteger("RecipeEUt");
        currentRecipe = progress > 0 ? findSavedRecipe(nbt) : null;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("RecipeEUtLong", recipeEUt);
        nbt.setInteger("RecipeEUt", (int) Math.min(Integer.MAX_VALUE, recipeEUt));
        if (progress > 0 && currentRecipe != null) {
            nbt.setInteger("ActiveRecipeIndex", AssemblerRecipeHandler.INSTANCE.getRecipeList().indexOf(currentRecipe));
            NBTTagCompound recipeTag = new NBTTagCompound();
            currentRecipe.outputs[0].writeToNBT(recipeTag);
            nbt.setTag("ActiveRecipeOutput", recipeTag);
        }
    }
}
