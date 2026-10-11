package com.mod.htgt6.minecraft1202.Module.Items.Bountiful;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public final class ItemBlockBountifulMeta extends ItemBlock {
    public ItemBlockBountifulMeta(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override public int getMetadata(int damage) { return damage; }
    @Override public String getUnlocalizedName(net.minecraft.item.ItemStack stack) {
        return field_150939_a.getUnlocalizedName() + "." + stack.getItemDamage();
    }
}
