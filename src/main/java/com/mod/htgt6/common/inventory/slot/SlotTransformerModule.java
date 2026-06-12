package com.mod.htgt6.common.inventory.slot;


import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotTransformerModule extends Slot {

    private final IInventory tile;

    public SlotTransformerModule(
            IInventory inventory,
            IInventory tile,
            int slot,
            int x,
            int y) {

        super(inventory, slot, x, y);
        this.tile = tile;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {

        if (stack == null)
            return false;

        Item item = stack.getItem();

        if (tile instanceof TESuperMasicCompressor) {

            return item == Tmodules.LowLevelTransformer
                    || item == Tmodules.MediumLevelTransformer
                    || item == Tmodules.HighLevelTransformer
                    || item == Tmodules.UltimateLevelTransformer
                    || item == Tmodules.OmegaTransformer;
        }

        if (tile instanceof TileEntityAssembler) {

            return item == Tmodules.LowLevelTransformer
                    || item == Tmodules.MediumLevelTransformer
                    || item == Tmodules.HighLevelTransformer
                    || item == Tmodules.UltimateLevelTransformer
                    || item == Tmodules.OmegaTransformer;
        }

        return false;
    }
}
