package com.mod.htgt6.common.inventory.slot;

// ==========================================
// SlotFluidContainer.java
// ==========================================



import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class SlotFluidContainer extends Slot {

    public SlotFluidContainer(IInventory inv, int id, int x, int y) {
        super(inv, id, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {

        if (stack == null) return false;

        return stack.getItem() instanceof IFluidContainerItem;
    }
}