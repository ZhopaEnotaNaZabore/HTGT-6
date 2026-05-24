package com.mod.htgt6.common.inventory.slot;

// ==========================================
// SlotTierModifier.java
// ==========================================


import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotTierModifier extends Slot {

    private final TileEntityAssembler tile;

    private static final int TRANSFORMER_SLOT = 12;

    public SlotTierModifier(
            IInventory inventory,
            TileEntityAssembler tile,
            int slot,
            int x,
            int y
    ) {

        super(
                inventory,
                slot,
                x,
                y
        );

        this.tile = tile;
    }

    @Override
    public boolean isItemValid(
            ItemStack stack
    ) {

        if (stack == null)
            return false;

        ItemStack transformer =
                tile.getStackInSlot(
                        TRANSFORMER_SLOT
                );

        if (transformer == null)
            return false;

        String transformerName =
                transformer.getUnlocalizedName()
                        .toLowerCase();

        Item item =
                stack.getItem();

        int moduleTier = 0;

        if (item == Tmodules.TierModuleMV)
            moduleTier = 2;

        else if (item == Tmodules.TierModuleHV)
            moduleTier = 3;

        else if (item == Tmodules.TierModuleEV)
            moduleTier = 4;

        else if (item == Tmodules.TierModuleIV)
            moduleTier = 5;

        else if (item == Tmodules.TierModuleLuV)
            moduleTier = 6;

        else if (item == Tmodules.TierModuleZPM)
            moduleTier = 7;

        else if (item == Tmodules.TierModuleUV)
            moduleTier = 8;

        else if (item == Tmodules.TierModulePUV1)
            moduleTier = 9;

        else if (item == Tmodules.TierModuleUX)
            moduleTier = 10;

        else if (item == Tmodules.TierModuleOLV)
            moduleTier = 11;

        else if (item == Tmodules.TierModuleOMV)
            moduleTier = 12;

        else if (item == Tmodules.TierModuleOHV)
            moduleTier = 13;

        else if (item == Tmodules.TierModuleOEV)
            moduleTier = 14;

        else if (item == Tmodules.TierModuleOIV)
            moduleTier = 15;

        else if (item == Tmodules.TierModuleMAX)
            moduleTier = 16;

        else
            return false;

        int maxTier = 0;

        if (transformerName.contains(
                "lowleveltransformer"
        )) {

            maxTier = 4;
        }

        else if (transformerName.contains(
                "mediumleveltransformer"
        )) {

            maxTier = 8;
        }

        else if (transformerName.contains(
                "highleveltransformer"
        )) {

            maxTier = 12;
        }

        else if (transformerName.contains(
                "ultimateleveltransformer"
        )) {

            maxTier = 15;
        }

        else if (transformerName.contains(
                "omegatransformer"
        )) {

            maxTier = 16;
        }

        return moduleTier <= maxTier;
    }

    @Override
    public int getSlotStackLimit() {

        return 1;
    }
}