package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public final class ItemBlockDeadCoral120 extends ItemBlock {
    private static final String[] TYPES={"tube","brain","bubble","fire","horn"};
    private final boolean fan;
    public ItemBlockDeadCoral120(Block block){super(block);fan=block.getUnlocalizedName().contains("fan");setHasSubtypes(true);setMaxDamage(0);}
    @Override public int getMetadata(int damage){return Math.max(0,Math.min(4,damage));}
    @Override public String getUnlocalizedName(ItemStack stack){return "tile.dead_"+TYPES[getMetadata(stack.getItemDamage())]+"_coral"+(fan?"_fan":"");}
}
