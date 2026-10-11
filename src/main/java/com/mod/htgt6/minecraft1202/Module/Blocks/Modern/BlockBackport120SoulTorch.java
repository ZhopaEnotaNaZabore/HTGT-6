package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.BlockTorch;
import net.minecraft.creativetab.CreativeTabs;

/** Soul torch with vanilla soul-light brightness. */
public final class BlockBackport120SoulTorch extends BlockTorch {
    public BlockBackport120SoulTorch(){setBlockName("backport_1_20.soul_torch");
        setBlockTextureName("htgt6:1_20_backport/soul_torch");setLightLevel(10F/15F);
        setCreativeTab(CreativeTabs.tabDecorations);}
}
