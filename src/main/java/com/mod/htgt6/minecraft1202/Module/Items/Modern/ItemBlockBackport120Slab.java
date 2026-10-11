package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;

/** Reflective GameRegistry constructor bridge for each modern slab pair. */
public final class ItemBlockBackport120Slab extends ItemSlab {
    public ItemBlockBackport120Slab(Block block, BlockBackport120Slab half,
                                    BlockBackport120Slab full, Boolean isFull) {
        super(block, half, full, isFull.booleanValue());
    }
}
