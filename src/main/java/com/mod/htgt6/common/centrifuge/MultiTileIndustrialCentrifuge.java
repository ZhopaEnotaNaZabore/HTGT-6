package com.mod.htgt6.common.centrifuge;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.IndustrialCentrifugeRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;

import java.util.List;

/** 7x5x7 centrifuge built on the shared HTGT6 processing/UI and gateway core. */
public final class MultiTileIndustrialCentrifuge extends MultiTileChemicalPlant {
    public static final int WIDTH = IndustrialCentrifugeGeometry.WIDTH;
    public static final int HEIGHT = IndustrialCentrifugeGeometry.HEIGHT;
    public static final int DEPTH = IndustrialCentrifugeGeometry.DEPTH;

    public static final int ELEMENT_AIR = 0;
    public static final int ELEMENT_CONTROLLER = 1;
    public static final int ELEMENT_FLEXIBLE_BASE = 2;
    public static final int ELEMENT_CASING = 3;
    public static final int ELEMENT_GLASS = 4;
    public static final int ELEMENT_RIB = 5;

    @Override public String getInventoryName() {
        return StatCollector.translateToLocal("tile.industrial_centrifuge.name");
    }

    @Override protected List<ChemicalPlantRecipe> getMachineRecipes() {
        return IndustrialCentrifugeRecipeHandler.INSTANCE.getRecipeList();
    }

    @Override protected String getPanelId() { return "industrial_centrifuge"; }
    @Override protected String getTitleKey() { return "tile.industrial_centrifuge.name"; }
    @Override protected ItemStack getControllerDisplayStack() {
        return new ItemStack(IndustrialCentrifugeContent.controller, 1, 0);
    }
    @Override protected String getStructureTooltipKey() {
        return "ui.htgt6.industrial_centrifuge.nei_structure";
    }
    @Override protected String getRecipeOverlayId() {
        return com.mod.htgt6.client.nei.IndustrialCentrifugeNEIHandler.OVERLAY_ID;
    }
    @Override protected String getRecipeTooltipKey() {
        return "ui.htgt6.industrial_centrifuge.nei_recipes";
    }
    @Override protected String getUiKey(String suffix) {
        return "ui.htgt6.industrial_centrifuge." + suffix;
    }
    @Override protected Block resolveGatewayCasingBlock() { return IndustrialCentrifugeContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return 0; }

    @Override protected MultiblockStructureDefinition createStructureDefinition() {
        return MultiblockStructureDefinition.builder(-3, 3, 0, 4, 0, 6,
                new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        int element = getRequiredElement(context.u, context.y, context.v);
                        IMultiblockGateway gateway = context.getGateway();
                        switch (element) {
                            case ELEMENT_CONTROLLER:
                                return result(context.block == IndustrialCentrifugeContent.controller, "controller");
                            case ELEMENT_FLEXIBLE_BASE:
                                return result(context.block == IndustrialCentrifugeContent.casing || gateway != null,
                                        gateway == null ? "centrifuge_casing"
                                                : "gateway." + gateway.getGatewayType().serializedName);
                            case ELEMENT_CASING:
                                return result(context.block == IndustrialCentrifugeContent.casing, "centrifuge_casing");
                            case ELEMENT_RIB:
                                return result(context.block == IndustrialCentrifugeContent.rib, "centrifuge_rib");
                            case ELEMENT_GLASS:
                                return result(context.block == IndustrialCentrifugeContent.glass, "centrifuge_glass");
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
                .optionalGateway(GatewayType.ENERGY_OUTPUT)
                .build();
    }

    /** Shared by world validation, hologram construction and the NEI layer renderer. */
    public static int getRequiredElement(int u, int y, int v) {
        return IndustrialCentrifugeGeometry.element(u, y, v);
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
            for (int u = -3; u <= 3; u++) for (int v = 0; v < DEPTH; v++) {
                if (getRequiredElement(u, y, v) != ELEMENT_GLASS) continue;
                int[] position = localToWorld(u, y, v);
                if (worldObj.getBlock(position[0], position[1], position[2])
                        == IndustrialCentrifugeContent.glass
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
