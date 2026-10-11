package com.mod.htgt6.common.TE;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.handler.ModBlocks;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.SolidifierRecipeHandler;
import com.mod.htgt6.common.multiblock.gateway.TileEntityMultiblockGateway;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.List;

/** Legacy BlockSolidifier TileEntity ID, rebuilt as the 5x7x16 casting-line controller. */
public class FluidSolidifierTE extends MultiTileChemicalPlant {
    public static final int WIDTH = 5, HEIGHT = 7, DEPTH = 16;
    public static final int ELEMENT_AIR = 0, ELEMENT_CONTROLLER = 1;
    public static final int ELEMENT_FRONT_SERVICE = 2, ELEMENT_CASING = 3;
    public static final int ELEMENT_GLASS = 4, ELEMENT_FIXED_OUTPUT = 5;
    private static final int SAVE_VERSION = 2;
    private int selectedFluidStart;
    private int selectedFluidLength;
    private int activeSelector = 1;

    @Override public String getInventoryName() {
        return StatCollector.translateToLocal("tile.fluidSolidifier.name");
    }

    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<ChemicalPlantRecipe> getMachineRecipes() {
        return (List) SolidifierRecipeHandler.recipes;
    }

    @Override protected String getPanelId() { return "multi_tile_fluid_solidifier"; }
    @Override protected String getTitleKey() { return "tile.fluidSolidifier.name"; }
    @Override protected ItemStack getControllerDisplayStack() {
        return new ItemStack(ModBlocks.getFluidSolidifierController(), 1, 0);
    }
    @Override protected String getStructureTooltipKey() { return "ui.htgt6.solidifier.nei_structure"; }
    @Override protected String getRecipeOverlayId() { return "fluid_solidifier"; }
    @Override protected String getRecipeTooltipKey() { return "ui.htgt6.solidifier.nei_recipes"; }
    @Override protected String getUiKey(String suffix) { return "ui.htgt6.solidifier." + suffix; }
    @Override protected Block resolveGatewayCasingBlock() { return FluidSolidifierContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return 0; }

    @Override protected ChemicalPlantRecipe findRecipe() {
        int controllerSelector = getConfiguredSelectorSetting();
        if (controllerSelector > 0) return findRecipeForSelector(controllerSelector);
        for (IMultiblockGateway gateway : getStructureGateways()) {
            if (gateway.getGatewayType() != GatewayType.FLUID_INPUT
                    || !(gateway instanceof TileEntityMultiblockGateway)) continue;
            ChemicalPlantRecipe recipe = findRecipeForSelector(
                    ((TileEntityMultiblockGateway) gateway).getSelectorTag());
            if (recipe != null) return recipe;
        }
        return null;
    }

    @Override protected int findBlockedRecipeTier() {
        int controllerSelector = getConfiguredSelectorSetting();
        if (controllerSelector > 0) return findBlockedRecipeTierForSelector(controllerSelector);
        int required = Integer.MAX_VALUE;
        for (IMultiblockGateway gateway : getStructureGateways()) {
            if (gateway.getGatewayType() != GatewayType.FLUID_INPUT
                    || !(gateway instanceof TileEntityMultiblockGateway)) continue;
            int tier = findBlockedRecipeTierForSelector(
                    ((TileEntityMultiblockGateway) gateway).getSelectorTag());
            if (tier > 0) required = Math.min(required, tier);
        }
        return required == Integer.MAX_VALUE ? 0 : required;
    }

