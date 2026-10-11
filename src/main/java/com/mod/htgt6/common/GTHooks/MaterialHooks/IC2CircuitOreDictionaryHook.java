package com.mod.htgt6.common.GTHooks.MaterialHooks;

import net.minecraft.item.ItemStack;

/** IC2's basic circuit is a separate, lower-tier component in the HiTech6 chain. */
public final class IC2CircuitOreDictionaryHook {
    public static boolean shouldSkip(String oreName, ItemStack stack) {
        if (!isTierOneName(oreName) || stack == null || stack.getItem() == null) return false;
        return isBlocked(oreName, stack.getItem().delegate.name());
    }

    public static boolean isBlocked(String oreName, String registryName) {
        return isTierOneName(oreName) && "IC2:itemPartCircuit".equals(registryName);
    }

    private static boolean isTierOneName(String name) {
        return "circuitBasic".equals(name) || "gt:circuit1".equals(name);
    }

    private IC2CircuitOreDictionaryHook() { }
}
