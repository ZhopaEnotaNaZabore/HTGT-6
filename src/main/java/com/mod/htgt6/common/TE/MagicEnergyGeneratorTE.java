package com.mod.htgt6.common.TE;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;

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
import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseGenerator;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import com.mod.htgt6.common.ui.MachineUiTheme;
import com.mod.htgt6.common.ui.SingleblockUiText;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Solid magic-fuel generator.  A consumed item becomes a finite EU reserve;
 * the reserve is emitted at the selected machine tier, never above 1,000,000
 * EU/t.  Therefore powerful items last longer in an LV machine instead of
 * bypassing its voltage limit.
 */
public class MagicEnergyGeneratorTE extends TileEntityBaseGenerator implements IGuiHolder {

    public static final long MAX_GENERATION_PER_TICK = 1_000_000L;
    private static final int WILDCARD_META = 32767;
    private static final Map<String, Long> MAGIC_FUELS = new LinkedHashMap<String, Long>();
    private static final Map<String, ItemStack> MAGIC_FUEL_DISPLAY_STACKS = new LinkedHashMap<String, ItemStack>();

    static {
        // The enchanted apple remains a distinct, high-value metadata variant.
        addMagicFuel(new ItemStack(Items.golden_apple, 1, 1), 5_000_000L);
    }

    private long remainingFuelEnergy;
    private long activeFuelTotalEnergy;
    private long currentEnergyProduction;
    private String activeFuelName = "";
    private String syncedFuelDisplay = "EMPTY";
    private String syncedBurnTime = "BURN TIME: 0/0 s";
    private final IItemHandlerModifiable modularInventory = new MagicInventory(this);

    public MagicEnergyGeneratorTE() {
        super(4, 1536870912D);
    }

    /** Registers all metadata variants of an item without changing the supplied item. */
    public static void addMagicFuel(Item item, double totalEuValue) {
        if (item != null) addMagicFuel(new ItemStack(item, 1, WILDCARD_META), Math.round(totalEuValue));
    }

    /** Registers an exact GT6 MetaItem/OP stack.  The caller stack is never consumed or altered. */
    public static void addMagicFuel(ItemStack stack, long totalEuValue) {
        if (stack == null || stack.getItem() == null || totalEuValue <= 0L) return;
        String key = fuelKey(stack.getItem(), stack.getItemDamage());
        MAGIC_FUELS.put(key, totalEuValue);
        ItemStack display = stack.copy();
        display.stackSize = 1;
        MAGIC_FUEL_DISPLAY_STACKS.put(key, display);
    }

    public static boolean isMagicFuel(ItemStack stack) { return getConfiguredFuelValue(stack) > 0L || getFuelPlan(stack) != null; }
    public static long getMagicFuelEu(ItemStack stack) {
        FuelPlan plan = getFuelPlan(stack);
        return plan == null ? 0L : plan.energy;
    }

    public static List<RegisteredMagicFuel> getRegisteredMagicFuels() {
        List<RegisteredMagicFuel> result = new ArrayList<RegisteredMagicFuel>();
        for (Map.Entry<String, Long> entry : MAGIC_FUELS.entrySet()) {
            ItemStack stack = MAGIC_FUEL_DISPLAY_STACKS.get(entry.getKey());
            if (stack != null) result.add(new RegisteredMagicFuel(stack.copy(), entry.getValue()));
        }
        return result;
    }

    public static final class RegisteredMagicFuel {
        public final ItemStack stack;
        public final long totalEu;
        private RegisteredMagicFuel(ItemStack stack, long totalEu) { this.stack = stack; this.totalEu = totalEu; }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;
        updateMachineTier();

        currentEnergyProduction = 0L;
        if (enabled && remainingFuelEnergy <= 0L) processMagicInput();
        if (enabled) generateEnergy();
        refreshTerminalText();
    }

    private void processMagicInput() {
        ItemStack input = inventory[0];
        FuelPlan plan = getFuelPlan(input);
        if (plan == null || plan.energy <= 0L || !canOutput(plan.returnedStack)) return;

        remainingFuelEnergy = plan.energy;
        activeFuelTotalEnergy = plan.energy;
        activeFuelName = input.getDisplayName();
        if (plan.returnedStack != null) addOutput(plan.returnedStack);
        input.stackSize--;
        if (input.stackSize <= 0) inventory[0] = null;
        markDirty();
    }

