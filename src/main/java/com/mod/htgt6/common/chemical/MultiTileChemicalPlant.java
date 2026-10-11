package com.mod.htgt6.common.chemical;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.future.IItemHandlerModifiable;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.MultiTileEntityBase;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.mining.MiningUnitContent;
import com.mod.htgt6.common.ui.MachineUiTheme;
import gregapi.data.IL;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reusable HTGT6 Chemical Plant controller. Structure, inherited tier, detachable
 * gateways and tier-scaled tanks are supplied by the shared MultiTile constructor API.
 */
public class MultiTileChemicalPlant extends MultiTileEntityBase implements IGuiHolder {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 13;
    public static final int DEPTH = 9;

    public static final int ELEMENT_AIR = 0;
    public static final int ELEMENT_CONTROLLER = 1;
    public static final int ELEMENT_FLEXIBLE_BASE = 2;
    public static final int ELEMENT_CHEMICAL_CASING = 3;
    public static final int ELEMENT_THERMOCHEMICAL_CASING = 4;
    public static final int ELEMENT_FRAME = 5;
    public static final int ELEMENT_GLASS = 6;

    public static final int SLOT_MODULE = 0;
    public static final int SLOT_TRANSFORMER = 1;
    public static final int INPUT_SLOT_START = 2;
    public static final int SLOT_COUNT_PER_BUFFER = 32;
    public static final int OUTPUT_SLOT_START = INPUT_SLOT_START + SLOT_COUNT_PER_BUFFER;
    private static final int[] INPUT_GATEWAY_SLOTS = makeSlotRange(INPUT_SLOT_START, SLOT_COUNT_PER_BUFFER);
    private static final int[] OUTPUT_GATEWAY_SLOTS = makeSlotRange(OUTPUT_SLOT_START, SLOT_COUNT_PER_BUFFER);

    private final ChemicalFluidBuffer inputTank = new ChemicalFluidBuffer(GatewayType.FLUID_INPUT);
    private final ChemicalFluidBuffer outputTank = new ChemicalFluidBuffer(GatewayType.FLUID_OUTPUT);
    private final IFluidHandler inputGateway = new InputFluidGateway();
    private final IFluidHandler outputGateway = new OutputFluidGateway();
    private final IItemHandlerModifiable modifierInventory = new ModifierInventory(this);
    private static final UITexture GT5_POWER_ON = UITexture.fullImage("htgt6", "textures/gui/overlays/gt5_power_switch_on.png");
    private static final UITexture GT5_POWER_OFF = UITexture.fullImage("htgt6", "textures/gui/overlays/gt5_power_switch_off.png");
    private static final UITexture GT5_RESTART = UITexture.fullImage("htgt6", "textures/gui/overlays/gt5_structure_update.png");
    private static final UITexture GT5_ENERGY_PANEL = UITexture.fullImage("htgt6", "textures/gui/overlays/gt5_power_panel.png");
    private long currentEnergyUsage;
    /** Auxiliary mechanical drives report their real draw without changing recipe accounting. */
    protected final void addAuxiliaryEnergyUsage(long usage) { currentEnergyUsage += Math.max(0L, usage); }
    protected final boolean hasPreparedRecipe() { return currentRecipe != null || !pendingItemOutputs.isEmpty() || !pendingFluidOutputs.isEmpty(); }
    private boolean energyPanelOpen;
    private ChemicalPlantRecipe currentRecipe;
    private final List<ItemStack> pendingItemOutputs = new ArrayList<ItemStack>();
    private final List<FluidStack> pendingFluidOutputs = new ArrayList<FluidStack>();
    private boolean processing;
    private String currentRecipeName = "";
    private int requiredRecipeTier;
    private int currentParallel = 1;
    private long clientProgressSyncTick = -1L;
    private ItemStack[] clientRecipeInputs;
    private ItemStack clientRecipeOutput;
    /** -1 disables configured recipes; 0-24 selects the matching virtual GT6 Selector Tag. */
    private int selectorSetting = -1;

    public MultiTileChemicalPlant() {
        super(OUTPUT_SLOT_START + SLOT_COUNT_PER_BUFFER);
        configureFluidGateway(GatewayType.FLUID_INPUT, inputGateway);
        configureFluidGateway(GatewayType.FLUID_OUTPUT, outputGateway);
        configureItemGateway(GatewayType.ITEM_INPUT, this, INPUT_GATEWAY_SLOTS);
        configureItemGateway(GatewayType.ITEM_OUTPUT, this, OUTPUT_GATEWAY_SLOTS);
        maxEnergy = 1_100_000_000_000_000_000L;
    }

    @Override public int getModuleSlot() { return SLOT_MODULE; }
    @Override public int getTransformerSlot() { return SLOT_TRANSFORMER; }
    @Override public String getInventoryName() { return StatCollector.translateToLocal("tile.multi_tile_chemical_plant.name"); }
    protected List<ChemicalPlantRecipe> getMachineRecipes() {
        return ChemicalPlantRecipeHandler.INSTANCE.getRecipeList();
    }
    protected String getPanelId() { return "multi_tile_chemical_plant"; }
    protected String getTitleKey() { return "tile.multi_tile_chemical_plant.name"; }
    protected ItemStack getControllerDisplayStack() { return new ItemStack(ChemicalPlantContent.controller, 1, 0); }
    protected String getStructureTooltipKey() { return "ui.htgt6.chemical_plant.nei_structure"; }
    protected String getRecipeOverlayId() { return com.mod.htgt6.client.nei.ChemicalPlantNEIHandler.OVERLAY_ID; }
    protected String getRecipeTooltipKey() { return "ui.htgt6.chemical_plant.nei_recipes"; }
    protected String getUiKey(String suffix) { return "ui.htgt6.chemical_plant." + suffix; }
    @Override protected Block resolveGatewayCasingBlock() { return ChemicalPlantContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return BlockChemicalPlantCasing.STANDARD; }
    @Override protected IFluidHandler resolveFluidGateway(
            GatewayType type, int gatewayX, int gatewayY, int gatewayZ) {
        int[] range = getStructureFluidTankRange(type, gatewayX, gatewayY, gatewayZ);
        if (range[1] <= 0) return null;
        if (type == GatewayType.FLUID_INPUT) return new GatewayFluidView(inputTank, range[0], range[1], true);
        if (type == GatewayType.FLUID_OUTPUT) return new GatewayFluidView(outputTank, range[0], range[1], false);
        return super.resolveFluidGateway(type, gatewayX, gatewayY, gatewayZ);
    }

    @Override public IFluidHandler getGatewayFluidTankHandler(GatewayType type, int x, int y, int z, int tank) {
        int[] range = getStructureFluidTankRange(type, x, y, z);
        if (tank < 0 || tank >= range[1]) return null;
        if (type == GatewayType.FLUID_INPUT) return new GatewayFluidView(inputTank, range[0] + tank, 1, true);
        if (type == GatewayType.FLUID_OUTPUT) return new GatewayFluidView(outputTank, range[0] + tank, 1, false);
        return null;
    }

