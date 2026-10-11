package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.common.mining.BlockMiningUnitPort;
import com.mod.htgt6.common.mining.MiningUnitContent;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Minimal valid 5x12x5 layout. Flexible base positions are resolved deterministically for preview. */
public final class MiningUnitNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks = new StructureBlock[5][12][5];
    private final List<Component> components;

    public MiningUnitNEIStructure() {
        build();
        List<Component> list = new ArrayList<Component>();
        list.add(new Component(new ItemStack(MiningUnitContent.rib,1,0),4,"tile.mining_unit_rib.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.casing, 1, 0), 31, "tile.mining_unit_casing.standard.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.frame, 1, 0), 62, "tile.mining_unit_frame.standard.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.frame, 1, 1), 1, "tile.mining_unit_frame.quarter.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.formedGlass), 36, "tile.unbreakable_glass.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.port, 1, BlockMiningUnitPort.FLUID_INPUT), 1, "tile.mining_unit_port.fluid_input.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.port, 1, BlockMiningUnitPort.FLUID_OUTPUT), 1, "tile.mining_unit_port.fluid_output.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.port, 1, BlockMiningUnitPort.ITEM_INPUT), 1, "tile.mining_unit_port.item_input.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.port, 1, BlockMiningUnitPort.ITEM_OUTPUT), 1, "tile.mining_unit_port.item_output.name"));
        list.add(new Component(new ItemStack(MiningUnitContent.port, 1, BlockMiningUnitPort.ENERGY_INPUT), 1, "tile.mining_unit_port.energy_input.name"));
        components = Collections.unmodifiableList(list);
    }

    private void build() {
        // Layer 1: controller plus the five mandatory delegated ports; all remaining cells are casing.
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) set(x, 0, z, MiningUnitContent.casing, 0);
        set(2, 0, 0, MiningUnitContent.controller, 2);
        set(0, 0, 0, MiningUnitContent.port, BlockMiningUnitPort.FLUID_INPUT);
        set(1, 0, 0, MiningUnitContent.port, BlockMiningUnitPort.FLUID_OUTPUT);
        set(3, 0, 0, MiningUnitContent.port, BlockMiningUnitPort.ITEM_INPUT);
        set(4, 0, 0, MiningUnitContent.port, BlockMiningUnitPort.ITEM_OUTPUT);
        set(0, 0, 1, MiningUnitContent.port, BlockMiningUnitPort.ENERGY_INPUT);

        for (int y = 1; y <= 3; y++) {
            for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) {
                boolean corner = (x == 0 || x == 4) && (z == 0 || z == 4);
                if (corner) set(x, y, z, MiningUnitContent.frame, 0);
                else if (x == 0 || x == 4 || z == 0 || z == 4) {
                    set(x, y, z, MiningUnitContent.formedGlass, 0);
                }
            }
        }
        addRing(4, 0, 4, 0, 4, MiningUnitContent.casing, 0);
        addRing(5, 0, 4, 0, 4, MiningUnitContent.frame, 0);
        addRing(6, 0, 4, 0, 4, MiningUnitContent.frame, 0);
        addRing(7, 1, 3, 1, 3, MiningUnitContent.frame, 0);
        addRing(8, 1, 3, 1, 3, MiningUnitContent.frame, 0);
        set(2, 9, 2, MiningUnitContent.frame, 0);
        set(2, 10, 2, MiningUnitContent.frame, 0);
        set(2, 11, 2, MiningUnitContent.frame, 1);
    }

    private void addRing(int y, int minX, int maxX, int minZ, int maxZ, net.minecraft.block.Block block, int meta) {
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (x == minX || x == maxX || z == minZ || z == maxZ) set(x, y, z, block, meta);
        }
    }

    private void set(int x, int y, int z, net.minecraft.block.Block block, int meta) {
        if (block == MiningUnitContent.casing && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.mining(x-2,y,z)) block = MiningUnitContent.rib;
        blocks[x][y][z] = new StructureBlock(block, meta);
    }

    @Override public String getName() { return "MultiTE Mining Unit"; }
    @Override public ItemStack getController() { return new ItemStack(MiningUnitContent.controller, 1, 0); }
    @Override public int getWidth() { return 5; }
    @Override public int getHeight() { return 12; }
    @Override public int getDepth() { return 5; }
    @Override public int getControllerX() { return 2; }
    @Override public int getControllerY() { return 0; }
    @Override public int getControllerZ() { return 0; }
    @Override public StructureBlock getBlock(int x, int y, int z) { return blocks[x][y][z]; }
    @Override public List<Component> getComponents() { return components; }
}