    private void generateEnergy() {
        if (remainingFuelEnergy <= 0L) return;
        long freeBuffer = (long) Math.floor(maxEnergy - energy);
        long tierOutput = Math.min(MAX_GENERATION_PER_TICK, getOutputVoltage());
        long generated = Math.min(remainingFuelEnergy, Math.min(tierOutput, freeBuffer));
        if (generated <= 0L) return;
        remainingFuelEnergy -= generated;
        currentEnergyProduction = (long) addGeneratedEnergy(generated);
        markDirty();
    }

    /**
     * A GT6 item is protected only if it actually carries enchantments.  The
     * former all-GregTech blacklist rejected every MetaItem and OP material.
     */
    private static FuelPlan getFuelPlan(ItemStack stack) {
        if (stack == null || stack.getItem() == null || isProtectedGregTechEnchanted(stack)) return null;
        long configured = getConfiguredFuelValue(stack);
        if (configured > 0L) return new FuelPlan(configured, null);

        if (stack.getItem() == Items.enchanted_book) {
            int levels = enchantmentLevels(((ItemEnchantedBook) stack.getItem()).func_92110_g(stack));
            return levels > 0 ? new FuelPlan(levels * 25000L, null) : null;
        }
        if (stack.getItem() == Items.potionitem) {
            List<?> effects = Items.potionitem.getEffects(stack);
            if (effects == null || effects.isEmpty()) return null;
            int maxDuration = 0;
            for (Object effect : effects) if (effect instanceof PotionEffect) maxDuration = Math.max(maxDuration, ((PotionEffect) effect).getDuration());
            long value = maxDuration < 2400 ? 16000L : maxDuration < 6000 ? 24000L : 40000L;
            return new FuelPlan(value, new ItemStack(Items.glass_bottle));
        }
        if (!stack.isItemEnchanted()) return null;

        int levels = 0;
        for (Object level : EnchantmentHelper.getEnchantments(stack).values()) if (level instanceof Integer) levels += (Integer) level;
        if (levels <= 0) return null;

        ItemStack returned = stack.copy();
        returned.stackSize = 1;
        if (returned.getTagCompound() != null) {
            returned.getTagCompound().removeTag("ench");
            returned.getTagCompound().removeTag("StoredEnchantments");
            if (returned.getTagCompound().hasNoTags()) returned.setTagCompound(null);
        }
        // Never burn an item unless its returned copy is genuinely unenchanted.
        if (returned.isItemEnchanted() || hasStoredEnchantments(returned)) return null;
        return new FuelPlan(levels * 25000L, returned);
    }

    private static boolean isProtectedGregTechEnchanted(ItemStack stack) {
        cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier id = cpw.mods.fml.common.registry.GameRegistry.findUniqueIdentifierFor(stack.getItem());
        return id != null && "gregtech".equals(id.modId) && (stack.isItemEnchanted() || hasStoredEnchantments(stack));
    }

    private static boolean hasStoredEnchantments(ItemStack stack) {
        return stack != null && stack.getTagCompound() != null && stack.getTagCompound().hasKey("StoredEnchantments", 9);
    }

    private static int enchantmentLevels(NBTTagList enchantments) {
        int levels = 0;
        if (enchantments != null) for (int i = 0; i < enchantments.tagCount(); i++) levels += enchantments.getCompoundTagAt(i).getShort("lvl");
        return levels;
    }

    private static long getConfiguredFuelValue(ItemStack stack) {
        if (stack == null) return 0L;
        Long value = MAGIC_FUELS.get(fuelKey(stack.getItem(), stack.getItemDamage()));
        if (value == null) value = MAGIC_FUELS.get(fuelKey(stack.getItem(), WILDCARD_META));
        return value == null ? 0L : value;
    }

    private static String fuelKey(Item item, int metadata) {
        return String.valueOf(Item.itemRegistry.getNameForObject(item)) + "@" + metadata;
    }

    private boolean canOutput(ItemStack stack) {
        if (stack == null) return true;
        ItemStack current = inventory[1];
        return current == null || (current.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(current, stack)
                && current.stackSize + stack.stackSize <= current.getMaxStackSize());
    }

    private void addOutput(ItemStack stack) {
        if (inventory[1] == null) inventory[1] = stack.copy(); else inventory[1].stackSize += stack.stackSize;
    }

    private void updateMachineTier() {
        ItemStack transformer = inventory[3], module = inventory[2];
        machineTier = MachineTierSystem.resolveMachineTier(module, transformer);
    }

