package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.chemical.BlockChemicalPlantCasing;
import com.mod.htgt6.common.chemical.ChemicalPlantContent;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.mining.MiningUnitContent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact 9x13x9 Chemical Plant preview generated from the server-side geometry function. */
public final class ChemicalPlantNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks = new StructureBlock[MultiTileChemicalPlant.WIDTH]
            [MultiTileChemicalPlant.HEIGHT][MultiTileChemicalPlant.DEPTH];
    private final List<Component> components;

    public ChemicalPlantNEIStructure() {
        build();
        List<Component> list = new ArrayList<Component>();
        list.add(component(ChemicalPlantContent.rib, 0, "tile.chemical_plant_rib.name"));
        list.add(component(ChemicalPlantContent.casing, BlockChemicalPlantCasing.STANDARD,
                "tile.chemical_plant_casing.standard.name"));
        list.add(component(ChemicalPlantContent.casing, BlockChemicalPlantCasing.THERMOCHEMICAL,
                "tile.chemical_plant_casing.thermochemical.name"));
        list.add(component(MiningUnitContent.frame, 0, "tile.mining_unit_frame.standard.name"));
        list.add(component(ChemicalPlantContent.glass, 0, "tile.unbreakable_glass.name"));
        for (GatewayType type : GatewayType.VALUES) {
            if (type == GatewayType.ENERGY_OUTPUT) continue;
            list.add(new Component(new ItemStack(MiningUnitContent.port, 1, type.metadata), 1,
                    "tile.mining_unit_port." + type.serializedName + ".name"));
        }
        components = Collections.unmodifiableList(list);
    }

    private void build() {
        for (int y = 0; y < MultiTileChemicalPlant.HEIGHT; y++) {
            for (int x = 0; x < MultiTileChemicalPlant.WIDTH; x++) {
                for (int z = 0; z < MultiTileChemicalPlant.DEPTH; z++) {
                    int element = MultiTileChemicalPlant.getRequiredElement(x - 4, y, z);
                    if (element == MultiTileChemicalPlant.ELEMENT_CONTROLLER) {
                        set(x, y, z, ChemicalPlantContent.controller, 2);
                    } else if (element == MultiTileChemicalPlant.ELEMENT_FLEXIBLE_BASE) {
                        set(x, y, z, ChemicalPlantContent.casing, BlockChemicalPlantCasing.STANDARD);
                    } else if (element == MultiTileChemicalPlant.ELEMENT_CHEMICAL_CASING) {
                        set(x, y, z, ChemicalPlantContent.casing, BlockChemicalPlantCasing.STANDARD);
                    } else if (element == MultiTileChemicalPlant.ELEMENT_THERMOCHEMICAL_CASING) {
                        set(x, y, z, ChemicalPlantContent.casing, BlockChemicalPlantCasing.THERMOCHEMICAL);
                    } else if (element == MultiTileChemicalPlant.ELEMENT_FRAME) {
                        set(x, y, z, MiningUnitContent.frame, 0);
                    } else if (element == MultiTileChemicalPlant.ELEMENT_GLASS) {
                        set(x, y, z, ChemicalPlantContent.glass, 0);
                    }
                }
            }
        }

        // Minimal valid set: one of each flexible I/O and exactly one energy input.
        set(0, 0, 0, MiningUnitContent.port, GatewayType.FLUID_INPUT.metadata);
        set(1, 0, 0, MiningUnitContent.port, GatewayType.FLUID_OUTPUT.metadata);
        set(2, 0, 0, MiningUnitContent.port, GatewayType.ITEM_INPUT.metadata);
        set(3, 0, 0, MiningUnitContent.port, GatewayType.ITEM_OUTPUT.metadata);
        set(0, 0, 1, MiningUnitContent.port, GatewayType.ENERGY_INPUT.metadata);
    }

    private Component component(Block block, int metadata, String role) {
        return new Component(new ItemStack(block, 1, metadata), count(block, metadata), role);
    }

    private int count(Block block, int metadata) {
        int count = 0;
        for (int x = 0; x < getWidth(); x++) for (int y = 0; y < getHeight(); y++) for (int z = 0; z < getDepth(); z++) {
            StructureBlock entry = blocks[x][y][z];
            if (entry != null && entry.block == block && entry.metadata == metadata) count++;
        }
        return count;
    }

    private void set(int x, int y, int z, Block block, int metadata) {
        if (block == ChemicalPlantContent.casing && com.mod.htgt6.common.multiblock.MultiblockTrimLayout.chemical(x-4,y,z)) block = ChemicalPlantContent.rib;
        blocks[x][y][z] = new StructureBlock(block, metadata);
    }

    @Override public String getName() { return "MultiTile Chemical Plant"; }
    @Override public ItemStack getController() { return new ItemStack(ChemicalPlantContent.controller, 1, 0); }
    @Override public int getWidth() { return MultiTileChemicalPlant.WIDTH; }
    @Override public int getHeight() { return MultiTileChemicalPlant.HEIGHT; }
    @Override public int getDepth() { return MultiTileChemicalPlant.DEPTH; }
    @Override public int getControllerX() { return 4; }
    @Override public int getControllerY() { return 0; }
    @Override public int getControllerZ() { return 0; }
    @Override public StructureBlock getBlock(int x, int y, int z) { return blocks[x][y][z]; }
    @Override public List<Component> getComponents() { return components; }
}