    @Override protected void processMachineLogic() {
        if (!structureFormed) {
            stopProcessing(false);
            currentEnergyUsage = 0L;
            return;
        }
        if (currentRecipe == null) {
            ChemicalPlantRecipe recipe = findRecipe();
            if (recipe == null) {
                processing = false;
                progress = 0;
                currentEnergyUsage = 0L;
                requiredRecipeTier = findBlockedRecipeTier();
                diagnostic = requiredRecipeTier > machineTier ? "WAITING: RECIPE TIER" : "WAITING: RECIPE INPUT";
                return;
            }
            requiredRecipeTier = 0;
            if (!prepareRecipe(recipe)) return;
        }
        processing = true;

        long usage = getRecipeEUt(currentRecipe, currentParallel);
        if (energy < usage) {
            boolean wasRunning = !"WAITING: ENERGY".equals(diagnostic);
            currentEnergyUsage = 0L;
            diagnostic = "WAITING: ENERGY";
            if (wasRunning) markControllerDirtyAndSync();
            return;
        }
        energy -= usage;
        currentEnergyUsage = usage;
        diagnostic = "PROCESSING: " + currentRecipeName;
        if (++progress >= maxProgress) completeRecipe();
        else if (progress % 10 == 0) markControllerDirtyAndSync();
    }

    /** Used by the client renderer; an assembled but stopped plant stays motionless. */
    public boolean isMachineActive() { return structureFormed && enabled && processing; }
    public float getProcessProgressFraction(float partialTicks) {
        if (!processing || maxProgress <= 0) return 0F;
        boolean client = worldObj != null && worldObj.isRemote;
        if (client && clientProgressSyncTick < 0L) clientProgressSyncTick = worldObj.getTotalWorldTime();
        boolean running = client && structureFormed && enabled
                && diagnostic != null && diagnostic.startsWith("PROCESSING:");
        long elapsed = client ? worldObj.getTotalWorldTime() - clientProgressSyncTick : 0L;
        return com.mod.htgt6.api.hitech6.multiblock.MachineAnimationProgress.ticks(
                progress, maxProgress, elapsed, partialTicks, running) / maxProgress;
    }

    /** Read-only recipe input view used by multiblock interior renderers. */
    public int getCurrentRecipeItemInputCount() {
        if (clientRecipeInputs != null) return clientRecipeInputs.length;
        return currentRecipe == null || currentRecipe.inputs == null ? 0 : currentRecipe.inputs.length;
    }

    /** Returns a defensive copy so a client renderer can never mutate the recipe database. */
    public ItemStack getCurrentRecipeItemInputForRender(int index) {
        if (clientRecipeInputs != null) {
            return index < 0 || index >= clientRecipeInputs.length || clientRecipeInputs[index] == null
                    ? null : clientRecipeInputs[index].copy();
        }
        if (currentRecipe == null || currentRecipe.inputs == null
                || index < 0 || index >= currentRecipe.inputs.length) return null;
        ItemStack stack = currentRecipe.inputs[index];
        return stack == null ? null : stack.copy();
    }

    /** Actual prepared output (including chance rolls) shown after the assembly chamber. */
    public ItemStack getCurrentRecipePrimaryOutputForRender() {
        if (clientRecipeInputs != null) return clientRecipeOutput == null ? null : clientRecipeOutput.copy();
        if (!pendingItemOutputs.isEmpty() && pendingItemOutputs.get(0) != null) {
            return pendingItemOutputs.get(0).copy();
        }
        if (currentRecipe == null || currentRecipe.outputs == null) return null;
        for (ItemStack output : currentRecipe.outputs) {
            if (output != null) return output.copy();
        }
        return null;
    }

    @Override public AxisAlignedBB getRenderBoundingBox() {
        // The TESR lives four blocks behind the controller and five blocks up;
        // the default one-block box was culled when the player approached glass.
        return AxisAlignedBB.getBoundingBox(xCoord - 6, yCoord - 1, zCoord - 6,
                xCoord + 7, yCoord + 14, zCoord + 7);
    }

    @Override public double getMaxRenderDistanceSquared() { return 65536.0D; }

    protected ChemicalPlantRecipe findRecipe() {
        if (selectorSetting >= 0 && hasMatchingRecipeForSelector(selectorSetting)) {
            return findRecipeForSelector(selectorSetting);
        }
        return findRecipeForSelector(-1);
    }

    protected boolean hasMatchingRecipeForSelector(int selectorTag) {
        for (ChemicalPlantRecipe recipe : getMachineRecipes()) {
            if (recipe != null && recipe.selectorTag == selectorTag
                    && hasItemInputs(recipe.inputs) && hasRecipeFluidInputs(recipe, 1)) return true;
        }
        return false;
    }

    protected ChemicalPlantRecipe findRecipeForSelector(int selectorTag) {
        for (ChemicalPlantRecipe recipe : getMachineRecipes()) {
            if (recipe == null || recipe.selectorTag != selectorTag || machineTier < recipe.minTier) continue;
            if (hasItemInputs(recipe.inputs) && hasRecipeFluidInputs(recipe, 1)
                    && canAcceptItemOutputs(recipe.outputs) && outputTank.canAccept(recipe.fluidOutputs)) return recipe;
        }
        return null;
    }

    protected int findBlockedRecipeTier() {
        if (selectorSetting >= 0 && hasMatchingRecipeForSelector(selectorSetting)) {
            return findBlockedRecipeTierForSelector(selectorSetting);
        }
        return findBlockedRecipeTierForSelector(-1);
    }

    protected int findBlockedRecipeTierForSelector(int selectorTag) {
        int required = Integer.MAX_VALUE;
        for (ChemicalPlantRecipe recipe : getMachineRecipes()) {
            if (recipe == null || recipe.selectorTag != selectorTag || recipe.minTier <= machineTier) continue;
            if (hasItemInputs(recipe.inputs) && hasRecipeFluidInputs(recipe, 1)) {
                required = Math.min(required, recipe.minTier);
            }
        }
        return required == Integer.MAX_VALUE ? 0 : required;
    }

    private boolean prepareRecipe(ChemicalPlantRecipe recipe) {
        pendingItemOutputs.clear();
        pendingFluidOutputs.clear();
        currentParallel = findParallelCount(recipe);
        if (currentParallel <= 0) {
            currentParallel = 1;
            diagnostic = "WAITING: OUTPUT FULL";
            return false;
        }
        for (int parallel = 0; parallel < currentParallel; parallel++) {
            for (int i = 0; i < recipe.outputs.length; i++) {
                ItemStack output = recipe.outputs[i];
                if (output != null && output.getItem() != null
                        && worldObj.rand.nextInt(10000) < Math.max(0, Math.min(10000, recipe.outputChances[i]))) {
                    addPendingItemOutput(output);
                }
            }
        }
        for (FluidStack output : recipe.fluidOutputs) {
            if (output != null && output.getFluid() != null && output.amount > 0) {
                addPendingFluidOutput(output, currentParallel);
            }
        }
        if (!canAcceptItemOutputs(pendingItemOutputs.toArray(new ItemStack[pendingItemOutputs.size()]))
                || !outputTank.canAccept(pendingFluidOutputs.toArray(new FluidStack[pendingFluidOutputs.size()]))) {
            pendingItemOutputs.clear();
            pendingFluidOutputs.clear();
            diagnostic = "WAITING: OUTPUT FULL";
            return false;
        }
        consumeItemInputs(recipe.inputs, currentParallel);
        consumeRecipeFluidInputs(recipe, currentParallel);
        currentRecipe = recipe;
        currentRecipeName = getRecipeName(recipe);
        progress = 0;
        maxProgress = getRecipeDuration(recipe);
        processing = true;
        diagnostic = "PROCESSING: " + currentRecipeName;
        markControllerDirtyAndSync();
        return true;
    }

    private void completeRecipe() {
        for (ItemStack output : pendingItemOutputs) insertItemOutput(output);
        outputTank.addAll(pendingFluidOutputs.toArray(new FluidStack[pendingFluidOutputs.size()]));
        pendingItemOutputs.clear();
        pendingFluidOutputs.clear();
        currentRecipe = null;
        currentRecipeName = "";
        progress = 0;
        processing = false;
        currentEnergyUsage = 0L;
        currentParallel = 1;
        requiredRecipeTier = 0;
        diagnostic = "READY: CHEMICAL PROCESS INTERFACE";
        markControllerDirtyAndSync();
    }

