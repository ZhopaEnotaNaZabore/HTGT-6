package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import java.util.List;
import net.minecraft.item.ItemStack;

/** Access bridge implemented by the transformed inventory; fields follow Minecraft 1.12. */
public interface InventoryPlayer112 {
    ItemStack[] htgt6$getOffHandInventory();
    List<ItemStack[]> htgt6$getAllInventories();
    void htgt6$setOffHandInventory(ItemStack[] inventory);
    void htgt6$setAllInventories(List<ItemStack[]> inventories);
}
