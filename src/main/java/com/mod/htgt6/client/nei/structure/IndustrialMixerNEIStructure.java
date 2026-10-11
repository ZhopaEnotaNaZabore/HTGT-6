package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.mining.MiningUnitContent;
import com.mod.htgt6.common.mixer.IndustrialMixerContent;
import com.mod.htgt6.common.mixer.MultiTileIndustrialMixer;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact layer-by-layer 5x10x5 preview generated from the validation geometry. */
public final class IndustrialMixerNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks = new StructureBlock[MultiTileIndustrialMixer.WIDTH]
            [MultiTileIndustrialMixer.HEIGHT][MultiTileIndustrialMixer.DEPTH];
    private final List<Component> components;

    public IndustrialMixerNEIStructure() {
        build();
        List<Component> list = new ArrayList<Component>();
        list.add(component(IndustrialMixerContent.rib, 0, "tile.industrial_mixer_rib.name"));
        list.add(component(IndustrialMixerContent.casing, 0,
                "tile.industrial_mixer_casing.name"));
        list.add(component(IndustrialMixerContent.glass, com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED,
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
                int element = MultiTileIndustrialMixer.getRequiredElement(x - 2, y, z);
                if (element == MultiTileIndustrialMixer.ELEMENT_CONTROLLER) {
                    set(x, y, z, IndustrialMixerContent.controller, 2);
                } else if (element == MultiTileIndustrialMixer.ELEMENT_CASING
                        || element == MultiTileIndustrialMixer.ELEMENT_FLEXIBLE_BASE) {
                    set(x, y, z, IndustrialMixerContent.casing, 0);
                } else if (element == MultiTileIndustrialMixer.ELEMENT_GLASS) {
                    blocks[x][y][z] = new StructureBlock(IndustrialMixerContent.glass,
                            com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED, true);
                }
            }
        }

        // Representative valid service deck: fluid ports are optional for item-only recipes.
        set(0, 0, 1, MiningUnitContent.port, GatewayType.ITEM_INPUT.metadata);
        set(1, 0, 1, MiningUnitContent.port, GatewayType.ITEM_OUTPUT.metadata);
        set(2, 0, 1, MiningUnitContent.port, GatewayType.FLUID_INPUT.metadata);
        set(3, 0, 1, MiningUnitContent.port, GatewayType.FLUID_OUTPUT.metadata);
        set(4, 0, 1, MiningUnitContent.port, GatewayType.ENERGY_INPUT.metadata);
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
        if (block == IndustrialMixerContent.casing && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.mixer(x-2,y,z)) block = IndustrialMixerContent.rib;
        blocks[x][y][z] = new StructureBlock(block, metadata);
    }

    @Override public String getName() { return "MultiTileIndustrialMixer"; }
    @Override public ItemStack getController() {
        return new ItemStack(IndustrialMixerContent.controller, 1, 0);
    }
    @Override public int getWidth() { return MultiTileIndustrialMixer.WIDTH; }
    @Override public int getHeight() { return MultiTileIndustrialMixer.HEIGHT; }
    @Override public int getDepth() { return MultiTileIndustrialMixer.DEPTH; }
    @Override public int getControllerX() { return 2; }
    @Override public int getControllerY() { return 0; }
    @Override public int getControllerZ() { return 0; }
    @Override public StructureBlock getBlock(int x, int y, int z) { return blocks[x][y][z]; }
    @Override public List<Component> getComponents() { return components; }
}
