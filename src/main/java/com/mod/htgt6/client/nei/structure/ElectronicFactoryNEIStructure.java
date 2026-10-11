package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.electronics.ElectronicFactoryContent;
import com.mod.htgt6.common.electronics.TEElectronicFactory;
import com.mod.htgt6.common.mining.MiningUnitContent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact 5x7x10 preview generated from the controller's server-side geometry function. */
public final class ElectronicFactoryNEIStructure implements HTGT6MultiblockStructure {
    private final StructureBlock[][][] blocks = new StructureBlock[TEElectronicFactory.WIDTH]
            [TEElectronicFactory.HEIGHT][TEElectronicFactory.DEPTH];
    private final List<Component> components;

    public ElectronicFactoryNEIStructure() {
        build();
        List<Component> list = new ArrayList<Component>();
        list.add(component(ElectronicFactoryContent.casing, 0,
                "tile.electronic_factory_casing.name"));
        list.add(component(ElectronicFactoryContent.rib, 0, "tile.electronic_factory_rib.name"));
        list.add(component(ElectronicFactoryContent.glass, com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED,
                "tile.unbreakable_glass.name"));
        for (GatewayType type : GatewayType.VALUES) {
            int amount = count(MiningUnitContent.port, type.metadata);
            if (amount > 0) list.add(new Component(new ItemStack(MiningUnitContent.port, 1, type.metadata),
                    amount, "tile.mining_unit_port." + type.serializedName + ".name"));
        }
        components = Collections.unmodifiableList(list);
    }

    private void build() {
        for (int y = 0; y < TEElectronicFactory.HEIGHT; y++) {
            for (int x = 0; x < TEElectronicFactory.WIDTH; x++) {
                for (int z = 0; z < TEElectronicFactory.DEPTH; z++) {
                    int element = TEElectronicFactory.getRequiredElement(x - 2, y - 3, z);
                    if (element == TEElectronicFactory.ELEMENT_CONTROLLER) {
                        set(x, y, z, ElectronicFactoryContent.controller, 2);
                    } else if (element == TEElectronicFactory.ELEMENT_OUTPUT_HATCH) {
                        set(x, y, z, MiningUnitContent.port, GatewayType.ITEM_OUTPUT.metadata);
                    } else if (element == TEElectronicFactory.ELEMENT_CASING
                            || element == TEElectronicFactory.ELEMENT_FLEXIBLE_FLOOR) {
                        set(x, y, z, ElectronicFactoryContent.casing, 0);
                    } else if (element == TEElectronicFactory.ELEMENT_RIB) {
                        set(x, y, z, ElectronicFactoryContent.rib, 0);
                    } else if (element == TEElectronicFactory.ELEMENT_GLASS) {
                        blocks[x][y][z] = new StructureBlock(ElectronicFactoryContent.glass,
                                com.mod.htgt6.common.multiblock.glass.BlockObservationGlass.REINFORCED, true);
                    }
                }
            }
        }

        // Minimal valid floor gateway set. The output remains fixed at the rear centre.
        set(1, 0, 1, MiningUnitContent.port, GatewayType.FLUID_INPUT.metadata);
        set(2, 0, 1, MiningUnitContent.port, GatewayType.FLUID_OUTPUT.metadata);
        set(3, 0, 1, MiningUnitContent.port, GatewayType.ITEM_INPUT.metadata);
        set(2, 0, 2, MiningUnitContent.port, GatewayType.ENERGY_INPUT.metadata);
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

    @Override public String getName() { return "TEElectronicFactory"; }
    @Override public ItemStack getController() {
        return new ItemStack(ElectronicFactoryContent.controller, 1, 0);
    }
    @Override public int getWidth() { return TEElectronicFactory.WIDTH; }
    @Override public int getHeight() { return TEElectronicFactory.HEIGHT; }
    @Override public int getDepth() { return TEElectronicFactory.DEPTH; }
    @Override public int getControllerX() { return 2; }
    @Override public int getControllerY() { return 3; }
    @Override public int getControllerZ() { return 0; }
    @Override public StructureBlock getBlock(int x, int y, int z) { return blocks[x][y][z]; }
    @Override public List<Component> getComponents() { return components; }
}
