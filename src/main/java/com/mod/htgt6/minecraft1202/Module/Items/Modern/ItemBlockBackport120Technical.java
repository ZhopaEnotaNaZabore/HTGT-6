package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import java.util.List;

/** Registered save-ID companion for blocks that must not expose a second public item. */
public final class ItemBlockBackport120Technical extends ItemBlock {
    public ItemBlockBackport120Technical(Block block) { super(block); setCreativeTab(null); }

    @Override public void getSubItems(Item item, CreativeTabs tab, List variants) { }
}
