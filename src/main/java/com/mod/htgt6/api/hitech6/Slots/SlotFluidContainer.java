package com.mod.htgt6.api.hitech6.Slots;

import com.mod.htgt6.common.TE.UniversalEngineTE;

// ==========================================
// SlotFluidContainer.java
// ==========================================





import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

public class SlotFluidContainer extends Slot {

    public SlotFluidContainer(IInventory inv, int id, int x, int y) {
        super(inv, id, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack == null) return false;
        if (FluidContainerRegistry.isContainer(stack) || stack.getItem() instanceof IFluidContainerItem) return true;
        // The universal engine shares this input slot with registered solid
        // fuels, including GT6 OP-generated MetaItems.
        return inventory instanceof UniversalEngineTE && UniversalEngineTE.isSolidFuel(stack);
    }
}
