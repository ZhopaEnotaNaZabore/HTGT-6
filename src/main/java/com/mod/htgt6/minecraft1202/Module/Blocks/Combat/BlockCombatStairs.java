package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.creativetab.CreativeTabs;

public final class BlockCombatStairs extends BlockStairs {
    public BlockCombatStairs(Block source) {
        super(source, 0);
        setBlockName("purpur_stairs");
        setCreativeTab(CreativeTabs.tabBlock);
        useNeighborBrightness = true;
    }
}
