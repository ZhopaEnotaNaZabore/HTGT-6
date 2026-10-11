package com.mod.htgt6.common.flotation;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.FlotationChamberRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Reference layout: three adjacent vats along a 13-block facade, 11 deep and 7 high. */
public final class MultiTileFlotationChamber extends MultiTileChemicalPlant {
    private boolean flotationRunning;
    private boolean flotationCharge;

    public boolean isFlotationRunning() { return structureFormed && enabled && flotationRunning; }
    public boolean hasFlotationCharge() { return flotationCharge; }

    @Override protected void processMachineLogic() {
        int before = progress;
        boolean previous = flotationRunning;
        boolean previousCharge = flotationCharge;
        super.processMachineLogic();
        flotationRunning = isMachineActive() && progress != before;
        flotationCharge = hasPreparedRecipe();
        if (previous != flotationRunning || previousCharge != flotationCharge) markControllerDirtyAndSync();
    }

    @Override public void writeToNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("FlotationRunning", isFlotationRunning());
        nbt.setBoolean("FlotationCharge", flotationCharge);
    }

    @Override public void readFromNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        // Render state does not depend on client recipe indices or diagnostic text.
        flotationRunning = worldObj != null && worldObj.isRemote && nbt.getBoolean("FlotationRunning");
        flotationCharge = nbt.getBoolean("FlotationCharge");
    }
    public static final int WIDTH = 13;
    public static final int HEIGHT = 7;
    public static final int DEPTH = 11;
    public static final int CONTROLLER_X = 10, CONTROLLER_Y = 1, CONTROLLER_Z = 0;

    public static final int ELEMENT_AIR = 0;
    public static final int ELEMENT_CONTROLLER = 1;
    public static final int ELEMENT_FLEXIBLE_BASE = 2;
    public static final int ELEMENT_CASING = 3;
    public static final int ELEMENT_GLASS = 4;
    public static final int ELEMENT_RIB = 5;

    @Override public String getInventoryName() {
        return StatCollector.translateToLocal("tile.flotation_chamber.name");
    }

    @Override protected List<ChemicalPlantRecipe> getMachineRecipes() {
        return FlotationChamberRecipeHandler.INSTANCE.getRecipeList();
    }

    @Override protected String getPanelId() { return "flotation_chamber"; }
    @Override protected String getTitleKey() { return "tile.flotation_chamber.name"; }
    @Override protected ItemStack getControllerDisplayStack() {
        return new ItemStack(FlotationChamberContent.controller, 1, 0);
    }
    @Override protected String getStructureTooltipKey() {
        return "ui.htgt6.flotation_chamber.nei_structure";
    }
    @Override protected String getRecipeOverlayId() {
        return com.mod.htgt6.client.nei.FlotationChamberNEIHandler.OVERLAY_ID;
    }
    @Override protected String getRecipeTooltipKey() {
        return "ui.htgt6.flotation_chamber.nei_recipes";
    }
    @Override protected String getUiKey(String suffix) {
        return "ui.htgt6.flotation_chamber." + suffix;
    }
    @Override protected Block resolveGatewayCasingBlock() { return FlotationChamberContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return 0; }

    @Override protected MultiblockStructureDefinition createStructureDefinition() {
        return MultiblockStructureDefinition.builder(-CONTROLLER_X, WIDTH - CONTROLLER_X - 1,
                -CONTROLLER_Y, HEIGHT - CONTROLLER_Y - 1, -CONTROLLER_Z, DEPTH - CONTROLLER_Z - 1,
                new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        int element = getRequiredElement(context.u, context.y, context.v);
                        IMultiblockGateway gateway = context.getGateway();
                        switch (element) {
                            case ELEMENT_CONTROLLER:
                                return result(context.block == FlotationChamberContent.controller, "controller");
                            case ELEMENT_FLEXIBLE_BASE:
                                GatewayType expected = getGatewayAt(context.u + CONTROLLER_X,
                                        context.y + CONTROLLER_Y, context.v + CONTROLLER_Z);
                                return result(gateway != null && gateway.getGatewayType() == expected
                                                || expected == GatewayType.ITEM_OUTPUT && context.block == FlotationChamberContent.casing,
                                        gateway == null ? "flotation_casing"
                                                : "gateway." + gateway.getGatewayType().serializedName);
                            case ELEMENT_CASING:
                                return result(context.block == FlotationChamberContent.casing, "flotation_casing");
                            case ELEMENT_RIB:
                                return result(context.block == FlotationChamberContent.rib, "reinforced_rib");
                            case ELEMENT_GLASS:
                                return result(context.block == FlotationChamberContent.glass, "flotation_glass");
                            default:
                                return result(context.isAir(), "air");
                        }
                    }
                })
                .gateway(GatewayType.FLUID_INPUT, 1, 1)
                .gateway(GatewayType.FLUID_OUTPUT, 1, 1)
                .gateway(GatewayType.ITEM_INPUT, 1, 1)
                .gateway(GatewayType.ITEM_OUTPUT, 0, 1)
                .gateway(GatewayType.ENERGY_INPUT, 1, 1)
                .build();
    }

    /** Shared by world validation, hologram construction and the NEI layer renderer. */
    public static int getRequiredElement(int u, int y, int v) {
        return getElementAt(u + CONTROLLER_X, y + CONTROLLER_Y, v + CONTROLLER_Z);
    }

    /** Zero-based construction coordinates, also used by NEI and the model. */
    public static int getElementAt(int x, int y, int z) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= DEPTH) return ELEMENT_AIR;
        if (x == CONTROLLER_X && y == CONTROLLER_Y && z == CONTROLLER_Z) return ELEMENT_CONTROLLER;
        if (getGatewayAt(x,y,z) != null) return ELEMENT_FLEXIBLE_BASE;
        if (y == 0) return ELEMENT_CASING;
        boolean vat = x >= 2 && x <= 11 && z >= 2 && z <= 8;
        boolean partition = x == 2 || x == 5 || x == 8 || x == 11;
        boolean end = z == 2 || z == 8;
        boolean drive = x == 3 || x == 6 || x == 9;
        if (vat) {
            if (y == 1) return ELEMENT_CASING;
            if (y <= 4 && partition && end) return ELEMENT_RIB;
            if (y <= 4 && partition) return ELEMENT_CASING;
            if (y <= 3 && end) return ELEMENT_GLASS;
            if (y == 4 && end) return ELEMENT_RIB;
            if (y == 5 && drive) return ELEMENT_RIB;
            if (y == 6 && drive && z == 5) return ELEMENT_CASING;
        }
        // Feed stand at the left; shallow product receiver on the right.
        if (x <= 1 && z >= 5 && z <= 7 && y <= 2) return ELEMENT_CASING;
        if (x == 12 && z >= 4 && z <= 7 && y == 1) return ELEMENT_CASING;
        return ELEMENT_AIR;
    }

    public static GatewayType getGatewayAt(int x, int y, int z) {
        if (y != 1) return null;
        if (x == 0 && z == 5) return GatewayType.ITEM_INPUT;
        if (x == 1 && z == 0) return GatewayType.FLUID_INPUT;
        if (x == 0 && z == 1) return GatewayType.ENERGY_INPUT;
        if (x == 12 && z == 6) return GatewayType.FLUID_OUTPUT;
        if (x == 12 && z == 8) return GatewayType.ITEM_OUTPUT;
        return null;
    }

    public static boolean isRib(int u, int y, int v) {
        return getRequiredElement(u,y,v) == ELEMENT_RIB;
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
        for (int y = -CONTROLLER_Y; y < HEIGHT - CONTROLLER_Y; y++) {
            for (int u = -CONTROLLER_X; u < WIDTH - CONTROLLER_X; u++) for (int v = 0; v < DEPTH; v++) {
                if (getRequiredElement(u, y, v) != ELEMENT_GLASS) continue;
                int[] position = localToWorld(u, y, v);
                if (worldObj.getBlock(position[0], position[1], position[2])
                        == FlotationChamberContent.glass
                        && worldObj.getBlockMetadata(position[0], position[1], position[2]) != target) {
                    worldObj.setBlockMetadataWithNotify(position[0], position[1], position[2], target, 2);
                }
            }
        }
    }

    @Override public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord - 14, yCoord - 1, zCoord - 14,
                xCoord + 15, yCoord + 8, zCoord + 15);
    }

    @Override public double getMaxRenderDistanceSquared() { return 65536.0D; }
}