    private void stopProcessing(boolean discardRecipe) {
        processing = false;
        currentEnergyUsage = 0L;
        if (discardRecipe) {
            currentRecipe = null;
            currentRecipeName = "";
            pendingItemOutputs.clear();
            pendingFluidOutputs.clear();
            progress = 0;
            requiredRecipeTier = 0;
            currentParallel = 1;
        }
    }

    private int findParallelCount(ChemicalPlantRecipe recipe) {
        int difference = Math.max(0, machineTier - recipe.minTier);
        int limit = 1 << Math.min(20, difference);
        int low = 0;
        int high = limit;
        while (low < high) {
            int candidate = low + (high - low + 1) / 2;
            if (canRunParallel(recipe, candidate)) low = candidate; else high = candidate - 1;
        }
        return low;
    }

    private boolean canRunParallel(ChemicalPlantRecipe recipe, int parallel) {
        return parallel > 0 && hasItemInputs(recipe.inputs, parallel)
                && hasRecipeFluidInputs(recipe, parallel)
                && canAcceptItemOutputs(recipe.outputs, parallel)
                && outputTank.canAccept(recipe.fluidOutputs, parallel);
    }

    /** Recipe-family hook for machines which bind a recipe to one physical fluid gateway. */
    protected boolean hasRecipeFluidInputs(ChemicalPlantRecipe recipe, int multiplier) {
        return inputTank.hasFluids(recipe.fluidInputs, multiplier);
    }

    /** Counterpart of {@link #hasRecipeFluidInputs}; called only after a successful preparation check. */
    protected void consumeRecipeFluidInputs(ChemicalPlantRecipe recipe, int multiplier) {
        inputTank.consume(recipe.fluidInputs, multiplier);
    }

    protected final int getConfiguredSelectorSetting() { return selectorSetting; }

    protected final boolean hasInputFluidsInRange(FluidStack[] requirements, int multiplier,
                                                   int start, int length) {
        return inputTank.hasFluidsRange(requirements, multiplier, start, length);
    }

    protected final void consumeInputFluidsInRange(FluidStack[] requirements, int multiplier,
                                                    int start, int length) {
        inputTank.consumeRange(requirements, multiplier, start, length);
        markControllerDirtyAndSync();
    }

    private void addPendingItemOutput(ItemStack output) {
        for (ItemStack pending : pendingItemOutputs) {
            if (!sameItem(output, pending) || pending.stackSize > Integer.MAX_VALUE - output.stackSize) continue;
            pending.stackSize += output.stackSize;
            return;
        }
        pendingItemOutputs.add(output.copy());
    }

    private void addPendingFluidOutput(FluidStack output, int multiplier) {
        long remaining = (long) output.amount * multiplier;
        while (remaining > 0L) {
            int amount = (int) Math.min(Integer.MAX_VALUE, remaining);
            FluidStack scaled = output.copy();
            scaled.amount = amount;
            pendingFluidOutputs.add(scaled);
            remaining -= amount;
        }
    }

    private int getRecipeDuration(ChemicalPlantRecipe recipe) {
        int difference = Math.max(0, machineTier - recipe.minTier);
        return Math.max(6, recipe.duration >> Math.min(30, difference));
    }

    private long getRecipeEUt(ChemicalPlantRecipe recipe, int parallel) {
        int difference = Math.max(0, machineTier - recipe.minTier);
        long result = Math.max(1, recipe.euPerTick);
        for (int i = 0; i < difference; i++) {
            if (result > Long.MAX_VALUE / 4L) return Long.MAX_VALUE;
            result *= 4L;
        }
        return safeMultiply(result, Math.max(1, parallel));
    }

    private boolean hasItemInputs(ItemStack[] requirements) {
        return hasItemInputs(requirements, 1);
    }

    private boolean hasItemInputs(ItemStack[] requirements, int multiplier) {
        int[] available = new int[SLOT_COUNT_PER_BUFFER];
        for (int i = 0; i < available.length; i++) {
            ItemStack stack = inventory[INPUT_SLOT_START + i];
            available[i] = stack == null ? 0 : stack.stackSize;
        }
        for (ItemStack requirement : requirements) {
            if (requirement == null) continue;
            long remaining = Math.max(0L, (long) requirement.stackSize * multiplier);
            boolean foundType = false;
            for (int i = 0; i < available.length; i++) {
                ItemStack stored = inventory[INPUT_SLOT_START + i];
                if (!sameItem(requirement, stored)) continue;
                foundType = true;
                if (remaining <= 0) break;
                int used = (int) Math.min(remaining, available[i]);
                available[i] -= used;
                remaining -= used;
                if (remaining <= 0) break;
            }
            if (!foundType || remaining > 0) return false;
        }
        return true;
    }

    private void consumeItemInputs(ItemStack[] requirements, int multiplier) {
        for (ItemStack requirement : requirements) {
            if (requirement == null || requirement.stackSize <= 0) continue;
            long remaining = (long) requirement.stackSize * multiplier;
            for (int i = 0; i < SLOT_COUNT_PER_BUFFER && remaining > 0; i++) {
                int slot = INPUT_SLOT_START + i;
                ItemStack stored = inventory[slot];
                if (!sameItem(requirement, stored)) continue;
                int used = (int) Math.min(remaining, stored.stackSize);
                stored.stackSize -= used;
                remaining -= used;
                if (stored.stackSize <= 0) inventory[slot] = null;
            }
        }
    }

    private boolean canAcceptItemOutputs(ItemStack[] outputs) {
        return canAcceptItemOutputs(outputs, 1);
    }

    private boolean canAcceptItemOutputs(ItemStack[] outputs, int multiplier) {
        ItemStack[] simulated = new ItemStack[SLOT_COUNT_PER_BUFFER];
        for (int i = 0; i < simulated.length; i++) {
            ItemStack stack = inventory[OUTPUT_SLOT_START + i];
            simulated[i] = stack == null ? null : stack.copy();
        }
        for (ItemStack output : outputs) {
            if (output == null) continue;
            long remaining = (long) Math.max(0, output.stackSize) * multiplier;
            while (remaining > 0L) {
                ItemStack chunk = output.copy();
                chunk.stackSize = (int) Math.min(Integer.MAX_VALUE, remaining);
                if (!insertInto(chunk, simulated)) return false;
                remaining -= Math.min(Integer.MAX_VALUE, remaining);
            }
        }
        return true;
    }

    private static long safeMultiply(long value, int multiplier) {
        if (value <= 0L || multiplier <= 0) return 0L;
        return value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
    }

    private void insertItemOutput(ItemStack output) {
        ItemStack[] target = new ItemStack[SLOT_COUNT_PER_BUFFER];
        System.arraycopy(inventory, OUTPUT_SLOT_START, target, 0, SLOT_COUNT_PER_BUFFER);
        insertInto(output.copy(), target);
        System.arraycopy(target, 0, inventory, OUTPUT_SLOT_START, SLOT_COUNT_PER_BUFFER);
    }

