package com.mod.htgt6.common.mixer;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.IndustrialMixerRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Vertical 5x10x5 component mixer built on the shared HTGT6 MultiTE machine core. */
public final class MultiTileIndustrialMixer extends MultiTileChemicalPlant {
    public static final int WIDTH = 5;
    public static final int HEIGHT = 10;
    public static final int DEPTH = 5;

    public static final int ELEMENT_AIR = 0;
    public static final int ELEMENT_CONTROLLER = 1;
    public static final int ELEMENT_FLEXIBLE_BASE = 2;
    public static final int ELEMENT_CASING = 3;
    public static final int ELEMENT_GLASS = 4;

    @Override public String getInventoryName() {
        return StatCollector.translateToLocal("tile.industrial_mixer.name");
    }

    @Override protected List<ChemicalPlantRecipe> getMachineRecipes() {
        return IndustrialMixerRecipeHandler.INSTANCE.getRecipeList();
    }

    @Override protected String getPanelId() { return "industrial_mixer"; }
    @Override protected String getTitleKey() { return "tile.industrial_mixer.name"; }
    @Override protected ItemStack getControllerDisplayStack() {
        return new ItemStack(IndustrialMixerContent.controller, 1, 0);
    }
    @Override protected String getStructureTooltipKey() {
        return "ui.htgt6.industrial_mixer.nei_structure";
    }
    @Override protected String getRecipeOverlayId() {
        return com.mod.htgt6.client.nei.IndustrialMixerNEIHandler.OVERLAY_ID;
    }
    @Override protected String getRecipeTooltipKey() {
        return "ui.htgt6.industrial_mixer.nei_recipes";
    }
    @Override protected String getUiKey(String suffix) {
        return "ui.htgt6.industrial_mixer." + suffix;
    }
    @Override protected Block resolveGatewayCasingBlock() { return IndustrialMixerContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return 0; }

    @Override protected MultiblockStructureDefinition createStructureDefinition() {
        return MultiblockStructureDefinition.builder(-2, 2, 0, 9, 0, 4,
                new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        int element = getRequiredElement(context.u, context.y, context.v);
                        if (element == ELEMENT_CASING && context.block == IndustrialMixerContent.rib
                                && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.mixer(context.u,context.y,context.v))
                            return MultiblockStructureDefinition.Match.valid("reinforced_rib");
                        IMultiblockGateway gateway = context.getGateway();
                        switch (element) {
                            case ELEMENT_CONTROLLER:
                                return result(context.block == IndustrialMixerContent.controller, "controller");
                            case ELEMENT_FLEXIBLE_BASE:
                                return result(context.block == IndustrialMixerContent.casing || gateway != null,
                                        gateway == null ? "mixer_casing"
                                                : "gateway." + gateway.getGatewayType().serializedName);
                            case ELEMENT_CASING:
                                return result(context.block == IndustrialMixerContent.casing, "mixer_casing");
                            case ELEMENT_GLASS:
                                return result(context.block == IndustrialMixerContent.glass, "mixer_glass");
                            default:
                                return result(context.isAir(), "air");
                        }
                    }
                })
                .gateway(GatewayType.FLUID_INPUT, 0, 10)
                .gateway(GatewayType.FLUID_OUTPUT, 0, 10)
                .gateway(GatewayType.ITEM_INPUT, 1, 10)
                .gateway(GatewayType.ITEM_OUTPUT, 1, 10)
                .gateway(GatewayType.ENERGY_INPUT, 1, 1)
                .build();
    }

    /** Shared by world validation, hologram construction and the NEI layer renderer. */
    public static int getRequiredElement(int u, int y, int v) {
        if (u == 0 && y == 0 && v == 0) return ELEMENT_CONTROLLER;
        if (y == 0) return ELEMENT_FLEXIBLE_BASE;
        if (y == HEIGHT - 1) return ELEMENT_CASING;
        boolean boundary = Math.abs(u) == 2 || v == 0 || v == DEPTH - 1;
        if (!boundary) return ELEMENT_AIR;
        boolean corner = Math.abs(u) == 2 && (v == 0 || v == DEPTH - 1);
        if (corner || y == 5) return ELEMENT_CASING;
        return ELEMENT_GLASS;
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
        for (int y = 1; y < HEIGHT - 1; y++) {
            for (int u = -2; u <= 2; u++) for (int v = 0; v < DEPTH; v++) {
                if (getRequiredElement(u, y, v) != ELEMENT_GLASS) continue;
                int[] position = localToWorld(u, y, v);
                if (worldObj.getBlock(position[0], position[1], position[2])
                        == IndustrialMixerContent.glass
                        && worldObj.getBlockMetadata(position[0], position[1], position[2]) != target) {
                    worldObj.setBlockMetadataWithNotify(position[0], position[1], position[2], target, 2);
                }
            }
        }
    }

    @Override public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord - 7, yCoord - 1, zCoord - 7,
                xCoord + 8, yCoord + 12, zCoord + 8);
    }

    @Override public double getMaxRenderDistanceSquared() { return 65536.0D; }
}