    private static int getModuleTier(Item item) {
        if (item == Tmodules.TierModuleMV) return 2; if (item == Tmodules.TierModuleHV) return 3;
        if (item == Tmodules.TierModuleEV) return 4; if (item == Tmodules.TierModuleIV) return 5;
        if (item == Tmodules.TierModuleLuV) return 6; if (item == Tmodules.TierModuleZPM) return 7;
        if (item == Tmodules.TierModuleUV) return 8; if (item == Tmodules.TierModulePUV1) return 9;
        if (item == Tmodules.TierModuleUX) return 10; if (item == Tmodules.TierModuleOLV) return 11;
        if (item == Tmodules.TierModuleOMV) return 12; if (item == Tmodules.TierModuleOHV) return 13;
        if (item == Tmodules.TierModuleOEV) return 14; if (item == Tmodules.TierModuleOIV) return 15;
        return item == Tmodules.TierModuleMAX ? 16 : 0;
    }

    private void refreshTerminalText() { syncedFuelDisplay = createFuelDisplay(); syncedBurnTime = createBurnTime(); }
    private String getFuelDisplay() { return worldObj != null && worldObj.isRemote ? syncedFuelDisplay : createFuelDisplay(); }
    private String getBurnTime() { return worldObj != null && worldObj.isRemote ? syncedBurnTime : createBurnTime(); }
    private String createFuelDisplay() { return remainingFuelEnergy > 0L ? activeFuelName : "EMPTY"; }
    private String createBurnTime() {
        long euPerSecond = Math.max(1L, Math.min(MAX_GENERATION_PER_TICK, getOutputVoltage()) * 20L);
        long remaining = (remainingFuelEnergy + euPerSecond - 1L) / euPerSecond;
        long total = (activeFuelTotalEnergy + euPerSecond - 1L) / euPerSecond;
        return "BURN TIME: " + remaining + "/" + total + " s";
    }

