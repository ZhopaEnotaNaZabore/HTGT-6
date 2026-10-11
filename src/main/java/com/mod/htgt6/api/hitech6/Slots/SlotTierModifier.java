package com.mod.htgt6.api.hitech6.Slots;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotTierModifier extends Slot {

    private final IInventory tile;
    private final int transformerSlot;

    public SlotTierModifier(
            IInventory inventory,
            IInventory tile,
            int slot,
            int x,
            int y,
            int transformerSlot
    ) {
        super(inventory, slot, x, y);

        this.tile = tile;
        this.transformerSlot = transformerSlot;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {

        if (stack == null)
            return false;

        ItemStack transformer =
                tile.getStackInSlot(transformerSlot);

        if (transformer == null)
            return false;

        Item item = stack.getItem();

        int moduleTier = MachineTierSystem.moduleTier(item);

        if (moduleTier == 0)
            return false;

        int maxTier = MachineTierSystem.transformerMaxTier(transformer);

        return moduleTier <= maxTier;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
