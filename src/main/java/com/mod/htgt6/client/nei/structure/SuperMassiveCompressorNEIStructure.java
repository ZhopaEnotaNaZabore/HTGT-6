package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.TE.SuperMassiveCompressorContent;
import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import com.mod.htgt6.common.handler.ModBlocks;
import com.mod.htgt6.common.mining.MiningUnitContent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Layer view generated from the same exact 5x5x5 geometry as world validation. */
public final class SuperMassiveCompressorNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks = new StructureBlock[5][5][5];
    private final List<Component> components;

    public SuperMassiveCompressorNEIStructure() {
        for (int x = 0; x < 5; x++) for (int y = 0; y < 5; y++) for (int z = 0; z < 5; z++) {
            int element = TESuperMasicCompressor.getRequiredElement(x - 2, y, z);
            if (element == TESuperMasicCompressor.ELEMENT_CONTROLLER) {
                set(x, y, z, ModBlocks.getSuperMassiveCompressorController(), 2);
            } else if (element == TESuperMasicCompressor.ELEMENT_CASING
                    || element == TESuperMasicCompressor.ELEMENT_FLEXIBLE_BASE) {
                set(x, y, z, SuperMassiveCompressorContent.casing, 0);
            } else if (element == TESuperMasicCompressor.ELEMENT_GLASS) {
                set(x, y, z, SuperMassiveCompressorContent.glass, 0);
            }
        }
        set(0, 0, 1, MiningUnitContent.port, GatewayType.ITEM_INPUT.metadata);
        set(1, 0, 1, MiningUnitContent.port, GatewayType.ITEM_OUTPUT.metadata);
        set(3, 0, 1, MiningUnitContent.port, GatewayType.FLUID_INPUT.metadata);
        set(4, 0, 1, MiningUnitContent.port, GatewayType.ENERGY_INPUT.metadata);

        List<Component> list = new ArrayList<Component>();
        list.add(component(SuperMassiveCompressorContent.rib, 0, "tile.super_massive_compressor_rib.name"));
        list.add(component(SuperMassiveCompressorContent.casing, 0,
                "tile.super_massive_compressor_casing.name"));
        list.add(component(SuperMassiveCompressorContent.glass, 0,
                "tile.unbreakable_glass.name"));
        for (GatewayType type : GatewayType.VALUES) {
            int count = count(MiningUnitContent.port, type.metadata);
            if (count > 0) list.add(new Component(new ItemStack(MiningUnitContent.port, 1, type.metadata),
                    count, "tile.mining_unit_port." + type.serializedName + ".name"));
        }
        components = Collections.unmodifiableList(list);
    }

    private Component component(Block block, int meta, String role) {
        return new Component(new ItemStack(block, 1, meta), count(block, meta), role);
    }
    private int count(Block block, int meta) {
        int value = 0;
        for (int x = 0; x < 5; x++) for (int y = 0; y < 5; y++) for (int z = 0; z < 5; z++) {
            StructureBlock entry = blocks[x][y][z];
            if (entry != null && entry.block == block && entry.metadata == meta) value++;
        }
        return value;
    }
    private void set(int x, int y, int z, Block block, int meta) {
        if (block == SuperMassiveCompressorContent.casing && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.compressor(x-2,y,z)) block = SuperMassiveCompressorContent.rib;
        blocks[x][y][z] = new StructureBlock(block, meta);
    }
    @Override public String getName() { return "Super-Massive Compressor"; }
    @Override public ItemStack getController() {
        return new ItemStack(ModBlocks.getSuperMassiveCompressorController(), 1, 0);
    }
    @Override public int getWidth() { return 5; }
    @Override public int getHeight() { return 5; }
    @Override public int getDepth() { return 5; }
    @Override public int getControllerX() { return 2; }
    @Override public int getControllerY() { return 0; }
    @Override public int getControllerZ() { return 0; }
    @Override public StructureBlock getBlock(int x, int y, int z) { return blocks[x][y][z]; }
    @Override public List<Component> getComponents() { return components; }
}
