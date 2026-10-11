package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.mining.MiningUnitContent;
import com.mod.htgt6.common.flotation.FlotationChamberContent;
import com.mod.htgt6.common.flotation.MultiTileFlotationChamber;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact reference layout shared with validation and the construction projector. */
public final class FlotationChamberNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks = new StructureBlock[MultiTileFlotationChamber.WIDTH]
            [MultiTileFlotationChamber.HEIGHT][MultiTileFlotationChamber.DEPTH];
    private final List<Component> components;

    public FlotationChamberNEIStructure() {
        build();
        List<Component> list = new ArrayList<Component>();
        list.add(component(FlotationChamberContent.rib, 0, "tile.flotation_chamber_rib.name"));
        list.add(component(FlotationChamberContent.casing, 0,
                "tile.flotation_chamber_casing.name"));
        list.add(component(FlotationChamberContent.glass, com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED,
                "tile.unbreakable_glass.name"));
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
                int element = MultiTileFlotationChamber.getElementAt(x, y, z);
                if (element == MultiTileFlotationChamber.ELEMENT_CONTROLLER) {
                    set(x, y, z, FlotationChamberContent.controller, 2);
                } else if (element == MultiTileFlotationChamber.ELEMENT_FLEXIBLE_BASE) {
                    set(x, y, z, MiningUnitContent.port, MultiTileFlotationChamber.getGatewayAt(x,y,z).metadata);
                } else if (element == MultiTileFlotationChamber.ELEMENT_RIB) {
                    set(x, y, z, FlotationChamberContent.rib, 0);
                } else if (element == MultiTileFlotationChamber.ELEMENT_CASING) {
                    set(x, y, z, FlotationChamberContent.casing, 0);
                } else if (element == MultiTileFlotationChamber.ELEMENT_GLASS) {
                    blocks[x][y][z] = new StructureBlock(FlotationChamberContent.glass,
                            com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED, true);
                }
            }
        }

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

    @Override public String getName() { return "MultiTileFlotationChamber"; }
    @Override public ItemStack getController() {
        return new ItemStack(FlotationChamberContent.controller, 1, 0);
    }
    @Override public int getWidth() { return MultiTileFlotationChamber.WIDTH; }
    @Override public int getHeight() { return MultiTileFlotationChamber.HEIGHT; }
    @Override public int getDepth() { return MultiTileFlotationChamber.DEPTH; }
    @Override public int getControllerX() { return MultiTileFlotationChamber.CONTROLLER_X; }
    @Override public int getControllerY() { return MultiTileFlotationChamber.CONTROLLER_Y; }
    @Override public int getControllerZ() { return MultiTileFlotationChamber.CONTROLLER_Z; }
    @Override public StructureBlock getBlock(int x, int y, int z) { return blocks[x][y][z]; }
    @Override public List<Component> getComponents() { return components; }
}
