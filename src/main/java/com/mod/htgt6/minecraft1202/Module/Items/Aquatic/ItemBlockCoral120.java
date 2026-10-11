package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public final class ItemBlockCoral120 extends ItemBlock {
    private static final String[] TYPES = {"tube", "brain", "bubble", "fire", "horn"};
    public ItemBlockCoral120(Block block) { super(block); setHasSubtypes(true); setMaxDamage(0); }
    @Override public int getMetadata(int damage) { return damage >= 8 ? Math.min(12, damage) : Math.min(4, damage); }
    @Override public String getUnlocalizedName(ItemStack stack) {
        int meta = getMetadata(stack.getItemDamage());
        return "tile." + (meta >= 8 ? "dead_" : "") + TYPES[meta >= 8 ? meta - 8 : meta] + "_coral_block";
    }
}
