package com.mod.htgt6.api.hitech6.Slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotOutputDeep extends SlotDeepStorage {

    public SlotOutputDeep(IInventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        // Strictly block players from manually placing items into output slots
        return false;
    }
}
