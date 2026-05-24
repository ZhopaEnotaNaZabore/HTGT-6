package com.mod.htgt6.common.inventory.slot;

// ==========================================
// SlotBattery.java
// ==========================================


import ic2.api.item.IElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotBattery extends Slot {

    public SlotBattery(IInventory inv, int id, int x, int y) {
        super(inv, id, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {

        if (stack == null) return false;

        return stack.getItem() instanceof IElectricItem;
    }
}