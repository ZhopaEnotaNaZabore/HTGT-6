package com.mod.htgt6.api.hitech6.Slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDeepStorage extends Slot {

    public SlotDeepStorage(IInventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public int getSlotStackLimit() {
        return 4096;
    }

    // Optional overload for ItemStack-specific checks (no @Override needed for standard vanilla Slot)
    public int getSlotStackLimit(ItemStack stack) {
        return getSlotStackLimit();
    }
}
