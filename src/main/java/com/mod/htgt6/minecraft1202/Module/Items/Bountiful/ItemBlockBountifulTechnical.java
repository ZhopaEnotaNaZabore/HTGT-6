package com.mod.htgt6.minecraft1202.Module.Items.Bountiful;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

/**
 * Registry companion for blocks which are placed through a dedicated item.
 *
 * Forge 1.7.10 requires the ItemBlock entry to remain paired with its block in
 * saved ID maps.  It must therefore stay registered, but it must never be
 * exposed as a second, unusable item in creative tabs or NEI.
 */
public final class ItemBlockBountifulTechnical extends ItemBlock {
    public ItemBlockBountifulTechnical(Block block) {
        super(block);
        setCreativeTab(null);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubItems(net.minecraft.item.Item item, CreativeTabs tab, List variants) {
        // Intentionally empty: the public banner/door/slab item is registered separately.
    }
}
