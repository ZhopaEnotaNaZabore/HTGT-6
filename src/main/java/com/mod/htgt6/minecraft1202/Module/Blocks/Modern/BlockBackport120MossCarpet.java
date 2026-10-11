package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public final class BlockBackport120MossCarpet extends Block {
    public BlockBackport120MossCarpet() {
        super(Material.carpet);
        setBlockName("backport_1_20.moss_carpet");
        setBlockTextureName("htgt6:1_20_backport/moss_block");
        setHardness(0.1F);
        setResistance(Backport120Content.legacyResistance(0.1F));
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0, 0, 0, 1, 1.0F / 16.0F, 1);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && canStay(world, x, y, z);
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!canStay(world, x, y, z)) {
            dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        }
    }
    private boolean canStay(World world, int x, int y, int z) {
        // CarpetBlock.canSurvive in 1.20.2 only requires a non-air block below.
        return y > 0 && !world.isAirBlock(x, y - 1, z);
    }
}