    @Override public String getInventoryName() { return "MagicEnergyGenerator"; }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot != 0 || isMagicFuel(stack); }
    @Override public ModularScreen createScreen(GuiCreationContext context, ModularPanel panel) { return new ModularScreen(panel); }

    @Override
    public ModularPanel buildUI(GuiCreationContext context, GuiSyncManager syncManager, boolean isClient) {
        syncManager.syncValue("enabled", SyncHandlers.bool(() -> enabled, value -> enabled = value));
        syncManager.registerSlotGroup("magic", 4);
        syncManager.syncValue("energy", SyncHandlers.longNumber(() -> (long) energy, value -> energy = value));
        syncManager.syncValue("tier", SyncHandlers.intNumber(() -> machineTier, value -> machineTier = value));
        syncManager.syncValue("current_generation", SyncHandlers.longNumber(() -> currentEnergyProduction, value -> currentEnergyProduction = value));
        syncManager.syncValue("fuel_display", SyncHandlers.string(() -> syncedFuelDisplay, value -> syncedFuelDisplay = value));
        syncManager.syncValue("burn_time", SyncHandlers.string(() -> syncedBurnTime, value -> syncedBurnTime = value));
        MachineUiTheme theme = MachineUiTheme.getActive();
        ModularPanel panel = ModularPanel.defaultPanel("magic_energy_generator", 240, 267).background(new Rectangle().setColor(theme.getPanelColor()));
        addPanelFrame(panel, theme.getFrameColor());
        Widget terminal = new Widget().background(new Rectangle().setColor(theme.getTerminalColor())); terminal.pos(8, 24).size(224, 92); panel.child(terminal);
        panel.child(new TextWidget("MAGIC ENERGY GENERATOR").pos(13, 8).color(theme.getTitleColor()));
        panel.child(createInfoButton().pos(178, 6));
        if (isClient) panel.child(com.mod.htgt6.client.GUI.MagicModularUiButtons.createNeiButton(this).pos(196, 6));
        panel.child(new TextWidget(IKey.dynamic(() -> SingleblockUiText.label("FUEL: " + getFuelDisplay()))).pos(14, 35).size(208, 11).color(theme.getPrimaryColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "GENERATING: " + SingleblockUiText.number(currentEnergyProduction) + " EU/t")).pos(14, 55).size(208, 11).color(theme.getProductionColor()));
        panel.child(new TextWidget(IKey.dynamic(this::getBurnTime)).pos(14, 76).size(208, 11).color(theme.getLabelColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> "BUFFER: " + SingleblockUiText.number((long) energy) + " EU")).pos(14, 97).size(208, 11).color(theme.getEnergyColor()));
        panel.child(new TextWidget("FUEL INPUT").pos(16, 123).color(theme.getLabelColor()));
        panel.child(createItemSlot(0, true, true).pos(17, 138));
        panel.child(new TextWidget("RETURN").pos(51, 123).color(theme.getLabelColor()));
        // ModularSlot.accessibility is (canPut, canTake): output must be extract-only.
        panel.child(createItemSlot(1, false, true).pos(56, 138));
        panel.child(new TextWidget("MODIFIERS").pos(184, 123).color(theme.getLabelColor()));
        panel.child(createItemSlot(2, true, true).pos(186, 138)); panel.child(createItemSlot(3, true, true).pos(208, 138));
        panel.child(SlotGroupWidget.playerInventory().pos(39, 183));
        return panel;
    }

    private ButtonWidget<?> createInfoButton() {
        ButtonWidget<?> button = new ButtonWidget(); button.size(12, 12).background(GuiTextures.BUTTON).overlay(IKey.str("i"));
        button.tooltip(t -> { t.setUpdateTooltipEveryTick(true); t.addLine("Magic Energy Generator status"); t.addLine("Fuel: " + getFuelDisplay()); t.addLine("Generating: " + currentEnergyProduction + " EU/t"); t.addLine(getBurnTime()); t.addLine("Buffer: " + (long) energy + " / " + (long) maxEnergy + " EU"); t.addLine("Machine tier: " + machineTier + " (" + getOutputVoltage() + " EU/t)"); });
        return button;
    }

    private ItemSlot createItemSlot(int index, boolean canPut, boolean canTake) {
        ModularSlot slot = SyncHandlers.itemSlot(modularInventory, index).slotGroup("magic").accessibility(canPut, canTake);
        return new ItemSlot().slot(slot);
    }

    private static void addPanelFrame(ModularPanel panel, int color) {
        frame(panel, color, 0, 0, 240, 1); frame(panel, color, 0, 266, 240, 1); frame(panel, color, 0, 0, 1, 267); frame(panel, color, 239, 0, 1, 267);
    }
    private static void frame(ModularPanel panel, int color, int x, int y, int width, int height) { Widget edge = new Widget(); edge.background(new Rectangle().setColor(color)).pos(x, y).size(width, height); panel.child(edge); }

    @Override public void readFromNBT(NBTTagCompound nbt) { super.readFromNBT(nbt); remainingFuelEnergy = nbt.hasKey("RemainingMagicFuelEnergy") ? nbt.getLong("RemainingMagicFuelEnergy") : (long) nbt.getDouble("RemainingFuelEnergy"); activeFuelTotalEnergy = nbt.getLong("ActiveMagicFuelTotalEnergy"); activeFuelName = nbt.getString("ActiveMagicFuelName"); refreshTerminalText(); }
    @Override public void writeToNBT(NBTTagCompound nbt) { super.writeToNBT(nbt); nbt.setLong("RemainingMagicFuelEnergy", remainingFuelEnergy); nbt.setLong("ActiveMagicFuelTotalEnergy", activeFuelTotalEnergy); nbt.setString("ActiveMagicFuelName", activeFuelName); }

    private static final class FuelPlan { final long energy; final ItemStack returnedStack; FuelPlan(long energy, ItemStack returnedStack) { this.energy = energy; this.returnedStack = returnedStack; } }
    private static final class MagicInventory implements IItemHandlerModifiable {
        private final MagicEnergyGeneratorTE generator; MagicInventory(MagicEnergyGeneratorTE generator) { this.generator = generator; }
        @Override public int getSlots() { return generator.inventory.length; }
        @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < getSlots() ? generator.inventory[slot] : null; }
        @Override public int getSlotLimit(int slot) { return slot >= 2 ? 1 : generator.getInventoryStackLimit(); }
        @Override public void setStackInSlot(int slot, ItemStack stack) { if (slot >= 0 && slot < getSlots()) generator.setInventorySlotContents(slot, stack); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack != null && (slot == 0 ? isMagicFuel(stack) : slot == 2 || slot == 3); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { if (!isItemValid(slot, stack)) return stack; ItemStack current = getStackInSlot(slot); if (current != null && (!current.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(current, stack))) return stack; int moved = Math.min(stack.stackSize, Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - (current == null ? 0 : current.stackSize)); if (moved <= 0) return stack; if (!simulate) { if (current == null) { ItemStack copy = stack.copy(); copy.stackSize = moved; generator.setInventorySlotContents(slot, copy); } else { current.stackSize += moved; generator.markDirty(); } } if (moved == stack.stackSize) return null; ItemStack remaining = stack.copy(); remaining.stackSize -= moved; return remaining; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { ItemStack current = getStackInSlot(slot); if (current == null || amount <= 0) return null; ItemStack result = current.copy(); result.stackSize = Math.min(amount, current.stackSize); if (!simulate) { current.stackSize -= result.stackSize; if (current.stackSize <= 0) generator.setInventorySlotContents(slot, null); else generator.markDirty(); } return result; }
    }
}
