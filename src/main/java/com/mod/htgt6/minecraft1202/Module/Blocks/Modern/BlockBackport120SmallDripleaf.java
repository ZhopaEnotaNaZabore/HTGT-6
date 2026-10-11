package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/** Direction occupies bits 0..1; bit 2 marks the upper half. */
public final class BlockBackport120SmallDripleaf extends Block {
    public static final int UPPER = 4;
    public BlockBackport120SmallDripleaf() {
        super(Material.plants);
        setBlockName("backport_1_20.small_dripleaf");
        setBlockTextureName("htgt6:1_20_backport/small_dripleaf_top");
        setHardness(0.1F);
        setResistance(Backport120Content.legacyResistance(0.1F));
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0.2F, 0, 0.2F, 0.8F, 1, 0.8F);
    }
    static int facing(int metadata) { return metadata & 3; }
    static boolean upper(int metadata) { return (metadata & UPPER) != 0; }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        int state = world.getBlockMetadata(x, y, z);
        boolean valid = upper(state) ? world.getBlock(x, y - 1, z) == this
                : world.getBlock(x, y + 1, z) == this && isSoil(world.getBlock(x, y - 1, z));
        if (!valid) {
            if (!upper(state)) dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        }
    }
    public static boolean isSoil(Block block) {
        return block == net.minecraft.init.Blocks.clay || block == net.minecraft.init.Blocks.dirt
                || block == net.minecraft.init.Blocks.grass || block == Backport120Content.mossBlock;
    }
}
