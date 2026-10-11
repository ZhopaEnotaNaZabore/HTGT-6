package com.mod.htgt6.minecraft1202.Module.Items.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockRedSandstoneSlab;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;

/** Exact constructor bridge required by Forge 1.7.10 GameRegistry's reflective lookup. */
public final class ItemBlockRedSandstoneSlab extends ItemSlab {
    public ItemBlockRedSandstoneSlab(Block block, BlockRedSandstoneSlab single,
            BlockRedSandstoneSlab doubleSlab, Boolean isDouble) {
        super(block, single, doubleSlab, isDouble.booleanValue());
    }
}
