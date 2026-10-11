package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockPurpurSlab;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;

public final class ItemPurpurSlab extends ItemSlab {
    public ItemPurpurSlab(Block block, BlockPurpurSlab half, BlockPurpurSlab full, Boolean isFull) {
        super(block, half, full, isFull.booleanValue());
    }
}
