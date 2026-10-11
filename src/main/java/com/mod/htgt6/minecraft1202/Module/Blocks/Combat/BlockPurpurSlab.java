package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public final class BlockPurpurSlab extends BlockSlab {
    public BlockPurpurSlab(boolean full) {
        super(full, Material.rock);
        setBlockName(full ? "double_purpur_slab" : "purpur_slab");
        setBlockTextureName("htgt6:combat_update/purpur_block");
        setHardness(1.5F);
        setResistance(10.0F);
        if (!full) setCreativeTab(CreativeTabs.tabBlock);
        useNeighborBrightness = !full;
    }
    @Override public String func_150002_b(int metadata) { return getUnlocalizedName(); }
}