    private static boolean insertInto(ItemStack stack, ItemStack[] slots) {
        for (ItemStack stored : slots) {
            if (!sameItem(stack, stored)) continue;
            int moved = Math.min(stack.stackSize, stored.getMaxStackSize() - stored.stackSize);
            stored.stackSize += Math.max(0, moved);
            stack.stackSize -= Math.max(0, moved);
            if (stack.stackSize <= 0) return true;
        }
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) continue;
            int moved = Math.min(stack.stackSize, stack.getMaxStackSize());
            slots[i] = stack.copy();
            slots[i].stackSize = moved;
            stack.stackSize -= moved;
            if (stack.stackSize <= 0) return true;
        }
        return false;
    }

    private static boolean sameItem(ItemStack expected, ItemStack actual) {
        return expected != null && actual != null && expected.getItem() == actual.getItem()
                && (expected.getItemDamage() == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE
                || expected.getItemDamage() == actual.getItemDamage())
                && ItemStack.areItemStackTagsEqual(expected, actual);
    }

    private static String getRecipeName(ChemicalPlantRecipe recipe) {
        for (ItemStack output : recipe.outputs) if (output != null) return output.getDisplayName();
        for (FluidStack output : recipe.fluidOutputs) if (output != null) return output.getLocalizedName();
        return recipe.source;
    }

    @Override protected MultiblockStructureDefinition createStructureDefinition() {
        return MultiblockStructureDefinition.builder(-4, 4, 0, 12, 0, 8,
                new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        int element = getRequiredElement(context.u, context.y, context.v);
                        if (element == ELEMENT_CHEMICAL_CASING && context.block == ChemicalPlantContent.rib
                                && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.chemical(context.u,context.y,context.v))
                            return MultiblockStructureDefinition.Match.valid("reinforced_rib");
                        switch (element) {
                            case ELEMENT_CONTROLLER:
                                return MultiTileChemicalPlant.match(context.block == ChemicalPlantContent.controller, "controller");
                            case ELEMENT_FLEXIBLE_BASE:
                                IMultiblockGateway gateway = context.getGateway();
                                return MultiTileChemicalPlant.match((context.block == ChemicalPlantContent.casing
                                                && context.metadata == BlockChemicalPlantCasing.STANDARD)
                                                || gateway != null,
                                        gateway == null ? "standard_casing" : "gateway");
                            case ELEMENT_CHEMICAL_CASING:
                                return MultiTileChemicalPlant.match(context.block == ChemicalPlantContent.casing
                                                && context.metadata == BlockChemicalPlantCasing.STANDARD,
                                        "chemical_casing");
                            case ELEMENT_THERMOCHEMICAL_CASING:
                                return MultiTileChemicalPlant.match(context.block == ChemicalPlantContent.casing
                                                && context.metadata == BlockChemicalPlantCasing.THERMOCHEMICAL,
                                        "thermochemical_casing");
                            case ELEMENT_FRAME:
                                return MultiTileChemicalPlant.match(context.block == MiningUnitContent.frame
                                        && context.metadata == 0, "reinforced_frame");
                            case ELEMENT_GLASS:
                                return MultiTileChemicalPlant.match(context.block == ChemicalPlantContent.glass,
                                        "chemical_glass");
                            default:
                                return MultiTileChemicalPlant.match(context.isAir(), null);
                        }
                    }
                })
                .gateway(GatewayType.FLUID_INPUT, 1, 10)
                .gateway(GatewayType.FLUID_OUTPUT, 1, 10)
                .gateway(GatewayType.ITEM_INPUT, 1, 10)
                .gateway(GatewayType.ITEM_OUTPUT, 1, 10)
                .gateway(GatewayType.ENERGY_INPUT, 1, 1)
                .build();
    }

    /** Shared by the server validator and the NEI model, preventing geometry drift. */
    public static int getRequiredElement(int u, int y, int v) {
        if (y == 0) return u == 0 && v == 0 ? ELEMENT_CONTROLLER : ELEMENT_FLEXIBLE_BASE;

        boolean outerRing = Math.abs(u) == 4 || v == 0 || v == 8;
        boolean outerCorner = Math.abs(u) == 4 && (v == 0 || v == 8);
        boolean centerFive = Math.abs(u) <= 2 && v >= 2 && v <= 6;
        boolean centerFiveRing = centerFive && (Math.abs(u) == 2 || v == 2 || v == 6);
        boolean centerFiveCorner = Math.abs(u) == 2 && (v == 2 || v == 6);
        boolean centerThree = Math.abs(u) <= 1 && v >= 3 && v <= 5;
        boolean centerThreeRing = centerThree && (Math.abs(u) == 1 || v == 3 || v == 5);
        boolean centerThreeCorner = Math.abs(u) == 1 && (v == 3 || v == 5);

        if (y == 1 || y == 8) {
            if (outerRing) return ELEMENT_CHEMICAL_CASING;
            if (centerFive) return ELEMENT_THERMOCHEMICAL_CASING;
        } else if (y == 2) {
            if (outerCorner) return ELEMENT_FRAME;
            if (centerFive) return ELEMENT_THERMOCHEMICAL_CASING;
        } else if (y >= 3 && y <= 7) {
            if (outerCorner || centerFiveCorner) return ELEMENT_FRAME;
            if (centerFiveRing) return ELEMENT_GLASS;
        } else if (y >= 9 && y <= 11) {
            if (centerThreeCorner) return ELEMENT_FRAME;
            if (centerThreeRing) return ELEMENT_CHEMICAL_CASING;
        } else if (y == 12 && centerThree) {
            return ELEMENT_THERMOCHEMICAL_CASING;
        }
        return ELEMENT_AIR;
    }

    private static MultiblockStructureDefinition.Match match(boolean valid, String key) {
        return valid ? MultiblockStructureDefinition.Match.valid(key)
                : MultiblockStructureDefinition.Match.invalid();
    }

    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == SLOT_MODULE) return isTierModule(stack);
        if (slot == SLOT_TRANSFORMER) return isTransformer(stack);
        return slot >= INPUT_SLOT_START && slot < OUTPUT_SLOT_START;
    }

    private static boolean isTierModule(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        return item == Tmodules.TierModuleMV || item == Tmodules.TierModuleHV || item == Tmodules.TierModuleEV
                || item == Tmodules.TierModuleIV || item == Tmodules.TierModuleLuV || item == Tmodules.TierModuleZPM
                || item == Tmodules.TierModuleUV || item == Tmodules.TierModulePUV1 || item == Tmodules.TierModuleUX
                || item == Tmodules.TierModuleOLV || item == Tmodules.TierModuleOMV || item == Tmodules.TierModuleOHV
                || item == Tmodules.TierModuleOEV || item == Tmodules.TierModuleOIV || item == Tmodules.TierModuleMAX;
    }

    private static boolean isTransformer(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        return item == Tmodules.LowLevelTransformer || item == Tmodules.MediumLevelTransformer
                || item == Tmodules.HighLevelTransformer || item == Tmodules.UltimateLevelTransformer
                || item == Tmodules.OmegaTransformer;
    }

    @Override public ModularScreen createScreen(GuiCreationContext context, ModularPanel panel) {
        return new ModularScreen(panel);
    }

    @Override public ModularPanel buildUI(GuiCreationContext context, GuiSyncManager sync, boolean isClient) {
        sync.registerSlotGroup("chemical_modifiers", 2);
        sync.syncValue("chemical_formed", SyncHandlers.bool(() -> structureFormed, value -> structureFormed = value));
        sync.syncValue("chemical_enabled", SyncHandlers.bool(() -> enabled, value -> enabled = value));
        sync.syncValue("chemical_tier", SyncHandlers.intNumber(() -> machineTier, value -> machineTier = value));
        sync.syncValue("chemical_energy", SyncHandlers.longNumber(() -> energy, value -> energy = value));
        sync.syncValue("chemical_energy_usage", SyncHandlers.longNumber(
                () -> currentEnergyUsage, value -> currentEnergyUsage = Math.max(0L, value)));
        sync.syncValue("chemical_processing", SyncHandlers.bool(() -> processing, value -> processing = value));
        sync.syncValue("chemical_progress", SyncHandlers.intNumber(() -> progress, value -> {
            progress = value;
            if (worldObj != null && worldObj.isRemote) clientProgressSyncTick = worldObj.getTotalWorldTime();
        }));
        sync.syncValue("chemical_max_progress", SyncHandlers.intNumber(() -> maxProgress, value -> maxProgress = value));
        sync.syncValue("chemical_parallel", SyncHandlers.intNumber(
                () -> currentParallel, value -> currentParallel = Math.max(1, value)));
        sync.syncValue("chemical_recipe_name", SyncHandlers.string(
                () -> currentRecipeName, value -> currentRecipeName = value == null ? "" : value));
        sync.syncValue("chemical_required_tier", SyncHandlers.intNumber(
                () -> requiredRecipeTier, value -> requiredRecipeTier = Math.max(0, value)));
        sync.syncValue("chemical_selector", SyncHandlers.intNumber(
                () -> selectorSetting, value -> selectorSetting = normalizeSelector(value)));
        sync.syncValue("chemical_diagnostic", SyncHandlers.string(() -> diagnostic, value -> diagnostic = value));
        sync.syncValue("chemical_power_action", new InteractionSyncHandler().setOnMouseTapped(data -> toggleMachinePower()));
        sync.syncValue("chemical_restart_action", new InteractionSyncHandler().setOnMouseTapped(data -> restartMachine()));
        sync.syncValue("chemical_selector_action", new InteractionSyncHandler().setOnMouseTapped(data -> cycleSelector()));

        MachineUiTheme theme = MachineUiTheme.getActive();
        ModularPanel panel = ModularPanel.defaultPanel(getPanelId(), 260, 240)
                .background(new Rectangle().setColor(theme.getPanelColor()));
        frame(panel, theme.getFrameColor(), 0, 0, 260, 2);
        frame(panel, theme.getFrameColor(), 0, 238, 260, 2);
        frame(panel, theme.getFrameColor(), 0, 0, 2, 240);
        frame(panel, theme.getFrameColor(), 258, 0, 2, 240);
        panel.child(new TextWidget(IKey.lang(getTitleKey())).pos(10, 8).color(theme.getTitleColor()));
        panel.child(new TextWidget(IKey.dynamic(this::getTierLine)).pos(164, 8).size(40, 12)
                .color(theme.getProductionColor()));
        if (isClient) {
            panel.child(com.mod.htgt6.client.GUI.MiningUnitModularUiButtons.createStructureButton(
                    getControllerDisplayStack(), getStructureTooltipKey()).pos(210, 7));
        }

        Widget terminal = new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        terminal.pos(8, 24).size(244, 70);
        panel.child(terminal);
        panel.child(new TextWidget(IKey.dynamic(this::getStructureLine)).pos(16, 37).size(228, 13)
                .color(0xFFFFFFFF));
        panel.child(new TextWidget(IKey.dynamic(this::getStatusLine)).pos(16, 64).size(228, 13)
                .color(theme.getPrimaryColor()));

        final ParentWidget<?> energyPanel = new ParentWidget();
        energyPanel.pos(260, 24).size(178, 92).background(new Rectangle().setColor(theme.getTerminalColor()));
        energyPanel.child(new TextWidget(IKey.lang(getUiKey("energy_panel")))
                .pos(7, 7).color(theme.getTitleColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.energy_tier", machineTier, getTierName())))
                .pos(7, 24).color(theme.getProductionColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.energy_buffer", formatEnergy(energy), formatEnergy(maxEnergy))))
                .pos(7, 39).color(theme.getEnergyColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.energy_input", formatEnergy(getMaxInputVoltage()))))
                .pos(7, 54).color(theme.getEnergyColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.energy_usage", formatEnergy(currentEnergyUsage))))
                .pos(7, 69).color(theme.getEnergyColor()));
        energyPanel.setEnabled(false);
        panel.child(energyPanel);

        panel.child(createModifierSlot(SLOT_MODULE, "ui.htgt6.mining_unit.tier_module").pos(54, 108));
        panel.child(createModifierSlot(SLOT_TRANSFORMER, "ui.htgt6.mining_unit.transformer").pos(82, 108));
        panel.child(createPowerButton().pos(108, 106));
        panel.child(createRestartButton().pos(134, 106));
        panel.child(createEnergyPanelButton(energyPanel).pos(160, 106));
        panel.child(createSelectorButton().pos(186, 106));
        if (isClient) {
            panel.child(com.mod.htgt6.client.GUI.MiningUnitModularUiButtons.createRecipeButton(
                    getRecipeOverlayId(), getRecipeTooltipKey()).pos(212, 106));
        }
        panel.child(new TextWidget(IKey.lang(getUiKey("player_inventory"))).pos(76, 139)
                .color(theme.getLabelColor()));
        panel.child(SlotGroupWidget.playerInventory().pos(44, 155));
        return panel;
    }

    private String getTierLine() { return "T" + machineTier + "  " + getTierName(); }

    private String getStructureLine() {
        return (structureFormed ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
                + StatCollector.translateToLocal(structureFormed
                ? "ui.htgt6.structure.complete" : "ui.htgt6.structure.incomplete");
    }

    private String getStatusLine() {
        if (!structureFormed) return EnumChatFormatting.RED + localizeDiagnostic();
        if (!enabled) return EnumChatFormatting.GOLD
                + StatCollector.translateToLocal("ui.htgt6.terminal.disabled");
        if (processing && currentRecipeName != null && !currentRecipeName.isEmpty()) {
            return EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted(
                    getUiKey("processing"), currentRecipeName,
                    currentParallel, formatSeconds(progress), formatSeconds(maxProgress));
        }
        if ("WAITING: ENERGY".equals(diagnostic)) return EnumChatFormatting.GOLD
                + StatCollector.translateToLocal("ui.htgt6.terminal.need_energy");
        if ("WAITING: OUTPUT FULL".equals(diagnostic)) return EnumChatFormatting.GOLD
                + StatCollector.translateToLocal(getUiKey("output_full"));
        if (requiredRecipeTier > machineTier) return EnumChatFormatting.RED
                + StatCollector.translateToLocalFormatted(getUiKey("tier_required"),
                requiredRecipeTier, getTierName(requiredRecipeTier));
        return EnumChatFormatting.AQUA
                + StatCollector.translateToLocal(getUiKey("waiting_recipe"));
    }

    private String localizeDiagnostic() {
        if (diagnostic != null && diagnostic.startsWith("STRUCTURE ERROR @ ")) {
            return StatCollector.translateToLocalFormatted("ui.htgt6.terminal.structure_error",
                    diagnostic.substring("STRUCTURE ERROR @ ".length()));
        }
        if (diagnostic != null && diagnostic.startsWith("MISSING GATEWAY: ")) {
            return StatCollector.translateToLocalFormatted("ui.htgt6.terminal.missing_gateway",
                    diagnostic.substring("MISSING GATEWAY: ".length()));
        }
        if (diagnostic != null && diagnostic.startsWith("TOO MANY GATEWAYS: ")) {
            return StatCollector.translateToLocalFormatted(getUiKey("too_many_gateway"),
                    diagnostic.substring("TOO MANY GATEWAYS: ".length()));
        }
        return StatCollector.translateToLocal("ui.htgt6.terminal.structure_not_checked");
    }

    private ItemSlot createModifierSlot(int index, String tooltipKey) {
        ModularSlot slot = SyncHandlers.itemSlot(modifierInventory, index)
                .slotGroup("chemical_modifiers").accessibility(true, true);
        ItemSlot widget = new ItemSlot().slot(slot).background(GuiTextures.SLOT_DARK);
        widget.tooltip(tooltip -> tooltip.addLine(StatCollector.translateToLocal(tooltipKey)));
        return widget;
    }

    private ButtonWidget<?> createPowerButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(22, 22).background(GuiTextures.BUTTON).overlay(new IDrawable() {
            @Override public void draw(GuiContext context, int x, int y, int width, int height) {
                (enabled ? GT5_POWER_ON : GT5_POWER_OFF).draw(context, x, y, width, height);
            }
        });
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(StatCollector.translateToLocal(enabled
                    ? getUiKey("power_off") : getUiKey("power_on")));
        });
        button.syncHandler("chemical_power_action");
        return button;
    }

    private ButtonWidget<?> createRestartButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(22, 22).background(GuiTextures.BUTTON).overlay(GT5_RESTART);
        button.tooltip(t -> t.addLine(StatCollector.translateToLocal(getUiKey("restart"))));
        button.syncHandler("chemical_restart_action");
        return button;
    }

    private ButtonWidget<?> createEnergyPanelButton(final ParentWidget<?> energyPanel) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(22, 22).background(GuiTextures.BUTTON).overlay(GT5_ENERGY_PANEL);
        button.tooltip(t -> t.addLine(StatCollector.translateToLocal(getUiKey("energy_button"))));
        button.onMouseTapped(mouse -> {
            energyPanelOpen = !energyPanelOpen;
            energyPanel.setEnabled(energyPanelOpen);
            return true;
        });
        return button;
    }

    private ButtonWidget<?> createSelectorButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(22, 22).background(GuiTextures.BUTTON).overlay(new IDrawable() {
            @Override public void draw(GuiContext context, int x, int y, int width, int height) {
                ItemStack selector = IL.Circuit_Selector.getWithMeta(1, Math.max(0, selectorSetting));
                if (selector != null) new ItemDrawable(selector).draw(context, x + 2, y + 2, width - 4, height - 4);
            }
        });
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(StatCollector.translateToLocalFormatted(
                    selectorSetting < 0 ? getUiKey("selector_off")
                            : getUiKey("selector_value"),
                    selectorSetting));
            t.addLine(StatCollector.translateToLocal(getUiKey("selector_hint")));
        });
        button.syncHandler("chemical_selector_action");
        return button;
    }

    private void toggleMachinePower() {
        enabled = !enabled;
        if (!enabled) currentEnergyUsage = 0L;
        markControllerDirtyAndSync();
    }

    private void restartMachine() {
        progress = 0;
        currentEnergyUsage = 0L;
        if (worldObj != null && !worldObj.isRemote) checkStructure();
        markControllerDirtyAndSync();
    }

    private void cycleSelector() {
        selectorSetting = selectorSetting >= 24 ? -1 : selectorSetting + 1;
        requiredRecipeTier = 0;
        markControllerDirtyAndSync();
    }

    private static int normalizeSelector(int value) { return value >= 0 && value <= 24 ? value : -1; }

    private static String formatEnergy(long value) {
        long absolute = value == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(value);
        String[] suffixes = { "", "K", "M", "G", "T", "P", "E" };
        int suffix = 0;
        double scaled = value;
        while (absolute >= 1000L && suffix < suffixes.length - 1) {
            absolute /= 1000L;
            scaled /= 1000.0D;
            suffix++;
        }
        if (suffix == 0) return Long.toString(value);
        return String.format(Locale.ROOT, scaled >= 100.0D ? "%.0f%s" : scaled >= 10.0D ? "%.1f%s" : "%.2f%s",
                scaled, suffixes[suffix]);
    }

    private static String formatSeconds(int ticks) {
        if (ticks % 20 == 0) return Integer.toString(ticks / 20);
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0F);
    }

    private static void frame(ModularPanel panel, int color, int x, int y, int width, int height) {
        Widget edge = new Widget().background(new Rectangle().setColor(color));
        edge.pos(x, y).size(width, height);
        panel.child(edge);
    }

    private String getTierName() { return getTierName(machineTier); }
    private static String getTierName(int tier) {
        String[] names = { "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV",
                "PUV1", "UX", "OLV", "OMV", "OHV", "OEV", "OIV", "MAX" };
        return names[Math.max(0, Math.min(tier - 1, names.length - 1))];
    }

    public int fillRecipeOutput(FluidStack resource, boolean doFill) {
        int filled = outputTank.fill(resource, doFill);
        if (filled > 0 && doFill) markControllerDirtyAndSync();
        return filled;
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (worldObj != null && worldObj.isRemote) clientProgressSyncTick = worldObj.getTotalWorldTime();
        if (nbt.hasKey("ChemicalInputTank")) inputTank.readFromNBT(nbt.getCompoundTag("ChemicalInputTank"));
        if (nbt.hasKey("ChemicalOutputTank")) outputTank.readFromNBT(nbt.getCompoundTag("ChemicalOutputTank"));
        processing = nbt.getBoolean("ChemicalProcessing");
        requiredRecipeTier = Math.max(0, nbt.getInteger("ChemicalRequiredTier"));
        currentParallel = nbt.hasKey("ChemicalParallel")
                ? Math.max(1, nbt.getInteger("ChemicalParallel")) : 1;
        selectorSetting = nbt.hasKey("ChemicalSelector")
                ? normalizeSelector(nbt.getInteger("ChemicalSelector")) : -1;
        currentRecipeName = nbt.getString("ChemicalRecipeName");
        int recipeIndex = nbt.hasKey("ChemicalRecipeIndex") ? nbt.getInteger("ChemicalRecipeIndex") : -1;
        List<ChemicalPlantRecipe> recipes = getMachineRecipes();
        currentRecipe = recipeIndex >= 0 && recipeIndex < recipes.size() ? recipes.get(recipeIndex) : null;
        pendingItemOutputs.clear();
        NBTTagList items = nbt.getTagList("ChemicalPendingItems", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i));
            if (stack != null) pendingItemOutputs.add(stack);
        }
        pendingFluidOutputs.clear();
        NBTTagList fluids = nbt.getTagList("ChemicalPendingFluids", 10);
        for (int i = 0; i < fluids.tagCount(); i++) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(fluids.getCompoundTagAt(i));
            if (stack != null) pendingFluidOutputs.add(stack);
        }
        if (worldObj != null && worldObj.isRemote) {
            // A server recipe index is not a client recipe identity: load order can differ.
            // Never interpret a server recipe index as a local visual identity,
            // even before the first full render snapshot (or with an older packet).
            clientRecipeInputs = new ItemStack[0];
            clientRecipeOutput = null;
            if (nbt.hasKey("ChemicalRenderInputs")) {
                NBTTagList renderInputs = nbt.getTagList("ChemicalRenderInputs", 10);
                clientRecipeInputs = new ItemStack[renderInputs.tagCount()];
                for (int i = 0; i < clientRecipeInputs.length; i++)
                    clientRecipeInputs[i] = ItemStack.loadItemStackFromNBT(renderInputs.getCompoundTagAt(i));
                if (nbt.hasKey("ChemicalRenderOutput"))
                    clientRecipeOutput = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("ChemicalRenderOutput"));
            }
        } else if (currentRecipe == null) {
            processing = false;
            progress = 0;
            currentParallel = 1;
            pendingItemOutputs.clear();
            pendingFluidOutputs.clear();
        }
    }

    @Override public net.minecraft.network.Packet getDescriptionPacket() {
        // S35's NBT getter is client-only and absent on dedicated servers.
        // Build the complete payload before constructing the outgoing packet.
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        NBTTagList inputs = new NBTTagList();
        if (currentRecipe != null && currentRecipe.inputs != null) {
            for (ItemStack stack : currentRecipe.inputs) {
                NBTTagCompound entry = new NBTTagCompound();
                if (stack != null) stack.writeToNBT(entry);
                inputs.appendTag(entry);
            }
        }
        nbt.setTag("ChemicalRenderInputs", inputs);
        ItemStack output = getCurrentRecipePrimaryOutputForRender();
        if (output != null) {
            NBTTagCompound entry = new NBTTagCompound();
            output.writeToNBT(entry);
            nbt.setTag("ChemicalRenderOutput", entry);
        }
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(
                xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagCompound input = new NBTTagCompound();
        inputTank.writeToNBT(input);
        nbt.setTag("ChemicalInputTank", input);
        NBTTagCompound output = new NBTTagCompound();
        outputTank.writeToNBT(output);
        nbt.setTag("ChemicalOutputTank", output);
        nbt.setBoolean("ChemicalProcessing", processing);
        nbt.setInteger("ChemicalRequiredTier", requiredRecipeTier);
        nbt.setInteger("ChemicalParallel", Math.max(1, currentParallel));
        nbt.setInteger("ChemicalSelector", selectorSetting);
        nbt.setString("ChemicalRecipeName", currentRecipeName == null ? "" : currentRecipeName);
        nbt.setInteger("ChemicalRecipeIndex", currentRecipe == null ? -1
                : getMachineRecipes().indexOf(currentRecipe));
        NBTTagList items = new NBTTagList();
        for (ItemStack stack : pendingItemOutputs) {
            if (stack == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            stack.writeToNBT(entry);
            items.appendTag(entry);
        }
        nbt.setTag("ChemicalPendingItems", items);
        NBTTagList fluids = new NBTTagList();
        for (FluidStack stack : pendingFluidOutputs) {
            if (stack == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            stack.writeToNBT(entry);
            fluids.appendTag(entry);
        }
        nbt.setTag("ChemicalPendingFluids", fluids);
    }

    private static int[] makeSlotRange(int start, int count) {
        int[] result = new int[count];
        for (int i = 0; i < count; i++) result[i] = start + i;
        return result;
    }

    private final class InputFluidGateway implements IFluidHandler {
        @Override public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
            int filled = inputTank.fill(resource, doFill);
            if (filled > 0 && doFill) markControllerDirtyAndSync();
            return filled;
        }
        @Override public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) { return null; }
        @Override public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) { return null; }
        @Override public boolean canFill(ForgeDirection from, Fluid fluid) { return inputTank.canFill(fluid); }
        @Override public boolean canDrain(ForgeDirection from, Fluid fluid) { return false; }
        @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) { return inputTank.getTankInfo(); }
    }

    private final class OutputFluidGateway implements IFluidHandler {
        @Override public int fill(ForgeDirection from, FluidStack resource, boolean doFill) { return 0; }
        @Override public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
            return outputTank.drain(resource, doDrain);
        }
        @Override public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
            FluidStack drained = outputTank.drain(maxDrain, doDrain);
            if (drained != null && doDrain) markControllerDirtyAndSync();
            return drained;
        }
        @Override public boolean canFill(ForgeDirection from, Fluid fluid) { return false; }
        @Override public boolean canDrain(ForgeDirection from, Fluid fluid) { return outputTank.canDrain(fluid); }
        @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) { return outputTank.getTankInfo(); }
    }

    /** One logical buffer with one tank for each installed 1/4/8-channel gateway. */
    private final class ChemicalFluidBuffer {
        private static final int MAX_TANKS = 80;
        private final GatewayType type;
        private final FluidStack[] fluids = new FluidStack[MAX_TANKS];

        private ChemicalFluidBuffer(GatewayType type) { this.type = type; }

        private int tankCount() {
            return Math.max(1, Math.min(MAX_TANKS, getStructureFluidTankCount(type)));
        }

        private int capacity() {
            return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, getGatewayFluidCapacity(type)));
        }

        private int fill(FluidStack resource, boolean doFill) {
            return fillRange(resource, doFill, 0, tankCount());
        }

        private int fillRange(FluidStack resource, boolean doFill, int start, int length) {
            if (resource == null || resource.getFluid() == null || resource.amount <= 0) return 0;
            int remaining = resource.amount;
            int end = Math.min(tankCount(), Math.max(0, start) + Math.max(0, length));
            start = Math.max(0, Math.min(start, end));
            int capacity = capacity();
            for (int i = start; i < end && remaining > 0; i++) {
                FluidStack stored = fluids[i];
                if (stored == null || !stored.isFluidEqual(resource)) continue;
                int moved = Math.min(remaining, Math.max(0, capacity - stored.amount));
                if (doFill) stored.amount += moved;
                remaining -= moved;
            }
            for (int i = start; i < end && remaining > 0; i++) {
                if (fluids[i] != null) continue;
                int moved = Math.min(remaining, capacity);
                if (doFill) {
                    fluids[i] = resource.copy();
                    fluids[i].amount = moved;
                }
                remaining -= moved;
            }
            return resource.amount - remaining;
        }

        private FluidStack drain(int maximum, boolean doDrain) {
            return drainRange(maximum, doDrain, 0, tankCount());
        }

        private FluidStack drainRange(int maximum, boolean doDrain, int start, int length) {
            if (maximum <= 0) return null;
            int end = Math.min(tankCount(), Math.max(0, start) + Math.max(0, length));
            for (int i = Math.max(0, start); i < end; i++) {
                if (fluids[i] != null) return drainRange(fluids[i].copy(), maximum, doDrain, start, length);
            }
            return null;
        }

        private FluidStack drain(FluidStack request, boolean doDrain) {
            return request == null ? null : drainRange(request, request.amount, doDrain, 0, tankCount());
        }

        private FluidStack drainRange(FluidStack request, int maximum, boolean doDrain, int start, int length) {
            if (request == null || request.getFluid() == null || maximum <= 0) return null;
            int remaining = maximum;
            FluidStack result = request.copy();
            result.amount = 0;
            int end = Math.min(tankCount(), Math.max(0, start) + Math.max(0, length));
            for (int i = Math.max(0, start); i < end && remaining > 0; i++) {
                FluidStack stored = fluids[i];
                if (stored == null || !stored.isFluidEqual(request)) continue;
                int moved = Math.min(remaining, stored.amount);
                result.amount += moved;
                remaining -= moved;
                if (doDrain) {
                    stored.amount -= moved;
                    if (stored.amount <= 0) fluids[i] = null;
                }
            }
            return result.amount <= 0 ? null : result;
        }

        private boolean canFill(Fluid fluid) {
            return canFillRange(fluid, 0, tankCount());
        }

        private boolean canFillRange(Fluid fluid, int start, int length) {
            if (fluid == null) return false;
            int end = Math.min(tankCount(), Math.max(0, start) + Math.max(0, length));
            for (int i = Math.max(0, start); i < end; i++) {
                if (fluids[i] == null || fluids[i].getFluid() == fluid) return true;
            }
            return false;
        }

        private boolean canDrain(Fluid fluid) {
            return canDrainRange(fluid, 0, tankCount());
        }

        private boolean canDrainRange(Fluid fluid, int start, int length) {
            int end = Math.min(tankCount(), Math.max(0, start) + Math.max(0, length));
            for (int i = Math.max(0, start); i < end; i++) {
                if (fluids[i] != null && (fluid == null || fluids[i].getFluid() == fluid)) return true;
            }
            return false;
        }

        private FluidTankInfo[] getTankInfo() {
            return getTankInfoRange(0, tankCount());
        }

        private FluidTankInfo[] getTankInfoRange(int start, int length) {
            int end = Math.min(tankCount(), Math.max(0, start) + Math.max(0, length));
            start = Math.max(0, Math.min(start, end));
            FluidTankInfo[] result = new FluidTankInfo[end - start];
            for (int i = start; i < end; i++) {
                result[i - start] = new FluidTankInfo(fluids[i] == null ? null : fluids[i].copy(), capacity());
            }
            return result;
        }

        private boolean hasFluids(FluidStack[] requirements) {
            return hasFluids(requirements, 1);
        }

        private boolean hasFluids(FluidStack[] requirements, int multiplier) {
            FluidStack[] simulated = copyFluidState();
            return consumeFrom(requirements, simulated, multiplier);
        }

        private boolean hasFluidsRange(FluidStack[] requirements, int multiplier,
                                       int start, int length) {
            FluidStack[] simulated = copyFluidState();
            return consumeFromRange(requirements, simulated, multiplier, start, length);
        }

        private void consume(FluidStack[] requirements, int multiplier) {
            consumeFrom(requirements, fluids, multiplier);
        }

        private void consumeRange(FluidStack[] requirements, int multiplier,
                                  int start, int length) {
            consumeFromRange(requirements, fluids, multiplier, start, length);
        }

        private boolean canAccept(FluidStack[] outputs) {
            return canAccept(outputs, 1);
        }

        private boolean canAccept(FluidStack[] outputs, int multiplier) {
            FluidStack[] simulated = copyFluidState();
            for (FluidStack output : outputs) {
                if (output == null) continue;
                long remaining = (long) Math.max(0, output.amount) * multiplier;
                while (remaining > 0L) {
                    FluidStack chunk = output.copy();
                    chunk.amount = (int) Math.min(Integer.MAX_VALUE, remaining);
                    int filled = fillInto(chunk, simulated, tankCount(), capacity());
                    if (filled < chunk.amount) return false;
                    remaining -= chunk.amount;
                }
            }
            return true;
        }

        private void addAll(FluidStack[] outputs) {
            for (FluidStack output : outputs) if (output != null) fill(output, true);
        }

        private boolean consumeFrom(FluidStack[] requirements, FluidStack[] state, int multiplier) {
            return consumeFromRange(requirements, state, multiplier, 0, tankCount());
        }

        private boolean consumeFromRange(FluidStack[] requirements, FluidStack[] state, int multiplier,
                                         int start, int length) {
            int begin = Math.max(0, start);
            int end = Math.min(tankCount(), begin + Math.max(0, length));
            for (FluidStack requirement : requirements) {
                if (requirement == null) continue;
                long remaining = Math.max(0L, (long) requirement.amount * multiplier);
                boolean foundType = false;
                for (int i = begin; i < end; i++) {
                    FluidStack stored = state[i];
                    if (stored == null || !stored.isFluidEqual(requirement)) continue;
                    foundType = true;
                    if (remaining <= 0) break;
                    int used = (int) Math.min(remaining, stored.amount);
                    stored.amount -= used;
                    remaining -= used;
                    if (stored.amount <= 0) state[i] = null;
                }
                if (!foundType || remaining > 0) return false;
            }
            return true;
        }

        private int fillInto(FluidStack resource, FluidStack[] state, int count, int capacity) {
            int remaining = resource.amount;
            for (int i = 0; i < count && remaining > 0; i++) {
                FluidStack stored = state[i];
                if (stored == null || !stored.isFluidEqual(resource)) continue;
                int moved = Math.min(remaining, Math.max(0, capacity - stored.amount));
                stored.amount += moved;
                remaining -= moved;
            }
            for (int i = 0; i < count && remaining > 0; i++) {
                if (state[i] != null) continue;
                int moved = Math.min(remaining, capacity);
                state[i] = resource.copy();
                state[i].amount = moved;
                remaining -= moved;
            }
            return resource.amount - remaining;
        }

        private FluidStack[] copyFluidState() {
            FluidStack[] result = new FluidStack[MAX_TANKS];
            for (int i = 0; i < fluids.length; i++) result[i] = fluids[i] == null ? null : fluids[i].copy();
            return result;
        }

        private void writeToNBT(NBTTagCompound nbt) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < fluids.length; i++) {
                if (fluids[i] == null) continue;
                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("Tank", i);
                fluids[i].writeToNBT(entry);
                list.appendTag(entry);
            }
            nbt.setTag("Tanks", list);
        }

        private void readFromNBT(NBTTagCompound nbt) {
            for (int i = 0; i < fluids.length; i++) fluids[i] = null;
            if (nbt.hasKey("Tanks")) {
                NBTTagList list = nbt.getTagList("Tanks", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound entry = list.getCompoundTagAt(i);
                    int tank = entry.getInteger("Tank");
                    if (tank >= 0 && tank < fluids.length) fluids[tank] = FluidStack.loadFluidStackFromNBT(entry);
                }
            } else {
                // Migration from the original single TieredFluidTank save format.
                fluids[0] = FluidStack.loadFluidStackFromNBT(nbt);
            }
        }
    }

    /** Physical gateway view: its fill/drain operations cannot cross into a neighbouring gateway's tanks. */
    private final class GatewayFluidView implements IFluidHandler {
        private final ChemicalFluidBuffer buffer;
        private final int start;
        private final int length;
        private final boolean input;

        private GatewayFluidView(ChemicalFluidBuffer buffer, int start, int length, boolean input) {
            this.buffer = buffer;
            this.start = start;
            this.length = length;
            this.input = input;
        }

        @Override public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
            if (!input) return 0;
            int filled = buffer.fillRange(resource, doFill, start, length);
            if (filled > 0 && doFill) markControllerDirtyAndSync();
            return filled;
        }
        @Override public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
            if (input || resource == null) return null;
            FluidStack drained = buffer.drainRange(resource, resource.amount, doDrain, start, length);
            if (drained != null && doDrain) markControllerDirtyAndSync();
            return drained;
        }
        @Override public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
            if (input) return null;
            FluidStack drained = buffer.drainRange(maxDrain, doDrain, start, length);
            if (drained != null && doDrain) markControllerDirtyAndSync();
            return drained;
        }
        @Override public boolean canFill(ForgeDirection from, Fluid fluid) {
            return input && buffer.canFillRange(fluid, start, length);
        }
        @Override public boolean canDrain(ForgeDirection from, Fluid fluid) {
            return !input && buffer.canDrainRange(fluid, start, length);
        }
        @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) {
            return buffer.getTankInfoRange(start, length);
        }
    }

    private static final class ModifierInventory implements IItemHandlerModifiable {
        private final MultiTileChemicalPlant tile;
        private ModifierInventory(MultiTileChemicalPlant tile) { this.tile = tile; }
        @Override public int getSlots() { return 2; }
        @Override public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < 2 ? tile.inventory[slot] : null;
        }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (slot >= 0 && slot < 2) tile.setInventorySlotContents(slot, stack);
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return tile.isItemValidForSlot(slot, stack);
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack == null || !isItemValid(slot, stack) || getStackInSlot(slot) != null) return stack;
            ItemStack inserted = stack.copy();
            inserted.stackSize = 1;
            ItemStack remainder = stack.copy();
            remainder.stackSize--;
            if (!simulate) setStackInSlot(slot, inserted);
            return remainder.stackSize <= 0 ? null : remainder;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack current = getStackInSlot(slot);
            if (current == null || amount <= 0) return null;
            ItemStack extracted = current.copy();
            extracted.stackSize = Math.min(amount, current.stackSize);
            if (!simulate) tile.decrStackSize(slot, extracted.stackSize);
            return extracted;
        }
    }
}
