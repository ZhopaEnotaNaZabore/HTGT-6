package com.mod.htgt6.common.handler.recipes.modpackgenerated;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;

/** Registry-name based reference which does not hard-link optional mods. */
public final class GeneratedStackRef {

    private final String modId;
    private final String itemName;
    private final int count;
    private final int metadata;

    private GeneratedStackRef(String modId, String itemName, int count, int metadata) {
        if (modId == null || modId.length() == 0) throw new IllegalArgumentException("modId");
        if (itemName == null || itemName.length() == 0) throw new IllegalArgumentException("itemName");
        if (count < 0) throw new IllegalArgumentException("count must not be negative");
        this.modId = modId;
        this.itemName = itemName;
        this.count = count;
        this.metadata = metadata;
    }

    public static GeneratedStackRef of(String modId, String itemName) {
        return new GeneratedStackRef(modId, itemName, 1, 0);
    }

    public static GeneratedStackRef of(String modId, String itemName, int count, int metadata) {
        return new GeneratedStackRef(modId, itemName, count, metadata);
    }

    public ItemStack resolve() {
        ItemStack registered = GameRegistry.findItemStack(modId, itemName, 1);
        if (registered == null || registered.getItem() == null) return null;
        ItemStack result = registered.copy();
        result.stackSize = count;
        result.setItemDamage(metadata);
        return result;
    }

    @Override
    public String toString() {
        return modId + ':' + itemName + ':' + metadata;
    }
}
