package com.mod.htgt6.client.nei.structure;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Extensible controller-to-structure registry used by the NEI module. */
public final class HTGT6MultiblockRegistry {
    private static final List<HTGT6MultiblockStructure> STRUCTURES = new ArrayList<HTGT6MultiblockStructure>();
    private static boolean initialized;

    private HTGT6MultiblockRegistry() { }

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        register(new MiningUnitNEIStructure());
        register(new ChemicalPlantNEIStructure());
        register(new ElectronicFactoryNEIStructure());
        register(new IndustrialMixerNEIStructure());
        register(new FlotationChamberNEIStructure());
        register(new IndustrialCentrifugeNEIStructure());
        register(new IndustrialCrusherNEIStructure());
        register(new SuperMassiveCompressorNEIStructure());
        register(new FluidSolidifierNEIStructure());
        register(new SteamTurbineNEIStructure());
    }

    public static synchronized void register(HTGT6MultiblockStructure structure) {
        if (structure != null) STRUCTURES.add(structure);
    }

    public static List<HTGT6MultiblockStructure> all() {
        initialize();
        return Collections.unmodifiableList(STRUCTURES);
    }

    public static boolean same(ItemStack first, ItemStack second) {
        if (first == null || second == null || first.getItem() != second.getItem()) return false;
        int firstMeta = first.getItemDamage(), secondMeta = second.getItemDamage();
        return firstMeta == secondMeta || firstMeta == 32767 || secondMeta == 32767;
    }

    public static HTGT6MultiblockStructure findByController(Block controller) {
        if (controller == null) return null;
        for (HTGT6MultiblockStructure structure : all()) {
            ItemStack stack = structure.getController();
            if (stack != null && Block.getBlockFromItem(stack.getItem()) == controller) return structure;
        }
        return null;
    }

    public static boolean contains(HTGT6MultiblockStructure structure, ItemStack target) {
        if (same(structure.getController(), target)) return true;
        for (HTGT6MultiblockStructure.Component component : structure.getComponents()) {
            if (same(component.stack, target)) return true;
        }
        return false;
    }
}
