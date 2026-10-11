package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.TE.FluidSolidifierContent;
import com.mod.htgt6.common.TE.FluidSolidifierTE;
import com.mod.htgt6.common.handler.ModBlocks;
import com.mod.htgt6.common.mining.MiningUnitContent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact 5x7x16 two-section casting line, including its fixed rear output. */
public final class FluidSolidifierNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks = new StructureBlock[FluidSolidifierTE.WIDTH]
            [FluidSolidifierTE.HEIGHT][FluidSolidifierTE.DEPTH];
    private final List<Component> components;

    public FluidSolidifierNEIStructure() {
        build();
        List<Component> list = new ArrayList<Component>();
        list.add(component(FluidSolidifierContent.rib, 0, "tile.fluid_solidifier_rib.name"));
        list.add(component(FluidSolidifierContent.casing, 0, "tile.fluid_solidifier_casing.name"));
        list.add(component(FluidSolidifierContent.glass, com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED,
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
                int element = FluidSolidifierTE.getRequiredElement(x - 2, y, z);
                if (element == FluidSolidifierTE.ELEMENT_CONTROLLER) set(x, y, z,
                        ModBlocks.getFluidSolidifierController(), 2);
                else if (element == FluidSolidifierTE.ELEMENT_CASING
                        || element == FluidSolidifierTE.ELEMENT_FRONT_SERVICE) set(x, y, z,
                        FluidSolidifierContent.casing, 0);
                else if (element == FluidSolidifierTE.ELEMENT_GLASS) blocks[x][y][z] =
                        new StructureBlock(FluidSolidifierContent.glass,
                                com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED, true);
                else if (element == FluidSolidifierTE.ELEMENT_FIXED_OUTPUT) set(x, y, z,
                        MiningUnitContent.port, GatewayType.ITEM_OUTPUT.metadata);
            }
        }
        // Representative front service wall. All three ports can be moved anywhere on v=0.
        set(1, 1, 0, MiningUnitContent.port, GatewayType.FLUID_INPUT.metadata);
        set(2, 1, 0, MiningUnitContent.port, GatewayType.ITEM_INPUT.metadata);
        set(3, 1, 0, MiningUnitContent.port, GatewayType.ENERGY_INPUT.metadata);
    }

    private Component component(Block block, int metadata, String role) {
        return new Component(new ItemStack(block, 1, metadata), count(block, metadata), role);
    }
    private int count(Block block, int metadata) {
        int result = 0;
        for (int x = 0; x < getWidth(); x++) for (int y = 0; y < getHeight(); y++)
            for (int z = 0; z < getDepth(); z++) {
                StructureBlock entry = blocks[x][y][z];
                if (entry != null && entry.block == block && entry.metadata == metadata) result++;
            }
        return result;
    }
    private void set(int x, int y, int z, Block block, int metadata) {
        if (block == FluidSolidifierContent.casing && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.foundry(x-2,y,z)) block = FluidSolidifierContent.rib;
        blocks[x][y][z] = new StructureBlock(block, metadata);
    }
    @Override public String getName() { return "MultiTileFluidSolidifier"; }
    @Override public ItemStack getController() {
        return new ItemStack(ModBlocks.getFluidSolidifierController(), 1, 0);
    }
    @Override public int getWidth() { return FluidSolidifierTE.WIDTH; }
    @Override public int getHeight() { return FluidSolidifierTE.HEIGHT; }
    @Override public int getDepth() { return FluidSolidifierTE.DEPTH; }
    @Override public int getControllerX() { return 2; }
    @Override public int getControllerY() { return 0; }
    @Override public int getControllerZ() { return 0; }
    @Override public StructureBlock getBlock(int x, int y, int z) { return blocks[x][y][z]; }
    @Override public List<Component> getComponents() { return components; }
}
