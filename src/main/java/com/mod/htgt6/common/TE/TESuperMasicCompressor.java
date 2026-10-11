package com.mod.htgt6.common.TE;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.handler.ModBlocks;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.CompressorRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Legacy SMCompressor TileEntity ID, now the controller of an exact 5x5x5 multiblock. */
public class TESuperMasicCompressor extends MultiTileChemicalPlant {
    public static final int WIDTH = 5, HEIGHT = 5, DEPTH = 5;
    public static final int ELEMENT_AIR = 0, ELEMENT_CONTROLLER = 1;
    public static final int ELEMENT_FLEXIBLE_BASE = 2, ELEMENT_CASING = 3;
    public static final int ELEMENT_GLASS = 4;
    private static final int SAVE_VERSION = 2;

    @Override public String getInventoryName() {
        return StatCollector.translateToLocal("tile.superMasicCompressor.name");
    }

    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<ChemicalPlantRecipe> getMachineRecipes() {
        // CompressionRecipe is a ChemicalPlantRecipe; keep the live map so addon recipes
        // become available immediately without allocating a new list every machine tick.
        return (List) CompressorRecipeHandler.INSTANCE.getRecipeList();
    }

    @Override protected String getPanelId() { return "super_massive_compressor"; }
    @Override protected String getTitleKey() { return "tile.superMasicCompressor.name"; }
    @Override protected ItemStack getControllerDisplayStack() {
        return new ItemStack(ModBlocks.getSuperMassiveCompressorController(), 1, 0);
    }
    @Override protected String getStructureTooltipKey() { return "ui.htgt6.smc.nei_structure"; }
    @Override protected String getRecipeOverlayId() { return "compressing_machine"; }
    @Override protected String getRecipeTooltipKey() { return "ui.htgt6.smc.nei_recipes"; }
    @Override protected String getUiKey(String suffix) { return "ui.htgt6.smc." + suffix; }
    @Override protected Block resolveGatewayCasingBlock() { return SuperMassiveCompressorContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return 0; }

    @Override protected MultiblockStructureDefinition createStructureDefinition() {
        return MultiblockStructureDefinition.builder(-2, 2, 0, 4, 0, 4,
                new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        int element = getRequiredElement(context.u, context.y, context.v);
                        if (element == ELEMENT_CASING && context.block == SuperMassiveCompressorContent.rib
                                && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.compressor(context.u,context.y,context.v))
                            return MultiblockStructureDefinition.Match.valid("reinforced_rib");
                        IMultiblockGateway gateway = context.getGateway();
                        if (element == ELEMENT_CONTROLLER) {
                            return result(context.block == ModBlocks.getSuperMassiveCompressorController(), "controller");
                        }
                        if (element == ELEMENT_FLEXIBLE_BASE) {
                            return result(context.block == SuperMassiveCompressorContent.casing || gateway != null,
                                    gateway == null ? "smc_casing" : "gateway." + gateway.getGatewayType().serializedName);
                        }
                        if (element == ELEMENT_CASING) {
                            return result(context.block == SuperMassiveCompressorContent.casing, "smc_casing");
                        }
                        if (element == ELEMENT_GLASS) {
                            return result(context.block == SuperMassiveCompressorContent.glass, "smc_glass");
                        }
                        return result(context.isAir(), "air");
                    }
                })
                .gateway(GatewayType.ITEM_INPUT, 1, 10)
                .gateway(GatewayType.ITEM_OUTPUT, 1, 10)
                .gateway(GatewayType.FLUID_INPUT, 0, 10)
                .gateway(GatewayType.FLUID_OUTPUT, 0, 10)
                .gateway(GatewayType.ENERGY_INPUT, 1, 1)
                .build();
    }

    public static int getRequiredElement(int u, int y, int v) {
        if (u == 0 && y == 0 && v == 0) return ELEMENT_CONTROLLER;
        if (y == 0) return ELEMENT_FLEXIBLE_BASE;
        if (y == HEIGHT - 1) return ELEMENT_CASING;
        if (v == 0 && Math.abs(u) <= 1) return ELEMENT_GLASS;
        return Math.abs(u) == 2 || v == 0 || v == DEPTH - 1 ? ELEMENT_CASING : ELEMENT_AIR;
    }

    private static MultiblockStructureDefinition.Match result(boolean valid, String key) {
        return valid ? MultiblockStructureDefinition.Match.valid(key)
                : MultiblockStructureDefinition.Match.invalid();
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        ItemStack[] legacy = nbt.hasKey("SMCMultiblockVersion") ? null : readLegacyInventory(nbt);
        ItemStack completedLegacyOutput = null;
        if (legacy != null && nbt.getInteger("Progress") > 0 && nbt.hasKey("ActiveRecipeOutput")) {
            completedLegacyOutput = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("ActiveRecipeOutput"));
        }
        super.readFromNBT(nbt);
        if (legacy != null) migrateLegacyInventory(legacy, completedLegacyOutput);
    }

    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("SMCMultiblockVersion", SAVE_VERSION);
    }

    private static ItemStack[] readLegacyInventory(NBTTagCompound nbt) {
        ItemStack[] values = new ItemStack[20];
        NBTTagList list = nbt.getTagList("Inventory", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < values.length) values[slot] = ItemStack.loadItemStackFromNBT(entry);
        }
        return values;
    }

    private void migrateLegacyInventory(ItemStack[] old, ItemStack completedOutput) {
        for (int i = 0; i < inventory.length; i++) inventory[i] = null;
        inventory[SLOT_MODULE] = old[5];
        inventory[SLOT_TRANSFORMER] = old[6];
        int input = INPUT_SLOT_START;
        for (int slot : new int[] { 0, 1, 2, 4, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 }) {
            if (old[slot] != null && input < OUTPUT_SLOT_START) inventory[input++] = old[slot];
        }
        int output = OUTPUT_SLOT_START;
        if (old[3] != null) inventory[output++] = old[3];
        if (completedOutput != null && output < inventory.length) inventory[output] = completedOutput;
        progress = 0;
        maxProgress = 200;
        markDirty();
    }
}
