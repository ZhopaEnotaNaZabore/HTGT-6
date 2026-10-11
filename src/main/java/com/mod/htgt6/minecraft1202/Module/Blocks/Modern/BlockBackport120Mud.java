package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.IBlockAccess;

/** Mud has the modern lowered collision/surface profile instead of behaving as dirt. */
public final class BlockBackport120Mud extends Block {
    public BlockBackport120Mud() {
        super(Material.ground);
        setBlockName("backport_1_20.mud");
        setBlockTextureName("htgt6:1_20_backport/mud");
        setHardness(0.5F);
        setResistance(Backport120Content.legacyResistance(0.5F));
        setStepSound(soundTypeGravel);
        setHarvestLevel("shovel", 0);
        setCreativeTab(CreativeTabs.tabBlock);
        setBlockBounds(0, 0, 0, 1, 0.875F, 1);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(0, 0, 0, 1, 0.875F, 1);
    }
}
