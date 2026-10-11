package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120CherryTree;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public final class BlockBackport120CherrySapling extends BlockBush implements IGrowable {
    public BlockBackport120CherrySapling() {
        setBlockName("backport_1_20.cherry_sapling");
        setBlockTextureName("htgt6:1_20_backport/cherry_sapling");
        setHardness(0.0F);
        setStepSound(Block.soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0.1F, 0, 0.1F, 0.9F, 0.8F, 0.9F);
        setTickRandomly(true);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override protected boolean canPlaceBlockOn(Block block) {
        return block == Blocks.grass || block == Blocks.dirt || block == Blocks.farmland
                || block == Backport120Content.mossBlock;
    }
    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        super.updateTick(world, x, y, z, random);
        if (!world.isRemote && world.getBlockLightValue(x, y + 1, z) >= 9
                && canGrowHere(world, x, y, z) && random.nextInt(7) == 0)
            grow(world, random, x, y, z);
    }
    @Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
        return canGrowHere(world, x, y, z);
    }
    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
    @Override public void func_149853_b(World world, Random random, int x, int y, int z) { grow(world, random, x, y, z); }
    private void grow(World world, Random random, int x, int y, int z) {
        if (!canGrowHere(world, x, y, z)) return;
        if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world, random, x, y, z)) return;
        new WorldGenBackport120CherryTree().generate(world, random, x, y, z);
    }
    private boolean canGrowHere(World world, int x, int y, int z) {
        return world.getBlock(x, y, z) == this && canBlockStay(world, x, y, z)
                && y + WorldGenBackport120CherryTree.MAX_TREE_ABOVE < world.getHeight();
    }
}
