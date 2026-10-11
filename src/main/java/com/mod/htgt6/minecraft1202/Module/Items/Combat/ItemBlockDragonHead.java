package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public final class ItemBlockDragonHead extends ItemBlock {
    public ItemBlockDragonHead(Block block) { super(block); setMaxStackSize(64); }
    @Override public boolean isValidArmor(net.minecraft.item.ItemStack stack, int armorType,
            net.minecraft.entity.Entity wearer) {
        return armorType == 0;
    }
}
