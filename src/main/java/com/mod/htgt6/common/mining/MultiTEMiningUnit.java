package com.mod.htgt6.common.mining;

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
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.MultiTileEntityBase;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.api.hitech6.multiblock.TieredFluidTank;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import com.mod.htgt6.common.ui.MachineUiTheme;
import gregapi.data.FL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.RM;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.OreDictManager;
import gregapi.oredict.OreDictMaterial;
import gregapi.recipes.Recipe;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** HTGT6-tiered, GT6-fluid-aware 5x12x5 mining multiblock controller. */
public final class MultiTEMiningUnit extends MultiTileEntityBase
        implements IFluidHandler, IGuiHolder {
    public static final int SLOT_MODULE = 0;
    public static final int SLOT_TRANSFORMER = 1;
    public static final int INPUT_SLOT_START = 2;
    public static final int INPUT_SLOT_COUNT = 32;
    public static final int OUTPUT_SLOT_START = INPUT_SLOT_START + INPUT_SLOT_COUNT;
    public static final int OUTPUT_SLOT_COUNT = 32;
    private static final int[] INPUT_GATEWAY_SLOTS = makeSlotRange(INPUT_SLOT_START, INPUT_SLOT_COUNT);
    private static final int[] OUTPUT_GATEWAY_SLOTS = makeSlotRange(OUTPUT_SLOT_START, OUTPUT_SLOT_COUNT);
    public static final int LUBRICANT_PER_ORE = 1000;
    private static final int SCAN_BUDGET = 256;
    private static final int BASE_OPERATION_TICKS = 240;
    private static final int MIN_OPERATION_TICKS = 6;
    private static final int MINING_STATE_VERSION = 2;
    private static final int MAX_PERSISTED_TARGETS = 65536;

    private final TieredFluidTank lubricantTank = new TieredFluidTank(this, GatewayType.FLUID_INPUT);
    private final TieredFluidTank recipeOutputTank = new TieredFluidTank(this, GatewayType.FLUID_OUTPUT);
    private final IFluidHandler lubricantInputHandler = new LubricantInputGateway();
    private final IFluidHandler recipeOutputHandler = new RecipeFluidOutputGateway();
    private final IItemHandlerModifiable modifierInventory = new ModifierInventory(this);
    private boolean miningActive;
    private int scanCursor;
    private int scanTotal;
    private int scanBottomY;
    private int scannedLayerCount;
    private int activeMiningY = -1;
    private int shaftExcavationIndex;
    private boolean shaftLayerPrepared;
    private int scannedAreaSize;
    private int bufferedOreCount;
    private int nextTargetIndex;
    private long minedOres;
    private long currentEnergyUsage;
    private boolean scanComplete;
    private boolean replaceWithStone;
    private boolean energyPanelOpen;
    private int radiusTier;
    private long radiusPreviewUntil;
    private final List<MiningTarget> oreBuffer = new ArrayList<MiningTarget>();
    private MiningTarget currentTarget;
    private List<ItemStack> currentDrops = new ArrayList<ItemStack>();
    private List<FluidStack> currentFluidDrops = new ArrayList<FluidStack>();
    private List<FluidStack> preparedFluidDrops = new ArrayList<FluidStack>();
    private String currentMiningName = "";
    private int currentMiningAmount;
    private transient boolean writingNetworkSnapshot;

    public MultiTEMiningUnit() {
        super(OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT);
        configureFluidGateway(GatewayType.FLUID_INPUT, lubricantInputHandler);
        configureFluidGateway(GatewayType.FLUID_OUTPUT, recipeOutputHandler);
        configureItemGateway(GatewayType.ITEM_INPUT, this, INPUT_GATEWAY_SLOTS);
        configureItemGateway(GatewayType.ITEM_OUTPUT, this, OUTPUT_GATEWAY_SLOTS);
        maxEnergy = 1_100_000_000_000_000_000L;
    }

    @Override public int getModuleSlot() { return SLOT_MODULE; }
    @Override public int getTransformerSlot() { return SLOT_TRANSFORMER; }
    @Override public String getInventoryName() { return "MultiTE Mining Unit"; }
    @Override protected Block resolveGatewayCasingBlock() { return MiningUnitContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return 0; }
    public boolean isMiningActive() { return miningActive; }
    public int getActiveMiningY() { return activeMiningY; }
    public int getLubricantAmount() { return lubricantTank.getFluidAmount(); }
    public long getMinedOres() { return minedOres; }

    @Override
    public void updateEntity() {
        boolean disabledServer = worldObj != null && !worldObj.isRemote && !enabled;
        if (disabledServer) {
            setMiningActive(false);
            currentEnergyUsage = 0L;
        }
        super.updateEntity();
    }

    @Override
    protected void processMachineLogic() {
        currentEnergyUsage = 0L;
        if (!structureFormed) {
            setMiningActive(false);
            progress = 0;
            return;
        }
        if (scannedAreaSize != getWorkAreaSize()) resetScan();
        if (!scanComplete) {
            setMiningActive(false);
            progress = 0;
            scanWorkingArea(SCAN_BUDGET);
            return;
        }
        if (currentTarget == null && !selectNextBufferedOre()) {
            setMiningActive(false);
            progress = 0;
            // An exhausted layer schedules an incremental scan of Y - 1.
            if (!scanComplete) return;
            diagnostic = "IDLE: ORE BUFFER EMPTY";
            return;
        }
        if (lubricantTank.getFluidAmount() < LUBRICANT_PER_ORE) {
            setMiningActive(false);
            diagnostic = "WAITING: 1000 L LUBRICANT";
            return;
        }
        if (!canStoreDrops(currentDrops)) {
            setMiningActive(false);
            diagnostic = "WAITING: OUTPUT HATCH FULL";
            return;
        }
        if (!canStoreFluidDrops(currentFluidDrops)) {
            setMiningActive(false);
            diagnostic = "WAITING: FLUID OUTPUT HATCH FULL";
            return;
        }
        long euPerTick = Math.max(32L, (long) getMaxInputVoltage());
        if (energy < euPerTick) {
            setMiningActive(false);
            diagnostic = "WAITING: ENERGY";
            return;
        }
        maxProgress = getOperationTicks();
        energy -= euPerTick;
        currentEnergyUsage = euPerTick;
        progress++;
        markDirty();
        setMiningActive(true);
        diagnostic = "MINING: " + currentMiningName;
        if (progress >= maxProgress) finishCurrentOre();
        else if ((progress & 7) == 0) markDirtyAndSync();
    }

    private int getOperationTicks() {
        int shift = Math.max(0, Math.min(30, machineTier - 1));
        return Math.max(MIN_OPERATION_TICKS, (BASE_OPERATION_TICKS + (1 << shift) - 1) >> shift);
    }

    @Override
    protected MultiblockStructureDefinition createStructureDefinition() {
        MultiblockStructureDefinition.Builder builder = MultiblockStructureDefinition.builder(
                -2, 2, 0, 11, 0, 4, new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        return matchStructurePosition(context);
                    }
                });
        builder.requireGateway(GatewayType.FLUID_INPUT, 1)
                .requireGateway(GatewayType.FLUID_OUTPUT, 1)
                .requireGateway(GatewayType.ITEM_INPUT, 1)
                .requireGateway(GatewayType.ITEM_OUTPUT, 1)
                .requireGateway(GatewayType.ENERGY_INPUT, 1);
        return builder.build();
    }

    private MultiblockStructureDefinition.Match matchStructurePosition(
            MultiblockStructureDefinition.Context context) {
        int u = context.u, y = context.y, v = context.v;
        Block block = context.block;
        int meta = context.metadata;
        boolean centerController = y == 0 && u == 0 && v == 0;
        if (centerController) return block == MiningUnitContent.controller
                ? MultiblockStructureDefinition.Match.valid("controller")
                : MultiblockStructureDefinition.Match.invalid();

        if (y == 0) {
            if (block == MiningUnitContent.casing && (meta == 0 || meta == 1)) {
                return MultiblockStructureDefinition.Match.valid(meta == 1 ? "diagnostic_casing" : "casing");
            }
            return block == MiningUnitContent.port && context.getGateway() != null
                    ? MultiblockStructureDefinition.Match.valid("gateway." + context.getGatewayType().serializedName)
                    : MultiblockStructureDefinition.Match.invalid();
        }
        if (y >= 1 && y <= 3) {
            // Four 3x3 reinforced-glass walls between the frame corner columns;
            // the internal 3x3 shaft remains empty for the animated auger.
            boolean corner = Math.abs(u) == 2 && (v == 0 || v == 4);
            boolean wall = Math.abs(u) == 2 || v == 0 || v == 4;
            if (corner) return match(block == MiningUnitContent.frame && meta == 0, "frame");
            if (wall) return match(block == MiningUnitContent.formedGlass || IC2ReinforcedGlass.matches(block, meta), "glass");
            return match(context.isAir(), "air");
        }
        if (y == 4) {
            if (block == MiningUnitContent.rib && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.mining(u,y,v))
                return match(true, "reinforced_rib");
            boolean ring = Math.abs(u) == 2 || v == 0 || v == 4;
            return ring ? match(block == MiningUnitContent.casing && meta == 0, "casing")
                    : match(context.isAir(), "air");
        }
        if (y == 5 || y == 6) {
            boolean ring = Math.abs(u) == 2 || v == 0 || v == 4;
            return ring ? match(block == MiningUnitContent.frame && meta == 0, "frame")
                    : match(context.isAir(), "air");
        }
        if (y == 7 || y == 8) {
            boolean inside = Math.abs(u) <= 1 && v >= 1 && v <= 3;
            boolean ring = inside && (Math.abs(u) == 1 || v == 1 || v == 3);
            return ring ? match(block == MiningUnitContent.frame && meta == 0, "frame")
                    : match(context.isAir(), "air");
        }
        if (y == 9 || y == 10) {
            return u == 0 && v == 2 ? match(block == MiningUnitContent.frame && meta == 0, "frame")
                    : match(context.isAir(), "air");
        }
        return u == 0 && v == 2 ? match(block == MiningUnitContent.frame && meta == 1, "quarter_frame")
                : match(context.isAir(), "air");
    }

    private static MultiblockStructureDefinition.Match match(boolean valid, String key) {
        return valid ? MultiblockStructureDefinition.Match.valid(key)
                : MultiblockStructureDefinition.Match.invalid();
    }

    @Override
    protected void onStructureFormed(boolean newlyFormed, MultiblockStructureDefinition.Result result) {
        setGlassFormed(true);
        if (newlyFormed) resetScan();
    }

    @Override
    protected void onStructureInvalidated(boolean wasFormed, MultiblockStructureDefinition.Result result) {
        setGlassFormed(false);
        setMiningActive(false);
        clearCurrentTarget();
    }

    public void restoreReinforcedGlass() {
        setGlassFormed(false);
    }

    private void setGlassFormed(boolean formed) {
        if (worldObj == null || worldObj.isRemote) return;
        for (int y = 1; y <= 3; y++) {
            for (int u = -2; u <= 2; u++) {
                for (int v = 0; v <= 4; v++) {
                    boolean corner = Math.abs(u) == 2 && (v == 0 || v == 4);
                    boolean wall = Math.abs(u) == 2 || v == 0 || v == 4;
                    if (!wall || corner) continue;
                    int[] p = localToWorld(u, y, v);
                    Block current = worldObj.getBlock(p[0], p[1], p[2]);
                    int meta = worldObj.getBlockMetadata(p[0], p[1], p[2]);
                    if (formed && IC2ReinforcedGlass.matches(current, meta)) {
                        worldObj.setBlock(p[0], p[1], p[2], MiningUnitContent.formedGlass, 1, 2);
                    } else if (current == MiningUnitContent.formedGlass && meta != (formed ? 1 : 0)) {
                        worldObj.setBlockMetadataWithNotify(p[0], p[1], p[2], formed ? 1 : 0, 2);
                    }
                }
            }
        }
    }

    /** Selected field starts at 16x16 and may grow up to the installed controller tier. */
    public int getWorkAreaSize() {
        int available = Math.max(0, machineTier - 1);
        return 16 + 6 * Math.max(0, Math.min(radiusTier, available));
    }

    public boolean shouldRenderWorkArea() {
        return worldObj != null && worldObj.getTotalWorldTime() <= radiusPreviewUntil;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int half = getWorkAreaSize() / 2 + 2;
        return AxisAlignedBB.getBoundingBox(xCoord - half, 0, zCoord - half,
                xCoord + half + 1, yCoord + 13, zCoord + half + 1);
    }

    @Override public double getMaxRenderDistanceSquared() { return 65536.0D; }

    private void scanWorkingArea(int budget) {
        int side = getWorkAreaSize();
        if (scanTotal <= 0) {
            scanBottomY = findBottomMiningY();
            scannedLayerCount = Math.max(1, yCoord - scanBottomY + 1);
            activeMiningY = yCoord;
            shaftExcavationIndex = 0;
            shaftLayerPrepared = true; // Never excavate the multiblock's own base layer.
            scanTotal = side * side;
            scannedAreaSize = side;
        }
        if (!prepareShaftLayer()) return;
        int[] center = localToWorld(0, 0, 2);
        int half = side / 2;
        for (int checked = 0; checked < budget && scanCursor < scanTotal;) {
            int index = scanCursor;
            int dx = index % side - half;
            int dz = index / side - half;
            int x = center[0] + dx;
            int z = center[2] + dz;
            int y = activeMiningY;
            // Never consume a scan coordinate while its chunk is absent; otherwise
            // every lower layer would silently miss the same part of the work area.
            if (!worldObj.getChunkProvider().chunkExists(x >> 4, z >> 4)) break;
            scanCursor++;
            checked++;
            if (!worldObj.blockExists(x, y, z)) continue;
            Block block = worldObj.getBlock(x, y, z);
            int meta = worldObj.getBlockMetadata(x, y, z);
            ItemStack oreStack = getOreStackAt(x, y, z, block, meta);
            if (oreStack != null) oreBuffer.add(new MiningTarget(x, y, z, block, meta, oreStack));
        }
        bufferedOreCount = oreBuffer.size();
        markDirty();
        if (scanCursor >= scanTotal) {
            scanComplete = true;
            nextTargetIndex = 0;
            diagnostic = "LAYER SCAN COMPLETE: Y=" + activeMiningY + " / " + bufferedOreCount;
        } else {
            diagnostic = "SCANNING LAYER Y=" + activeMiningY + ": " + getScanPercent() + "% / " + bufferedOreCount;
        }
        if (scanComplete || (scanCursor & 2047) == 0) markDirtyAndSync();
    }

    /** Cheap vertical pre-scan: the mining volume ends immediately above the first bedrock block. */
    private int findBottomMiningY() {
        int[] center = localToWorld(0, 0, 2);
        for (int y = yCoord; y >= 0; y--) {
            if (!worldObj.blockExists(center[0], y, center[2])) continue;
            if (worldObj.getBlock(center[0], y, center[2]) == Blocks.bedrock) return Math.min(yCoord, y + 1);
        }
        return 0;
    }

    /**
     * Opens exactly one 3x3 plane below the structure before that Y layer is scanned.
     * At most one solid block is removed per tick so output checks and animation remain observable.
     */
    private boolean prepareShaftLayer() {
        if (shaftLayerPrepared || activeMiningY >= yCoord) return true;
        int[] center = localToWorld(0, 0, 2);
        while (shaftExcavationIndex < 9) {
            int index = shaftExcavationIndex;
            int x = center[0] + index % 3 - 1;
            int z = center[2] + index / 3 - 1;
            int y = activeMiningY;
            if (!worldObj.getChunkProvider().chunkExists(x >> 4, z >> 4)) {
                diagnostic = "WAITING: SHAFT CHUNK UNLOADED";
                setMiningActive(false);
                return false;
            }
            Block block = worldObj.getBlock(x, y, z);
            if (block == null || block.isAir(worldObj, x, y, z)) {
                shaftExcavationIndex++;
                continue;
            }
            if (block == Blocks.bedrock) {
                shaftExcavationIndex++;
                continue;
            }
            TileEntity tile = worldObj.getTileEntity(x, y, z);
            if (tile != null && !(block instanceof PrefixBlock && tile instanceof PrefixBlockTileEntity)) {
                diagnostic = "SHAFT BLOCKED BY TILE ENTITY";
                setMiningActive(false);
                return false;
            }
            if (block.getBlockHardness(worldObj, x, y, z) < 0.0F) {
                diagnostic = "SHAFT BLOCKED BY UNBREAKABLE BLOCK";
                setMiningActive(false);
                return false;
            }
            List<ItemStack> drops = getShaftBlockDrops(x, y, z, block, worldObj.getBlockMetadata(x, y, z));
            if (!canStoreDrops(drops)) {
                diagnostic = "WAITING: OUTPUT HATCH FULL";
                setMiningActive(false);
                return false;
            }
            long euPerTick = Math.max(32L, (long) getMaxInputVoltage());
            if (energy < euPerTick) {
                diagnostic = "WAITING: ENERGY";
                setMiningActive(false);
                return false;
            }
            energy -= euPerTick;
            currentEnergyUsage = euPerTick;
            worldObj.setBlockToAir(x, y, z);
            for (ItemStack drop : drops) if (drop != null && drop.stackSize > 0) storeDrop(drop.copy());
            shaftExcavationIndex++;
            diagnostic = "EXCAVATING SHAFT: Y=" + activeMiningY;
            setMiningActive(true);
            markDirtyAndSync();
            return false;
        }
        shaftLayerPrepared = true;
        setMiningActive(false);
        markDirtyAndSync();
        return true;
    }

    private List<ItemStack> getShaftBlockDrops(int x, int y, int z, Block block, int meta) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        ItemStack oreStack = getOreStackAt(x, y, z, block, meta);
        if (isSmallOre(block, oreStack)) {
            addDrops(drops, block.getDrops(worldObj, x, y, z, meta, 3));
            return drops;
        }
        if (block instanceof PrefixBlock) {
            ItemStack stack = ((PrefixBlock) block).getItemStackFromBlock(worldObj, x, y, z, (byte) 0);
            if (stack != null && stack.getItem() != null) drops.add(stack.copy());
            return drops;
        }
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            drops.add(new ItemStack(item, 1, block.damageDropped(meta)));
            return drops;
        }
        addDrops(drops, block.getDrops(worldObj, x, y, z, meta, 0));
        return drops;
    }

    private int getScanPercent() {
        return Math.min(100, (int) (scanCursor * 100L / Math.max(1, scanTotal)));
    }

    private void resetScan() {
        oreBuffer.clear();
        clearCurrentTarget();
        scanCursor = 0;
        scanTotal = 0;
        scanBottomY = 0;
        scannedLayerCount = 0;
        activeMiningY = -1;
        shaftExcavationIndex = 0;
        shaftLayerPrepared = false;
        scannedAreaSize = getWorkAreaSize();
        bufferedOreCount = 0;
        nextTargetIndex = 0;
        scanComplete = false;
        diagnostic = "SCANNING: 0% / 0";
        setMiningActive(false);
        markDirtyAndSync();
    }

    private boolean selectNextBufferedOre() {
        while (nextTargetIndex < oreBuffer.size()) {
            MiningTarget target = oreBuffer.get(nextTargetIndex++);
            if (!isValidOreTarget(target)) {
                bufferedOreCount = Math.max(0, bufferedOreCount - 1);
                continue;
            }
            List<ItemStack> outputs = getMiningOutputs(target);
            if (outputs.isEmpty()) {
                bufferedOreCount = Math.max(0, bufferedOreCount - 1);
                continue;
            }
            currentTarget = target;
            activeMiningY = target.y;
            currentDrops = outputs;
            currentFluidDrops = copyFluidList(preparedFluidDrops);
            currentMiningName = outputs.get(0).getDisplayName();
            currentMiningAmount = outputs.get(0).stackSize;
            progress = 0;
            maxProgress = getOperationTicks();
            return true;
        }
        advanceToNextLayer();
        return false;
    }

    private void advanceToNextLayer() {
        oreBuffer.clear();
        bufferedOreCount = 0;
        nextTargetIndex = 0;
        clearCurrentTarget();
        if (activeMiningY > scanBottomY) {
            activeMiningY--;
            shaftExcavationIndex = 0;
            shaftLayerPrepared = false;
            scanCursor = 0;
            scanTotal = getWorkAreaSize() * getWorkAreaSize();
            scanComplete = false;
            diagnostic = "SCANNING NEXT LAYER: Y=" + activeMiningY;
        } else {
            activeMiningY = -1;
            scanComplete = true;
            diagnostic = "IDLE: ALL LAYERS COMPLETE";
        }
        markDirtyAndSync();
    }

    private boolean isValidOreTarget(MiningTarget target) {
        if (target == null || !worldObj.blockExists(target.x, target.y, target.z)) return false;
        Block block = worldObj.getBlock(target.x, target.y, target.z);
        int meta = worldObj.getBlockMetadata(target.x, target.y, target.z);
        if (block != target.block || meta != target.meta) return false;
        ItemStack current = getOreStackAt(target.x, target.y, target.z, block, meta);
        return current != null && current.isItemEqual(target.oreStack)
                && ItemStack.areItemStackTagsEqual(current, target.oreStack);
    }

    private List<ItemStack> getMiningOutputs(MiningTarget target) {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        preparedFluidDrops = new ArrayList<FluidStack>();
        ItemStack oreStack = target.oreStack == null ? null : target.oreStack.copy();
        if (oreStack != null) oreStack.stackSize = 1;
        // Small ores use their native pickaxe drops. Fortune is deliberately
        // restricted to this branch and never affects normal/dense Crusher ore.
        if (isSmallOre(target.block, oreStack)) {
            addDrops(result, target.block.getDrops(worldObj, target.x, target.y, target.z, target.meta, 3));
            return result;
        }
        boolean usedCrusherRecipe = false;
        if (oreStack != null) {
            Recipe recipe = RM.Crusher.findRecipe(null, null, true, Long.MAX_VALUE, null,
                    new FluidStack[0], oreStack);
            if (recipe != null && recipe.mEnabled && !recipe.mFakeRecipe) {
                ItemStack[] outputs = recipe.getOutputs(worldObj.rand);
                if (outputs != null) {
                    for (ItemStack output : outputs) {
                        if (output != null && output.stackSize > 0) result.add(output.copy());
                    }
                }
                usedCrusherRecipe = !result.isEmpty();
                if (usedCrusherRecipe) {
                    FluidStack[] fluidOutputs = recipe.getFluidOutputs(worldObj.rand);
                    if (fluidOutputs != null) {
                        for (FluidStack output : fluidOutputs) {
                            if (output != null && output.amount > 0) preparedFluidDrops.add(output.copy());
                        }
                    }
                }
            }
        }
        if (usedCrusherRecipe) {
            addHostRockDustChance(oreStack, result);
            return result;
        }
        if (oreStack != null) {
            result.add(oreStack.copy());
            return result;
        }
        List<ItemStack> fallback = target.block.getDrops(worldObj, target.x, target.y, target.z, target.meta, 0);
        if (fallback != null) for (ItemStack drop : fallback) if (drop != null && drop.stackSize > 0) result.add(drop.copy());
        return result;
    }

    private static boolean isSmallOre(Block block, ItemStack stack) {
        if (block instanceof PrefixBlock) {
            String prefix = ((PrefixBlock) block).mPrefix.mNameInternal;
            if (prefix != null && prefix.equalsIgnoreCase("oreSmall")) return true;
        }
        if (stack == null) return false;
        OreDictItemData data = OreDictManager.INSTANCE.getItemData(stack);
        if (data != null && data.mPrefix != null
                && "oreSmall".equalsIgnoreCase(data.mPrefix.mNameInternal)) return true;
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if (name != null && name.toLowerCase(Locale.ROOT).startsWith("oresmall")) return true;
        }
        return false;
    }

    private static void addDrops(List<ItemStack> target, List<ItemStack> drops) {
        if (drops == null) return;
        for (ItemStack drop : drops) {
            if (drop != null && drop.stackSize > 0) target.add(drop.copy());
        }
    }

    /** 25% bonus dust of the stone material encoded by the GT6 ore prefix. */
    private void addHostRockDustChance(ItemStack oreStack, List<ItemStack> outputs) {
        if (oreStack == null || worldObj.rand.nextInt(4) != 0) return;
        OreDictItemData data = OreDictManager.INSTANCE.getItemData(oreStack);
        if (data == null || data.mPrefix == null) return;
        OreDictMaterial host = getHostRockMaterial(data.mPrefix.mNameInternal);
        if (host == null) return;
        ItemStack dust = OP.dust.mat(host, 1);
        if (dust == null || dust.stackSize <= 0) return;
        mergeOutput(outputs, dust);
    }

    private static void mergeOutput(List<ItemStack> outputs, ItemStack addition) {
        for (ItemStack output : outputs) {
            if (output != null && output.isItemEqual(addition)
                    && ItemStack.areItemStackTagsEqual(output, addition)
                    && output.stackSize + addition.stackSize <= output.getMaxStackSize()) {
                output.stackSize += addition.stackSize;
                return;
            }
        }
        outputs.add(addition.copy());
    }

    private static OreDictMaterial getHostRockMaterial(String prefix) {
        if (prefix == null) return null;
        if (prefix.equals("oreBlackgranite")) return MT.GraniteBlack;
        if (prefix.equals("oreRedgranite")) return MT.GraniteRed;
        if (prefix.equals("oreVanillagranite")) return MT.Granite;
        if (prefix.equals("oreAndesite")) return MT.Andesite;
        if (prefix.equals("oreDiorite")) return MT.Diorite;
        if (prefix.equals("oreBlackstone")) return MT.Blackstone;
        if (prefix.equals("oreHolystone")) return MT.Holystone;
        if (prefix.equals("oreLivingrock")) return MT.Livingrock;
        if (prefix.equals("oreDeadrock")) return MT.Deadrock;
        if (prefix.equals("oreBetweenstone")) return MT.Betweenstone;
        if (prefix.equals("orePitstone")) return MT.Pitstone;
        if (prefix.equals("oreUmberstone")) return MT.Umber;
        if (prefix.equals("oreKomatiite")) return MT.Komatiite;
        if (prefix.equals("oreBasalt")) return MT.Basalt;
        if (prefix.equals("oreMarble")) return MT.Marble;
        if (prefix.equals("oreLimestone")) return MT.Limestone;
        if (prefix.equals("oreSiltstone")) return MT.Siltstone;
        if (prefix.equals("oreShale")) return MT.Shale;
        if (prefix.equals("oreSlate")) return MT.Slate;
        if (prefix.equals("oreDeepslate"))
            return com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.DEEPSLATE;
        if (prefix.equals("oreGreenschist")) return MT.Greenstone;
        if (prefix.equals("oreBlueschist")) return MT.Bluestone;
        if (prefix.equals("oreLightprismarine")) return MT.PrismarineLight;
        if (prefix.equals("oreDarkprismarine")) return MT.PrismarineDark;
        if (prefix.equals("oreKimberlite")) return MT.Kimberlite;
        if (prefix.equals("oreQuartzite")) return MT.Quartzite;
        if (prefix.equals("oreNetherrack") || prefix.equals("oreNether")) return MT.Netherrack;
        if (prefix.equals("oreEndstone") || prefix.equals("oreEnd") || prefix.equals("oreHee")) return MT.Endstone;
        if (prefix.equals("oreSandstone") || prefix.equals("oreSand") || prefix.equals("oreRedSand")
                || prefix.equals("oreStrangesand")) return MT.Sand;
        if (prefix.equals("ore") || prefix.equals("oreVanillastone") || prefix.equals("oreNormal")
                || prefix.equals("oreSmall") || prefix.equals("orePoor") || prefix.equals("oreRich")
                || prefix.equals("oreDense") || prefix.equals("oreGravel")) return MT.Stone;
        return null;
    }

    private void finishCurrentOre() {
        if (!isValidOreTarget(currentTarget)) {
            bufferedOreCount = Math.max(0, bufferedOreCount - 1);
            clearCurrentTarget();
            progress = 0;
            setMiningActive(false);
            return;
        }
        if (replaceWithStone) worldObj.setBlock(currentTarget.x, currentTarget.y, currentTarget.z, Blocks.stone, 0, 3);
        else worldObj.setBlockToAir(currentTarget.x, currentTarget.y, currentTarget.z);
        for (ItemStack drop : currentDrops) if (drop != null && drop.stackSize > 0) storeDrop(drop.copy());
        for (FluidStack drop : currentFluidDrops) {
            if (drop != null && drop.amount > 0) recipeOutputTank.fill(drop.copy(), true);
        }
        lubricantTank.drain(LUBRICANT_PER_ORE, true);
        minedOres++;
        bufferedOreCount = Math.max(0, bufferedOreCount - 1);
        diagnostic = "MINED: " + currentMiningName;
        clearCurrentTarget();
        progress = 0;
        setMiningActive(false);
        markDirtyAndSync();
    }

    private void clearCurrentTarget() {
        currentTarget = null;
        currentDrops = new ArrayList<ItemStack>();
        currentFluidDrops = new ArrayList<FluidStack>();
        preparedFluidDrops = new ArrayList<FluidStack>();
        currentMiningName = "";
        currentMiningAmount = 0;
    }

    private ItemStack getOreStackAt(int x, int y, int z, Block block, int meta) {
        if (block == null || block.isAir(worldObj, x, y, z)) return null;
        TileEntity tile = worldObj.getTileEntity(x, y, z);
        if (block instanceof PrefixBlock) {
            PrefixBlock prefixBlock = (PrefixBlock) block;
            if (!(tile instanceof PrefixBlockTileEntity) || !isMineableOrePrefix(prefixBlock.mPrefix.mNameInternal)) {
                return null;
            }
            ItemStack stack = prefixBlock.getItemStackFromBlock(worldObj, x, y, z, (byte) 0);
            return stack == null || stack.getItem() == null ? null : stack;
        }
        // Non-prefix TileEntities remain forbidden. This keeps machines and
        // inventories safe even if their block names accidentally contain "ore".
        if (tile != null) return null;
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ItemStack stack = new ItemStack(item, 1, meta);
            OreDictItemData data = OreDictManager.INSTANCE.getItemData(stack);
            if (data != null && data.mPrefix != null && isMineableOrePrefix(data.mPrefix.mNameInternal)) return stack;
            for (int id : OreDictionary.getOreIDs(stack)) {
                String name = OreDictionary.getOreName(id);
                if (isMineableOrePrefix(name)) return stack;
            }
        }
        String name = block.getUnlocalizedName();
        String lowerName = name == null ? "" : name.toLowerCase();
        String className = block.getClass().getSimpleName().toLowerCase();
        return item != null && (lowerName.startsWith("tile.ore") || lowerName.contains("_ore") || lowerName.contains(".ore")
                || className.startsWith("ore") || className.contains("blockore"))
                ? new ItemStack(item, 1, meta) : null;
    }

    private static boolean isMineableOrePrefix(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.startsWith("ore") && !lower.startsWith("oreberry")
                && !lower.startsWith("orebush") && !lower.startsWith("orebedrock");
    }

    private boolean canStoreDrops(List<ItemStack> drops) {
        ItemStack[] simulation = new ItemStack[OUTPUT_SLOT_COUNT];
        for (int i = 0; i < simulation.length; i++) {
            ItemStack current = inventory[OUTPUT_SLOT_START + i];
            simulation[i] = current == null ? null : current.copy();
        }
        for (ItemStack drop : drops) if (drop != null && !insertInto(drop.copy(), simulation, 0)) return false;
        return true;
    }

    private boolean canStoreFluidDrops(List<FluidStack> drops) {
        FluidTank simulation = new FluidTank(recipeOutputTank.getCapacity());
        if (recipeOutputTank.getFluid() != null) simulation.setFluid(recipeOutputTank.getFluid().copy());
        for (FluidStack drop : drops) {
            if (drop != null && drop.amount > 0 && simulation.fill(drop.copy(), true) != drop.amount) return false;
        }
        return true;
    }

    private static List<FluidStack> copyFluidList(List<FluidStack> fluids) {
        ArrayList<FluidStack> result = new ArrayList<FluidStack>();
        if (fluids != null) {
            for (FluidStack fluid : fluids) if (fluid != null && fluid.amount > 0) result.add(fluid.copy());
        }
        return result;
    }

    private void storeDrop(ItemStack stack) { insertInto(stack, inventory, OUTPUT_SLOT_START); }

    private static boolean insertInto(ItemStack stack, ItemStack[] target, int offset) {
        for (int i = 0; i < OUTPUT_SLOT_COUNT && stack.stackSize > 0; i++) {
            int index = offset + i;
            ItemStack current = target[index];
            if (current != null && current.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(current, stack)) {
                int moved = Math.min(stack.stackSize, Math.min(64, current.getMaxStackSize()) - current.stackSize);
                if (moved > 0) { current.stackSize += moved; stack.stackSize -= moved; }
            }
        }
        for (int i = 0; i < OUTPUT_SLOT_COUNT && stack.stackSize > 0; i++) {
            int index = offset + i;
            if (target[index] == null) {
                int moved = Math.min(stack.stackSize, Math.min(64, stack.getMaxStackSize()));
                target[index] = stack.copy();
                target[index].stackSize = moved;
                stack.stackSize -= moved;
            }
        }
        return stack.stackSize <= 0;
    }

    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == SLOT_MODULE) return isTierModule(stack);
        if (slot == SLOT_TRANSFORMER) return isTransformer(stack);
        return slot >= INPUT_SLOT_START && slot < INPUT_SLOT_START + INPUT_SLOT_COUNT;
    }
    private static boolean isTierModule(ItemStack stack) {
        if (stack == null) return false;
        Item i = stack.getItem();
        return i == Tmodules.TierModuleMV || i == Tmodules.TierModuleHV || i == Tmodules.TierModuleEV
                || i == Tmodules.TierModuleIV || i == Tmodules.TierModuleLuV || i == Tmodules.TierModuleZPM
                || i == Tmodules.TierModuleUV || i == Tmodules.TierModulePUV1 || i == Tmodules.TierModuleUX
                || i == Tmodules.TierModuleOLV || i == Tmodules.TierModuleOMV || i == Tmodules.TierModuleOHV
                || i == Tmodules.TierModuleOEV || i == Tmodules.TierModuleOIV || i == Tmodules.TierModuleMAX;
    }
    private static boolean isTransformer(ItemStack stack) {
        if (stack == null) return false;
        Item i = stack.getItem();
        return i == Tmodules.LowLevelTransformer || i == Tmodules.MediumLevelTransformer
                || i == Tmodules.HighLevelTransformer || i == Tmodules.UltimateLevelTransformer
                || i == Tmodules.OmegaTransformer;
    }

    private static FluidStack lubricant(int amount) { return FL.Lubricant.make(amount); }
    @Override public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        FluidStack required = lubricant(1);
        if (resource == null || required == null || !resource.isFluidEqual(required)) return 0;
        int filled = lubricantTank.fill(resource, doFill);
        if (filled > 0 && doFill) markDirtyAndSync();
        return filled;
    }
    @Override public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || lubricantTank.getFluid() == null || !resource.isFluidEqual(lubricantTank.getFluid())) return null;
        FluidStack drained = lubricantTank.drain(resource.amount, doDrain);
        if (drained != null && doDrain) markDirtyAndSync();
        return drained;
    }
    @Override public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        FluidStack drained = lubricantTank.drain(maxDrain, doDrain);
        if (drained != null && doDrain) markDirtyAndSync();
        return drained;
    }
    @Override public boolean canFill(ForgeDirection from, Fluid fluid) {
        FluidStack required = lubricant(1);
        return required != null && required.getFluid() == fluid;
    }
    @Override public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return lubricantTank.getFluid() != null && (fluid == null || lubricantTank.getFluid().getFluid() == fluid);
    }
    @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) { return new FluidTankInfo[] { lubricantTank.getInfo() }; }

    @Override public ModularScreen createScreen(GuiCreationContext context, ModularPanel panel) { return new ModularScreen(panel); }
    @Override public ModularPanel buildUI(GuiCreationContext context, GuiSyncManager sync, boolean isClient) {
        sync.registerSlotGroup("mining_modifiers", 2);
        sync.syncValue("mining_formed", SyncHandlers.bool(() -> structureFormed, v -> structureFormed = v));
        sync.syncValue("mining_active", SyncHandlers.bool(() -> miningActive, v -> miningActive = v));
        sync.syncValue("mining_enabled", SyncHandlers.bool(() -> enabled, v -> enabled = v));
        sync.syncValue("mining_tier", SyncHandlers.intNumber(() -> machineTier, v -> machineTier = v));
        sync.syncValue("mining_progress", SyncHandlers.intNumber(() -> progress, v -> progress = v));
        sync.syncValue("mining_max_progress", SyncHandlers.intNumber(() -> maxProgress, v -> maxProgress = v));
        sync.syncValue("mining_energy", SyncHandlers.longNumber(() -> energy, v -> energy = v));
        sync.syncValue("mining_energy_usage", SyncHandlers.longNumber(
                () -> currentEnergyUsage, v -> currentEnergyUsage = Math.max(0L, v)));
        sync.syncValue("mining_lube", SyncHandlers.intNumber(lubricantTank::getFluidAmount, this::setSyncedLube));
        sync.syncValue("mining_count", SyncHandlers.longNumber(() -> minedOres, v -> minedOres = v));
        sync.syncValue("mining_diagnostic", SyncHandlers.string(() -> diagnostic, v -> diagnostic = v));
        sync.syncValue("mining_scan_cursor", SyncHandlers.intNumber(() -> scanCursor, v -> scanCursor = v));
        sync.syncValue("mining_scan_total", SyncHandlers.intNumber(() -> scanTotal, v -> scanTotal = v));
        sync.syncValue("mining_scan_layers", SyncHandlers.intNumber(() -> scannedLayerCount, v -> scannedLayerCount = v));
        sync.syncValue("mining_layer_y", SyncHandlers.intNumber(() -> activeMiningY, v -> activeMiningY = v));
        sync.syncValue("mining_shaft_index", SyncHandlers.intNumber(
                () -> shaftExcavationIndex, v -> shaftExcavationIndex = Math.max(0, Math.min(9, v))));
        sync.syncValue("mining_ore_count", SyncHandlers.intNumber(() -> bufferedOreCount, v -> bufferedOreCount = v));
        sync.syncValue("mining_scan_complete", SyncHandlers.bool(() -> scanComplete, v -> scanComplete = v));
        sync.syncValue("mining_replace_stone", SyncHandlers.bool(() -> replaceWithStone, v -> replaceWithStone = v));
        sync.syncValue("mining_radius_tier", SyncHandlers.intNumber(() -> radiusTier, v -> radiusTier = v));
        sync.syncValue("mining_radius_preview", SyncHandlers.longNumber(() -> radiusPreviewUntil, v -> radiusPreviewUntil = v));
        sync.syncValue("mining_current_name", SyncHandlers.string(() -> currentMiningName, v -> currentMiningName = v));
        sync.syncValue("mining_current_amount", SyncHandlers.intNumber(() -> currentMiningAmount, v -> currentMiningAmount = v));
        sync.syncValue("mining_power_action", new InteractionSyncHandler().setOnMouseTapped(data -> toggleMachinePower()));
        sync.syncValue("mining_rescan_action", new InteractionSyncHandler().setOnMouseTapped(data -> resetScan()));
        sync.syncValue("mining_replace_action", new InteractionSyncHandler().setOnMouseTapped(data -> toggleReplacementMode()));
        sync.syncValue("mining_radius_action", new InteractionSyncHandler().setOnMouseTapped(data -> cycleWorkRadius()));

        MachineUiTheme theme = MachineUiTheme.getActive();
        ModularPanel panel = ModularPanel.defaultPanel("multi_te_mining_unit", 330, 300)
                .background(new Rectangle().setColor(theme.getPanelColor()));
        frame(panel, theme.getFrameColor(), 0, 0, 330, 2);
        frame(panel, theme.getFrameColor(), 0, 298, 330, 2);
        frame(panel, theme.getFrameColor(), 0, 0, 2, 300);
        frame(panel, theme.getFrameColor(), 328, 0, 2, 300);
        frame(panel, theme.getTerminalColor(), 3, 3, 324, 1);
        frame(panel, theme.getTerminalColor(), 3, 296, 324, 1);
        frame(panel, theme.getTerminalColor(), 3, 3, 1, 293);
        frame(panel, theme.getTerminalColor(), 326, 3, 1, 293);
        panel.child(new TextWidget("MINING UNIT").pos(12, 8).color(theme.getTitleColor()));
        panel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.radius_compact", getWorkAreaSize(), getWorkAreaSize())))
                .pos(205, 8).size(68, 11).color(theme.getPrimaryColor()));
        if (isClient) {
            panel.child(com.mod.htgt6.client.GUI.MiningUnitModularUiButtons
                    .createStructureButton(this).pos(280, 7));
        }
        Widget terminal = new Widget().background(new Rectangle().setColor(theme.getTerminalColor()));
        terminal.pos(8, 24).size(314, 82);
        panel.child(terminal);
        panel.child(new TextWidget(IKey.dynamic(this::getStructureCompletionLine)).pos(16, 35).size(300, 13).color(0xFFFFFFFF));
        panel.child(new TextWidget(IKey.dynamic(this::getOreScanLine)).pos(16, 58).size(300, 13).color(theme.getPrimaryColor()));
        panel.child(new TextWidget(IKey.dynamic(this::getActivityLine)).pos(16, 81).size(300, 13).color(0xFFFFFFFF));

        final ParentWidget<?> energyPanel = new ParentWidget();
        energyPanel.pos(330, 24).size(178, 108).background(new Rectangle().setColor(theme.getTerminalColor()));
        energyPanel.child(new TextWidget(IKey.lang("ui.htgt6.mining_unit.energy_panel")).pos(7, 7).color(theme.getTitleColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.energy_tier", machineTier, getTierName()))).pos(7, 24).color(theme.getProductionColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.energy_buffer", formatEnergy(energy), formatEnergy(maxEnergy))))
                .pos(7, 39).color(theme.getEnergyColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.energy_input", formatEnergy(getMaxInputVoltage())))).pos(7, 54).color(theme.getEnergyColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.energy_usage", formatEnergy(currentEnergyUsage)))).pos(7, 69).color(theme.getEnergyColor()));
        energyPanel.child(new TextWidget(IKey.dynamic(() -> StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.operation_time", formatSeconds(getOperationTicks())))).pos(7, 84).color(theme.getLabelColor()));
        energyPanel.setEnabled(false);
        panel.child(energyPanel);

        panel.child(createModifierSlot(SLOT_MODULE, "ui.htgt6.mining_unit.tier_module").pos(76, 121));
        panel.child(createModifierSlot(SLOT_TRANSFORMER, "ui.htgt6.mining_unit.transformer").pos(100, 121));
        panel.child(createPowerButton().pos(132, 119));
        panel.child(createEnergyPanelButton(energyPanel).pos(160, 119));
        panel.child(createRescanButton().pos(188, 119));
        panel.child(createReplacementButton().pos(216, 119));
        panel.child(createRadiusButton().pos(244, 119));

        panel.child(new TextWidget(IKey.lang("ui.htgt6.mining_unit.player_inventory")).pos(84, 161).color(theme.getLabelColor()));
        panel.child(SlotGroupWidget.playerInventory().pos(84, 177));
        return panel;
    }

    private String getOreScanLine() {
        if (!structureFormed) return EnumChatFormatting.DARK_GRAY
                + StatCollector.translateToLocal("ui.htgt6.mining_unit.scan_unavailable");
        if (!scanComplete) return EnumChatFormatting.AQUA + StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.scanning", scannedLayerCount, activeMiningY, getScanPercent(), bufferedOreCount);
        return EnumChatFormatting.AQUA + StatCollector.translateToLocalFormatted(
                "ui.htgt6.mining_unit.ores_buffered", bufferedOreCount,
                activeMiningY >= scanBottomY ? Integer.toString(activeMiningY) : "-");
    }

    private String getActivityLine() {
        if (!structureFormed) {
            if (diagnostic.equals("INVALID CONTROLLER FACING")) {
                return color(EnumChatFormatting.RED, "ui.htgt6.terminal.invalid_controller_facing");
            }
            if (diagnostic.startsWith("STRUCTURE ERROR @ ")) {
                return color(EnumChatFormatting.RED, "ui.htgt6.terminal.structure_error",
                        diagnostic.substring("STRUCTURE ERROR @ ".length()));
            }
            if (diagnostic.startsWith("MISSING GATEWAY: ")) {
                String id = diagnostic.substring("MISSING GATEWAY: ".length());
                return color(EnumChatFormatting.RED, "ui.htgt6.terminal.missing_gateway", getGatewayDisplayName(id));
            }
            return color(EnumChatFormatting.RED, "ui.htgt6.terminal.structure_not_checked");
        }
        if (!enabled) return color(EnumChatFormatting.GOLD, "ui.htgt6.terminal.disabled");
        if (!scanComplete) return color(EnumChatFormatting.AQUA, "ui.htgt6.mining_unit.scan_active");
        if (diagnostic.equals("WAITING: 1000 L LUBRICANT")) {
            return color(EnumChatFormatting.GOLD, "ui.htgt6.terminal.need_lubricant");
        }
        if (diagnostic.equals("WAITING: OUTPUT HATCH FULL")) {
            return color(EnumChatFormatting.GOLD, "ui.htgt6.terminal.output_full");
        }
        if (diagnostic.equals("WAITING: FLUID OUTPUT HATCH FULL")) {
            return color(EnumChatFormatting.GOLD, "ui.htgt6.terminal.fluid_output_full");
        }
        if (diagnostic.equals("WAITING: ENERGY")) {
            return color(EnumChatFormatting.GOLD, "ui.htgt6.terminal.need_energy");
        }
        if (diagnostic.startsWith("EXCAVATING SHAFT: ")) {
            return color(EnumChatFormatting.GREEN, "ui.htgt6.mining_unit.excavating_shaft",
                    activeMiningY, shaftExcavationIndex, 9);
        }
        if (diagnostic.startsWith("SHAFT BLOCKED")) {
            return color(EnumChatFormatting.RED, "ui.htgt6.mining_unit.shaft_blocked");
        }
        if (currentMiningName != null && !currentMiningName.isEmpty()) {
            return color(miningActive ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW,
                    "ui.htgt6.mining_unit.mining_progress", currentMiningName, currentMiningAmount, activeMiningY,
                    formatSeconds(progress), formatSeconds(Math.max(1, maxProgress)));
        }
        return color(EnumChatFormatting.GRAY, "ui.htgt6.mining_unit.idle");
    }

    private String getGatewayDisplayName(String serializedName) {
        for (GatewayType type : GatewayType.VALUES) {
            if (type.serializedName.equals(serializedName)) {
                return StatCollector.translateToLocal("tile.mining_unit_port." + type.serializedName + ".name");
            }
        }
        return serializedName;
    }

    private static String color(EnumChatFormatting color, String key, Object... arguments) {
        return color + StatCollector.translateToLocalFormatted(key, arguments);
    }

    private String getStructureCompletionLine() {
        String key = structureFormed ? "ui.htgt6.structure.complete" : "ui.htgt6.structure.incomplete";
        return (structureFormed ? EnumChatFormatting.GREEN : EnumChatFormatting.RED)
                + StatCollector.translateToLocal(key);
    }

    private static String formatSeconds(int ticks) {
        if (ticks % 20 == 0) return Integer.toString(ticks / 20);
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0F);
    }

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

    private ItemSlot createModifierSlot(int index, String tooltipKey) {
        ModularSlot slot = SyncHandlers.itemSlot(modifierInventory, index)
                .slotGroup("mining_modifiers").accessibility(true, true);
        ItemSlot widget = new ItemSlot().slot(slot).background(GuiTextures.SLOT_DARK);
        widget.tooltip(t -> t.addLine(StatCollector.translateToLocal(tooltipKey)));
        return widget;
    }

    private ButtonWidget<?> createPowerButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(22, 22).background(GuiTextures.BUTTON).overlay(IKey.dynamic(() -> enabled ? "■" : "▶"));
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(StatCollector.translateToLocal(enabled
                    ? "ui.htgt6.mining_unit.power_off" : "ui.htgt6.mining_unit.power_on"));
        });
        button.syncHandler("mining_power_action");
        return button;
    }

    private ButtonWidget<?> createEnergyPanelButton(final ParentWidget<?> energyPanel) {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(22, 22).background(GuiTextures.BUTTON).overlay(IKey.str("⚡"));
        button.tooltip(t -> t.addLine(StatCollector.translateToLocal("ui.htgt6.mining_unit.energy_button")));
        button.onMouseTapped(mouse -> {
            energyPanelOpen = !energyPanelOpen;
            energyPanel.setEnabled(energyPanelOpen);
            return true;
        });
        return button;
    }

    private ButtonWidget<?> createRescanButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(22, 22).background(GuiTextures.BUTTON).overlay(GuiTextures.REFRESH);
        button.tooltip(t -> t.addLine(StatCollector.translateToLocal("ui.htgt6.mining_unit.rescan")));
        button.syncHandler("mining_rescan_action");
        return button;
    }

    private ButtonWidget<?> createReplacementButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(22, 22).background(GuiTextures.BUTTON).overlay(GuiTextures.BLOCK);
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(StatCollector.translateToLocal(replaceWithStone
                    ? "ui.htgt6.mining_unit.replace_stone" : "ui.htgt6.mining_unit.replace_air"));
        });
        button.syncHandler("mining_replace_action");
        return button;
    }

    private ButtonWidget<?> createRadiusButton() {
        ButtonWidget<?> button = new ButtonWidget();
        button.size(30, 22).background(GuiTextures.BUTTON)
                .overlay(IKey.dynamic(() -> Integer.toString(getWorkAreaSize())));
        button.tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(StatCollector.translateToLocalFormatted(
                    "ui.htgt6.mining_unit.radius", getWorkAreaSize(), getWorkAreaSize()));
            t.addLine(StatCollector.translateToLocal("ui.htgt6.mining_unit.radius_hint"));
        });
        // Returning false deliberately lets ButtonWidget continue into its
        // InteractionSyncHandler after this immediate client-side preview.
        button.onMouseTapped(mouseButton -> {
            if (worldObj != null && worldObj.isRemote) previewNextWorkRadius();
            return false;
        });
        button.syncHandler("mining_radius_action");
        return button;
    }

    private void toggleMachinePower() {
        enabled = !enabled;
        if (!enabled) {
            setMiningActive(false);
            currentEnergyUsage = 0L;
        }
        markDirtyAndSync();
    }

    private void toggleReplacementMode() {
        replaceWithStone = !replaceWithStone;
        markDirtyAndSync();
    }

    private void cycleWorkRadius() {
        radiusTier = getNextRadiusTier();
        radiusPreviewUntil = worldObj == null ? 0L : worldObj.getTotalWorldTime() + 200L;
        resetScan();
    }

    private void previewNextWorkRadius() {
        radiusTier = getNextRadiusTier();
        radiusPreviewUntil = worldObj.getTotalWorldTime() + 200L;
    }

    private int getNextRadiusTier() {
        int maximum = Math.max(0, machineTier - 1);
        return radiusTier >= maximum ? 0 : radiusTier + 1;
    }
    private static void frame(ModularPanel panel, int color, int x, int y, int w, int h) {
        Widget edge = new Widget().background(new Rectangle().setColor(color));
        edge.pos(x, y).size(w, h);
        panel.child(edge);
    }
    private static int[] makeSlotRange(int start, int count) {
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) slots[i] = start + i;
        return slots;
    }
    private String getTierName() {
        String[] names = { "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "PUV1", "UX", "OLV", "OMV", "OHV", "OEV", "OIV", "MAX" };
        return names[Math.max(0, Math.min(machineTier - 1, names.length - 1))];
    }
    private void setSyncedLube(int amount) {
        FluidStack fluid = lubricant(Math.max(0, amount));
        lubricantTank.setFluid(fluid);
    }

    private void markDirtyAndSync() {
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    private void setMiningActive(boolean active) {
        if (miningActive == active) return;
        miningActive = active;
        markDirtyAndSync();
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        boolean loadingFromDisk = worldObj == null;
        super.readFromNBT(nbt);
        if (!nbt.hasKey("MiningInventoryLayout")) migrateLegacyOutputBuffer();
        lubricantTank.readFromNBT(nbt.getCompoundTag("LubricantTank"));
        recipeOutputTank.readFromNBT(nbt.getCompoundTag("RecipeOutputTank"));
        miningActive = nbt.getBoolean("MiningActive");
        scanCursor = nbt.getInteger("ScanCursor");
        scanTotal = nbt.getInteger("ScanTotal");
        scanBottomY = nbt.getInteger("ScanBottomY");
        scannedLayerCount = nbt.getInteger("ScannedLayerCount");
        activeMiningY = nbt.hasKey("ActiveMiningY") ? nbt.getInteger("ActiveMiningY") : -1;
        scannedAreaSize = nbt.getInteger("ScannedAreaSize");
        bufferedOreCount = nbt.getInteger("BufferedOreCount");
        scanComplete = nbt.getBoolean("ScanComplete");
        replaceWithStone = nbt.getBoolean("ReplaceWithStone");
        radiusTier = Math.max(0, nbt.getInteger("RadiusTier"));
        radiusPreviewUntil = nbt.getLong("RadiusPreviewUntil");
        currentMiningName = nbt.getString("CurrentMiningName");
        currentMiningAmount = nbt.getInteger("CurrentMiningAmount");
        minedOres = nbt.getLong("MinedOres");
        if (loadingFromDisk) {
            shaftExcavationIndex = Math.max(0, Math.min(9, nbt.getInteger("ShaftExcavationIndex")));
            shaftLayerPrepared = nbt.getBoolean("ShaftLayerPrepared");
            nextTargetIndex = Math.max(0, nbt.getInteger("NextTargetIndex"));
            miningActive = false;
            currentEnergyUsage = 0L;
            if (nbt.getInteger("MiningStateVersion") >= MINING_STATE_VERSION) {
                readPersistentWorkState(nbt);
            } else {
                migrateLegacyMiningState();
            }
        }
    }
    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagCompound tank = new NBTTagCompound();
        lubricantTank.writeToNBT(tank);
        nbt.setTag("LubricantTank", tank);
        NBTTagCompound outputTank = new NBTTagCompound();
        recipeOutputTank.writeToNBT(outputTank);
        nbt.setTag("RecipeOutputTank", outputTank);
        nbt.setBoolean("MiningActive", miningActive);
        nbt.setInteger("ScanCursor", scanCursor);
        nbt.setInteger("ScanTotal", scanTotal);
        nbt.setInteger("ScanBottomY", scanBottomY);
        nbt.setInteger("ScannedLayerCount", scannedLayerCount);
        nbt.setInteger("ActiveMiningY", activeMiningY);
        nbt.setInteger("ScannedAreaSize", scannedAreaSize);
        nbt.setInteger("BufferedOreCount", bufferedOreCount);
        nbt.setBoolean("ScanComplete", scanComplete);
        nbt.setBoolean("ReplaceWithStone", replaceWithStone);
        nbt.setInteger("RadiusTier", radiusTier);
        nbt.setLong("RadiusPreviewUntil", radiusPreviewUntil);
        nbt.setInteger("MiningInventoryLayout", 1);
        nbt.setString("CurrentMiningName", currentMiningName == null ? "" : currentMiningName);
        nbt.setInteger("CurrentMiningAmount", currentMiningAmount);
        nbt.setLong("MinedOres", minedOres);
        nbt.setInteger("MiningStateVersion", MINING_STATE_VERSION);
        nbt.setInteger("ShaftExcavationIndex", shaftExcavationIndex);
        nbt.setBoolean("ShaftLayerPrepared", shaftLayerPrepared);
        nbt.setInteger("NextTargetIndex", nextTargetIndex);
        if (!writingNetworkSnapshot) {
            nbt.setTag("OreBuffer", writeTargetList(oreBuffer));
            if (currentTarget != null) nbt.setTag("CurrentTarget", writeTarget(currentTarget));
            nbt.setTag("CurrentDrops", writeItemList(currentDrops));
            nbt.setTag("CurrentFluidDrops", writeFluidList(currentFluidDrops));
        }
    }

    private void readPersistentWorkState(NBTTagCompound nbt) {
        oreBuffer.clear();
        NBTTagList targets = nbt.getTagList("OreBuffer", 10);
        int targetCount = Math.min(targets.tagCount(), MAX_PERSISTED_TARGETS);
        for (int i = 0; i < targetCount; i++) {
            MiningTarget target = readTarget(targets.getCompoundTagAt(i));
            if (target != null) oreBuffer.add(target);
        }
        nextTargetIndex = Math.max(0, Math.min(nextTargetIndex, oreBuffer.size()));
        currentTarget = nbt.hasKey("CurrentTarget")
                ? readTarget(nbt.getCompoundTag("CurrentTarget")) : null;
        currentDrops = readItemList(nbt.getTagList("CurrentDrops", 10));
        currentFluidDrops = readFluidList(nbt.getTagList("CurrentFluidDrops", 10));
        preparedFluidDrops = new ArrayList<FluidStack>();
        bufferedOreCount = Math.max(0, oreBuffer.size() - nextTargetIndex + (currentTarget == null ? 0 : 1));

        // A partially written legacy/corrupt operation must never destroy an ore
        // without its already rolled outputs. Put it back into the selection path.
        if (currentTarget != null && currentDrops.isEmpty()) {
            currentTarget = null;
            currentFluidDrops.clear();
            nextTargetIndex = Math.max(0, nextTargetIndex - 1);
            progress = 0;
        }
        if (currentTarget == null) {
            currentMiningName = "";
            currentMiningAmount = 0;
            progress = 0;
        } else {
            maxProgress = Math.max(1, maxProgress);
            progress = Math.max(0, Math.min(progress, maxProgress - 1));
        }
    }

    /** Migration for saves created before mining work buffers were serialized. */
    private void migrateLegacyMiningState() {
        oreBuffer.clear();
        clearCurrentTarget();
        nextTargetIndex = 0;
        progress = 0;
        shaftExcavationIndex = 0;
        if (activeMiningY >= 0) {
            scanCursor = 0;
            scanTotal = Math.max(1, getWorkAreaSize() * getWorkAreaSize());
            scannedAreaSize = getWorkAreaSize();
            bufferedOreCount = 0;
            scanComplete = false;
            shaftLayerPrepared = activeMiningY >= yCoord;
            diagnostic = "RESTORING LAYER SCAN: Y=" + activeMiningY;
        } else if (!scanComplete) {
            scanCursor = 0;
            scanTotal = 0;
            scanBottomY = 0;
            scannedLayerCount = 0;
            bufferedOreCount = 0;
        }
    }

    private static NBTTagList writeTargetList(List<MiningTarget> targets) {
        NBTTagList list = new NBTTagList();
        if (targets == null) return list;
        for (int i = 0; i < targets.size() && i < MAX_PERSISTED_TARGETS; i++) {
            MiningTarget target = targets.get(i);
            if (target != null) list.appendTag(writeTarget(target));
        }
        return list;
    }

    private static NBTTagCompound writeTarget(MiningTarget target) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("X", target.x);
        tag.setInteger("Y", target.y);
        tag.setInteger("Z", target.z);
        tag.setInteger("Block", Block.getIdFromBlock(target.block));
        tag.setInteger("Meta", target.meta);
        if (target.oreStack != null) {
            NBTTagCompound ore = new NBTTagCompound();
            target.oreStack.writeToNBT(ore);
            tag.setTag("Ore", ore);
        }
        return tag;
    }

    private static MiningTarget readTarget(NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("Ore")) return null;
        Block block = Block.getBlockById(tag.getInteger("Block"));
        ItemStack ore = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Ore"));
        if (block == null || ore == null) return null;
        return new MiningTarget(tag.getInteger("X"), tag.getInteger("Y"), tag.getInteger("Z"),
                block, tag.getInteger("Meta"), ore);
    }

    private static NBTTagList writeItemList(List<ItemStack> stacks) {
        NBTTagList list = new NBTTagList();
        if (stacks == null) return list;
        for (ItemStack stack : stacks) {
            if (stack == null || stack.stackSize <= 0) continue;
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            list.appendTag(tag);
        }
        return list;
    }

    private static List<ItemStack> readItemList(NBTTagList tags) {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        for (int i = 0; i < tags.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(tags.getCompoundTagAt(i));
            if (stack != null && stack.stackSize > 0) result.add(stack);
        }
        return result;
    }

    private static NBTTagList writeFluidList(List<FluidStack> stacks) {
        NBTTagList list = new NBTTagList();
        if (stacks == null) return list;
        for (FluidStack stack : stacks) {
            if (stack == null || stack.amount <= 0) continue;
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            list.appendTag(tag);
        }
        return list;
    }

    private static List<FluidStack> readFluidList(NBTTagList tags) {
        ArrayList<FluidStack> result = new ArrayList<FluidStack>();
        for (int i = 0; i < tags.tagCount(); i++) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(tags.getCompoundTagAt(i));
            if (stack != null && stack.amount > 0) result.add(stack);
        }
        return result;
    }

    /** Moves the former 16-slot output range (10..25) into the new 32-slot output buffer. */
    private void migrateLegacyOutputBuffer() {
        for (int oldSlot = 25; oldSlot >= 10; oldSlot--) {
            ItemStack stack = inventory[oldSlot];
            if (stack == null) continue;
            inventory[oldSlot] = null;
            if (!insertInto(stack, inventory, OUTPUT_SLOT_START)) inventory[oldSlot] = stack;
        }
    }
    @Override public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        // Persistent mining buffers can contain thousands of entries and are
        // server-only. ModularUI has dedicated lightweight live sync values.
        writingNetworkSnapshot = true;
        try {
            writeToNBT(nbt);
        } finally {
            writingNetworkSnapshot = false;
        }
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }
    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }

    private static final class MiningTarget {
        final int x, y, z, meta;
        final Block block;
        final ItemStack oreStack;
        MiningTarget(int x, int y, int z, Block block, int meta, ItemStack oreStack) {
            this.x = x; this.y = y; this.z = z; this.block = block; this.meta = meta;
            this.oreStack = oreStack == null ? null : oreStack.copy();
        }
    }

    /** Fill-only view used by the lubricant input gateway. */
    private final class LubricantInputGateway implements IFluidHandler {
        @Override public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
            return MultiTEMiningUnit.this.fill(from, resource, doFill);
        }
        @Override public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) { return null; }
        @Override public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) { return null; }
        @Override public boolean canFill(ForgeDirection from, Fluid fluid) {
            return MultiTEMiningUnit.this.canFill(from, fluid);
        }
        @Override public boolean canDrain(ForgeDirection from, Fluid fluid) { return false; }
        @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) {
            return new FluidTankInfo[] { lubricantTank.getInfo() };
        }
    }

    /** Drain-only view of the independent Crusher recipe-product tank. */
    private final class RecipeFluidOutputGateway implements IFluidHandler {
        @Override public int fill(ForgeDirection from, FluidStack resource, boolean doFill) { return 0; }
        @Override public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
            FluidStack stored = recipeOutputTank.getFluid();
            if (resource == null || stored == null || !resource.isFluidEqual(stored)) return null;
            return drain(from, resource.amount, doDrain);
        }
        @Override public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
            FluidStack drained = recipeOutputTank.drain(maxDrain, doDrain);
            if (drained != null && doDrain) markDirtyAndSync();
            return drained;
        }
        @Override public boolean canFill(ForgeDirection from, Fluid fluid) { return false; }
        @Override public boolean canDrain(ForgeDirection from, Fluid fluid) {
            FluidStack stored = recipeOutputTank.getFluid();
            return stored != null && (fluid == null || stored.getFluid() == fluid);
        }
        @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) {
            return new FluidTankInfo[] { recipeOutputTank.getInfo() };
        }
    }

    private static final class ModifierInventory implements IItemHandlerModifiable {
        private final MultiTEMiningUnit tile;
        ModifierInventory(MultiTEMiningUnit tile) { this.tile = tile; }
        @Override public int getSlots() { return 2; }
        @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < 2 ? tile.inventory[slot] : null; }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public void setStackInSlot(int slot, ItemStack stack) { if (slot >= 0 && slot < 2) tile.setInventorySlotContents(slot, stack); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return tile.isItemValidForSlot(slot, stack); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack == null || !isItemValid(slot, stack) || getStackInSlot(slot) != null) return stack;
            ItemStack remainder = stack.copy();
            ItemStack one = stack.copy(); one.stackSize = 1;
            remainder.stackSize--;
            if (!simulate) setStackInSlot(slot, one);
            return remainder.stackSize <= 0 ? null : remainder;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack current = getStackInSlot(slot);
            if (current == null || amount <= 0) return null;
            ItemStack out = current.copy(); out.stackSize = Math.min(amount, current.stackSize);
            if (!simulate) tile.decrStackSize(slot, out.stackSize);
            return out;
        }
    }
}