    @Override protected boolean hasRecipeFluidInputs(ChemicalPlantRecipe recipe, int multiplier) {
        if (recipe == null) return false;
        int controllerSelector = getConfiguredSelectorSetting();
        if (controllerSelector > 0) {
            selectedFluidStart = 0;
            selectedFluidLength = getStructureFluidTankCount(GatewayType.FLUID_INPUT);
            activeSelector = controllerSelector;
            return hasInputFluidsInRange(recipe.fluidInputs, multiplier,
                    selectedFluidStart, selectedFluidLength);
        }
        for (IMultiblockGateway gateway : getStructureGateways()) {
            if (gateway.getGatewayType() != GatewayType.FLUID_INPUT
                    || !(gateway instanceof TileEntityMultiblockGateway)
                    || ((TileEntityMultiblockGateway) gateway).getSelectorTag() != recipe.selectorTag
                    || !(gateway instanceof TileEntity)) continue;
            TileEntity tile = (TileEntity) gateway;
            int[] range = getStructureFluidTankRange(GatewayType.FLUID_INPUT,
                    tile.xCoord, tile.yCoord, tile.zCoord);
            if (range[1] > 0 && hasInputFluidsInRange(recipe.fluidInputs, multiplier, range[0], range[1])) {
                selectedFluidStart = range[0];
                selectedFluidLength = range[1];
                activeSelector = recipe.selectorTag;
                return true;
            }
        }
        return false;
    }

    @Override protected void consumeRecipeFluidInputs(ChemicalPlantRecipe recipe, int multiplier) {
        consumeInputFluidsInRange(recipe.fluidInputs, multiplier,
                selectedFluidStart, selectedFluidLength);
    }

    public int getActiveSelectorForRender() { return activeSelector; }
    public String getTierName() {
        String[] names = { "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV",
                "PUV1", "UX", "OLV", "OMV", "OHV", "OEV", "OIV", "MAX" };
        return names[Math.max(0, Math.min(machineTier - 1, names.length - 1))];
    }

