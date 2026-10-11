package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Solid invisible administrator block with a visible inventory icon. */
public final class BlockBarrier extends Block {
    public BlockBarrier() {
        super(Material.rock);
        setBlockName("barrier"); setBlockTextureName("htgt6:bountiful/barrier");
        setBlockUnbreakable(); setResistance(6000000.0F); setCreativeTab(null);
        disableStats();
    }
    @Override public int getRenderType() { return -1; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public int getMobilityFlag(){return 2;}
}
