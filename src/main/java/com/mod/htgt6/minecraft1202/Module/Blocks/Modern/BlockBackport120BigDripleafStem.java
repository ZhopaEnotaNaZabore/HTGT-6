package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Random;

public final class BlockBackport120BigDripleafStem extends Block {
    public BlockBackport120BigDripleafStem() {
        super(Material.plants);
        setBlockName("backport_1_20.big_dripleaf_stem");
        setBlockTextureName("htgt6:1_20_backport/big_dripleaf_stem");
        setHardness(0.1F);
        setResistance(Backport120Content.legacyResistance(0.1F));
        setStepSound(soundTypeGrass);
        setBlockBounds(0.375F, 0, 0.375F, 0.625F, 1, 0.625F);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return null; }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        Block above = world.getBlock(x, y + 1, z), below = world.getBlock(x, y - 1, z);
        boolean supported = below == this || BlockBackport120SmallDripleaf.isSoil(below);
        if (!supported || (above != this && above != Backport120Content.bigDripleaf)) world.setBlockToAir(x, y, z);
    }
}
