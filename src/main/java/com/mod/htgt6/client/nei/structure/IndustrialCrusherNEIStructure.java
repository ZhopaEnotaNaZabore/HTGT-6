package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.mining.MiningUnitContent;
import com.mod.htgt6.common.crusher.IndustrialCrusherContent;
import com.mod.htgt6.common.crusher.MultiTileIndustrialCrusher;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact layer-by-layer 7x6x16 preview generated from the validation geometry. */
public final class IndustrialCrusherNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks = new StructureBlock[MultiTileIndustrialCrusher.WIDTH]
            [MultiTileIndustrialCrusher.HEIGHT][MultiTileIndustrialCrusher.DEPTH];
    private final List<Component> components;

    public IndustrialCrusherNEIStructure() {
        build();
        List<Component> list = new ArrayList<Component>();
        list.add(component(IndustrialCrusherContent.casing, 0,
                "tile.industrial_crusher_casing.name"));
        list.add(component(IndustrialCrusherContent.rib, 0,
                "tile.industrial_crusher_rib.name"));
        for (GatewayType type : GatewayType.VALUES) {
            int count = count(MiningUnitContent.port, type.metadata);
            if (count > 0) list.add(new Component(new ItemStack(MiningUnitContent.port, 1, type.metadata),
                    count, "tile.mining_unit_port." + type.serializedName + ".name"));
        }
        components = Collections.unmodifiableList(list);
    }

    private void build() {
        for (int x = 0; x < getWidth(); x++) for (int y = 0; y < getHeight(); y++) {
            for (int z = 0; z < getDepth(); z++) {
                int element = MultiTileIndustrialCrusher.getRequiredElement(x - 3, y, z);
                if (element == MultiTileIndustrialCrusher.ELEMENT_CONTROLLER) {
                    set(x, y, z, IndustrialCrusherContent.controller, 2);
                } else if (element == MultiTileIndustrialCrusher.ELEMENT_CASING
                        || element == MultiTileIndustrialCrusher.ELEMENT_FLEXIBLE_BASE) {
                    set(x, y, z, IndustrialCrusherContent.casing, 0);
                } else if (element == MultiTileIndustrialCrusher.ELEMENT_RIB) {
                    set(x, y, z, IndustrialCrusherContent.rib, 0);
                } else if (element == MultiTileIndustrialCrusher.ELEMENT_OUTPUT) {
                    set(x, y, z, MiningUnitContent.port, GatewayType.ITEM_OUTPUT.metadata);
                }
            }
        }

        // Representative valid service deck: fluid ports are optional for item-only recipes.
        set(1, 0, 1, MiningUnitContent.port, GatewayType.ITEM_INPUT.metadata);
        set(3, 0, 1, MiningUnitContent.port, GatewayType.FLUID_INPUT.metadata);
        set(4, 0, 1, MiningUnitContent.port, GatewayType.FLUID_OUTPUT.metadata);
        set(5, 0, 1, MiningUnitContent.port, GatewayType.ENERGY_INPUT.metadata);
    }

    private Component component(Block block, int metadata, String role) {
        return new Component(new ItemStack(block, 1, metadata), count(block, metadata), role);
    }

    private int count(Block block, int metadata) {
        int count = 0;
        for (int x = 0; x < getWidth(); x++) for (int y = 0; y < getHeight(); y++) {
            for (int z = 0; z < getDepth(); z++) {
                StructureBlock entry = blocks[x][y][z];
                if (entry != null && entry.block == block && entry.metadata == metadata) count++;
            }
        }
        return count;
    }

    private void set(int x, int y, int z, Block block, int metadata) {
        blocks[x][y][z] = new StructureBlock(block, metadata);
    }

    @Override public String getName() { return "MultiTileIndustrialCrusher"; }
    @Override public ItemStack getController() {
        return new ItemStack(IndustrialCrusherContent.controller, 1, 0);
    }
    @Override public int getWidth() { return MultiTileIndustrialCrusher.WIDTH; }
    @Override public int getHeight() { return MultiTileIndustrialCrusher.HEIGHT; }
    @Override public int getDepth() { return MultiTileIndustrialCrusher.DEPTH; }
    @Override public int getControllerX() { return 3; }
    @Override public int getControllerY() { return 0; }
    @Override public int getControllerZ() { return 0; }
    @Override public StructureBlock getBlock(int x, int y, int z) { return blocks[x][y][z]; }
    @Override public List<Component> getComponents() { return components; }
}
