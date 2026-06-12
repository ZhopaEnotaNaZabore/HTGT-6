package com.mod.htgt6.common.inventory.slot;

import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
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

        int moduleTier = getModuleTier(item);

        if (moduleTier == 0)
            return false;

        int maxTier = getTransformerTier(transformer);

        return moduleTier <= maxTier;
    }

    private int getModuleTier(Item item) {

        if (item == Tmodules.TierModuleMV) return 2;
        if (item == Tmodules.TierModuleHV) return 3;
        if (item == Tmodules.TierModuleEV) return 4;
        if (item == Tmodules.TierModuleIV) return 5;
        if (item == Tmodules.TierModuleLuV) return 6;
        if (item == Tmodules.TierModuleZPM) return 7;
        if (item == Tmodules.TierModuleUV) return 8;
        if (item == Tmodules.TierModulePUV1) return 9;
        if (item == Tmodules.TierModuleUX) return 10;
        if (item == Tmodules.TierModuleOLV) return 11;
        if (item == Tmodules.TierModuleOMV) return 12;
        if (item == Tmodules.TierModuleOHV) return 13;
        if (item == Tmodules.TierModuleOEV) return 14;
        if (item == Tmodules.TierModuleOIV) return 15;
        if (item == Tmodules.TierModuleMAX) return 16;

        return 0;
    }

    private int getTransformerTier(ItemStack transformer) {

        Item item = transformer.getItem();

        if (item == Tmodules.LowLevelTransformer)
            return 4;

        if (item == Tmodules.MediumLevelTransformer)
            return 8;

        if (item == Tmodules.HighLevelTransformer)
            return 12;

        if (item == Tmodules.UltimateLevelTransformer)
            return 15;

        if (item == Tmodules.OmegaTransformer)
            return 16;

        return 0;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