    @Override protected MultiblockStructureDefinition createStructureDefinition() {
        return MultiblockStructureDefinition.builder(-2, 2, 0, HEIGHT - 1, 0, DEPTH - 1,
                new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        int element = getRequiredElement(context.u, context.y, context.v);
                        if (element == ELEMENT_CASING && context.block == FluidSolidifierContent.rib
                                && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.foundry(context.u,context.y,context.v))
                            return MultiblockStructureDefinition.Match.valid("reinforced_rib");
                        IMultiblockGateway gateway = context.getGateway();
                        if (element == ELEMENT_CONTROLLER) {
                            return result(context.block == ModBlocks.getFluidSolidifierController(), "controller");
                        }
                        if (element == ELEMENT_FRONT_SERVICE) {
                            boolean permitted = gateway == null
                                    || gateway.getGatewayType() == GatewayType.FLUID_INPUT
                                    || gateway.getGatewayType() == GatewayType.ITEM_INPUT
                                    || gateway.getGatewayType() == GatewayType.ENERGY_INPUT
                                    || gateway.getGatewayType() == GatewayType.ENERGY_OUTPUT;
                            return result(permitted && (context.block == FluidSolidifierContent.casing || gateway != null),
                                    gateway == null ? "solidifier_casing"
                                            : "gateway." + gateway.getGatewayType().serializedName);
                        }
                        if (element == ELEMENT_FIXED_OUTPUT) {
                            return result(gateway != null && gateway.getGatewayType() == GatewayType.ITEM_OUTPUT,
                                    "gateway.item_output");
                        }
                        if (element == ELEMENT_CASING) {
                            return result(context.block == FluidSolidifierContent.casing, "solidifier_casing");
                        }
                        if (element == ELEMENT_GLASS) {
                            return result(context.block == FluidSolidifierContent.glass, "solidifier_glass");
                        }
                        return result(context.isAir(), "air");
                    }
                })
                .gateway(GatewayType.FLUID_INPUT, 1, 10)
                .gateway(GatewayType.FLUID_OUTPUT, 0, 0)
                .gateway(GatewayType.ITEM_INPUT, 0, 10)
                .gateway(GatewayType.ITEM_OUTPUT, 1, 1)
                .gateway(GatewayType.ENERGY_INPUT, 1, 1)
                .build();
    }

    /** Shared by validation, the hologram and the NEI layer model. */
    public static int getRequiredElement(int u, int y, int v) {
        if (u == 0 && y == 0 && v == 0) return ELEMENT_CONTROLLER;
        if (u == 0 && y == 1 && v == DEPTH - 1) return ELEMENT_FIXED_OUTPUT;
        if (v == 0) return ELEMENT_FRONT_SERVICE;
        if (y == 0 || y == HEIGHT - 1 || v == DEPTH - 1 || u == 2) return ELEMENT_CASING;
        if (u == -2) return v == 8 ? ELEMENT_CASING : ELEMENT_GLASS;
        return ELEMENT_AIR;
    }

    private static MultiblockStructureDefinition.Match result(boolean valid, String key) {
        return valid ? MultiblockStructureDefinition.Match.valid(key)
                : MultiblockStructureDefinition.Match.invalid();
    }

    @Override protected void onStructureFormed(boolean newlyFormed,
                                                MultiblockStructureDefinition.Result result) {
        setGlassFormed(true);
    }

    @Override protected void onStructureInvalidated(boolean wasFormed,
                                                     MultiblockStructureDefinition.Result result) {
        setGlassFormed(false);
    }

    public void restoreReinforcedGlass() { setGlassFormed(false); }

    private void setGlassFormed(boolean formed) {
        if (worldObj == null || worldObj.isRemote) return;
        int target = formed ? com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.FORMED_CLEAR
                : com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED;
        for (int y = 1; y < HEIGHT - 1; y++) for (int v = 1; v < DEPTH - 1; v++) {
            if (getRequiredElement(-2, y, v) != ELEMENT_GLASS) continue;
            int[] position = localToWorld(-2, y, v);
            if (worldObj.getBlock(position[0], position[1], position[2]) == FluidSolidifierContent.glass
                    && worldObj.getBlockMetadata(position[0], position[1], position[2]) != target) {
                worldObj.setBlockMetadataWithNotify(position[0], position[1], position[2], target, 2);
            }
        }
    }

    @Override public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord - 20, yCoord - 2, zCoord - 20,
                xCoord + 21, yCoord + 10, zCoord + 21);
    }

    @Override public double getMaxRenderDistanceSquared() { return 65536.0D; }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        ItemStack[] legacy = nbt.hasKey("SolidifierMultiblockVersion") ? null : readLegacyInventory(nbt);
        FluidStack legacyFluid = null;
        if (legacy != null && nbt.hasKey("FluidTank")) {
            FluidTank tank = new FluidTank(16000);
            tank.readFromNBT(nbt.getCompoundTag("FluidTank"));
            legacyFluid = tank.getFluid() == null ? null : tank.getFluid().copy();
        }
        super.readFromNBT(nbt);
        activeSelector = nbt.hasKey("SolidifierActiveSelector")
                ? Math.max(1, Math.min(24, nbt.getInteger("SolidifierActiveSelector"))) : 1;
        if (legacy != null) migrateLegacyState(legacy, legacyFluid);
    }

    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("SolidifierMultiblockVersion", SAVE_VERSION);
        nbt.setInteger("SolidifierActiveSelector", activeSelector);
    }

    private static ItemStack[] readLegacyInventory(NBTTagCompound nbt) {
        ItemStack[] values = new ItemStack[6];
        NBTTagList list = nbt.getTagList("Inventory", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < values.length) values[slot] = ItemStack.loadItemStackFromNBT(entry);
        }
        return values;
    }

    private void migrateLegacyState(ItemStack[] old, FluidStack fluid) {
        for (int i = 0; i < inventory.length; i++) inventory[i] = null;
        inventory[SLOT_MODULE] = old[4];
        inventory[SLOT_TRANSFORMER] = old[5];
        if (old[3] != null) inventory[OUTPUT_SLOT_START] = old[3];
        if (fluid != null) {
            IFluidHandler handler = resolveFluidGateway(GatewayType.FLUID_INPUT);
            if (handler != null) handler.fill(ForgeDirection.UNKNOWN, fluid, true);
        }
        progress = 0;
        maxProgress = 200;
        markDirty();
    }
}
