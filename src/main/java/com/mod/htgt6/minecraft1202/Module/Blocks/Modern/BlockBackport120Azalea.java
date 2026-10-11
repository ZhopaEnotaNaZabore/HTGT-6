package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120AzaleaTree;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public final class BlockBackport120Azalea extends BlockBush implements IGrowable {
    private final boolean flowering;

    public BlockBackport120Azalea(boolean flowering) {
        this.flowering = flowering;
        setBlockName("backport_1_20." + (flowering ? "flowering_azalea" : "azalea"));
        setBlockTextureName("htgt6:1_20_backport/" + (flowering ? "flowering_azalea_side" : "azalea_side"));
        setHardness(0.0F);
        setStepSound(Block.soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0, 0, 0, 1, 1, 1);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override protected boolean canPlaceBlockOn(Block block) {
        return block == Blocks.grass || block == Blocks.dirt || block == Blocks.farmland
                || block == Blocks.clay || block == Backport120Content.mossBlock
                || block == Backport120Content.rootedDirt;
    }
    @Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
        return world.getBlock(x, y, z) == this && canBlockStay(world, x, y, z)
                && y + WorldGenBackport120AzaleaTree.MAX_TREE_ABOVE < world.getHeight();
    }
    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
    @Override public void func_149853_b(World world, Random random, int x, int y, int z) {
        if (!func_149851_a(world, x, y, z, false)) return;
        if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world, random, x, y, z)) return;
        new WorldGenBackport120AzaleaTree().generate(world, random, x, y, z);
    }
    boolean isFlowering() { return flowering; }
}
