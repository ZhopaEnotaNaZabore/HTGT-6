package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.creativetab.CreativeTabs;

/** Missing single-block Overworld flowers used by the 1.20.2 flower providers. */
public final class BlockBackport120Flower extends BlockBush {
    public BlockBackport120Flower(String name) {
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + name);
        setHardness(0.0F);
        setStepSound(Block.soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
}
