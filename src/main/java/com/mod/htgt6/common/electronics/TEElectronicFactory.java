package com.mod.htgt6.common.electronics;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.IMultiblockGateway;
import com.mod.htgt6.api.hitech6.multiblock.MultiblockStructureDefinition;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.ElectronicFactoryRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;

import java.util.List;

/**
 * 5x7x10 Electronics Factory using the common HTGT6 tier, gateway and recipe-machine implementation.
 * The controller and mandatory output hatch define the front/rear centres of the production line.
 */
public final class TEElectronicFactory extends MultiTileChemicalPlant {
    public static final int WIDTH = 5;
    public static final int HEIGHT = 7;
    public static final int DEPTH = 10;

    public static final int ELEMENT_AIR = 0;
    public static final int ELEMENT_CONTROLLER = 1;
    public static final int ELEMENT_CASING = 2;
    public static final int ELEMENT_FLEXIBLE_FLOOR = 3;
    public static final int ELEMENT_GLASS = 4;
    public static final int ELEMENT_OUTPUT_HATCH = 5;
    public static final int ELEMENT_RIB = 6;
    private int factoryStructureVersion = 2;

    @Override public String getInventoryName() {
        return StatCollector.translateToLocal("tile.te_electronic_factory.name");
    }

    @Override protected List<ChemicalPlantRecipe> getMachineRecipes() {
        return ElectronicFactoryRecipeHandler.INSTANCE.getRecipeList();
    }

    @Override protected String getPanelId() { return "te_electronic_factory"; }
    @Override protected String getTitleKey() { return "tile.te_electronic_factory.name"; }
    @Override protected ItemStack getControllerDisplayStack() {
        return new ItemStack(ElectronicFactoryContent.controller, 1, 0);
    }
    @Override protected String getStructureTooltipKey() {
        return "ui.htgt6.electronic_factory.nei_structure";
    }
    @Override protected String getRecipeOverlayId() {
        return com.mod.htgt6.client.nei.ElectronicFactoryNEIHandler.OVERLAY_ID;
    }
    @Override protected String getRecipeTooltipKey() {
        return "ui.htgt6.electronic_factory.nei_recipes";
    }
    @Override protected String getUiKey(String suffix) {
        return "ui.htgt6.electronic_factory." + suffix;
    }
    @Override protected Block resolveGatewayCasingBlock() { return ElectronicFactoryContent.casing; }
    @Override protected int resolveGatewayCasingMetadata() { return 0; }

    @Override protected MultiblockStructureDefinition createStructureDefinition() {
        return MultiblockStructureDefinition.builder(-2, 2, -3, 3, 0, 9,
                new MultiblockStructureDefinition.PositionMatcher() {
                    @Override public MultiblockStructureDefinition.Match match(
                            MultiblockStructureDefinition.Context context) {
                        int element = getRequiredElement(context.u, context.y, context.v);
                        IMultiblockGateway gateway = context.getGateway();
                        // Old saved factories remain valid; no blocks or inventories are replaced.
                        if (element == ELEMENT_RIB && factoryStructureVersion < 2
                                && context.block != ElectronicFactoryContent.rib) {
                            element = ElectronicFactoryGeometry.legacyElement(context.u, context.y, context.v);
                        }
                        switch (element) {
                            case ELEMENT_RIB:
                                return result(context.block == ElectronicFactoryContent.rib, "factory_rib");
                            case ELEMENT_CONTROLLER:
                                return result(context.block == ElectronicFactoryContent.controller, "controller");
                            case ELEMENT_OUTPUT_HATCH:
                                return result(gateway != null
                                        && gateway.getGatewayType() == GatewayType.ITEM_OUTPUT,
                                        "rear_output_hatch");
                            case ELEMENT_FLEXIBLE_FLOOR:
                                return result(context.block == ElectronicFactoryContent.casing || gateway != null,
                                        gateway == null ? "casing" : "gateway." + gateway.getGatewayType().serializedName);
                            case ELEMENT_CASING:
                                return result(context.block == ElectronicFactoryContent.casing, "casing");
                            case ELEMENT_GLASS:
                                return result(context.block == ElectronicFactoryContent.glass, "reinforced_glass");
                            default:
                                return result(context.isAir(), "air");
                        }
                    }
                })
                .gateway(GatewayType.FLUID_INPUT, 1, 10)
                .gateway(GatewayType.FLUID_OUTPUT, 1, 10)
                .gateway(GatewayType.ITEM_INPUT, 1, 10)
                .gateway(GatewayType.ITEM_OUTPUT, 1, 1)
                .gateway(GatewayType.ENERGY_INPUT, 1, 1)
                .optionalGateway(GatewayType.ENERGY_OUTPUT)
                .build();
    }

    /** Shared by validation and the NEI renderer. Local Y is centred on the controller. */
    public static int getRequiredElement(int u, int y, int v) {
        return ElectronicFactoryGeometry.element(u,y,v);
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        factoryStructureVersion = nbt.hasKey("FactoryStructureVersion") ? nbt.getInteger("FactoryStructureVersion") : 1;
    }
    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("FactoryStructureVersion", factoryStructureVersion);
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
        for (int y = -2; y <= 2; y++) {
            for (int u : new int[] { -2, 2 }) {
                for (int v = 1; v < DEPTH - 1; v++) {
                    int[] position = localToWorld(u, y, v);
                    if (worldObj.getBlock(position[0], position[1], position[2])
                            != ElectronicFactoryContent.glass) continue;
                    if (worldObj.getBlockMetadata(position[0], position[1], position[2]) != target) {
                        worldObj.setBlockMetadataWithNotify(position[0], position[1], position[2], target, 2);
                    }
                }
            }
        }
    }

    @Override public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord - 12, yCoord - 5, zCoord - 12,
                xCoord + 13, yCoord + 6, zCoord + 13);
    }

    @Override public double getMaxRenderDistanceSquared() { return 65536.0D; }
}
