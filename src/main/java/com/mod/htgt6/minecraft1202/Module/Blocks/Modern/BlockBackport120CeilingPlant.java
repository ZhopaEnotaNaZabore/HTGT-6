package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/** Shared support rules for hanging roots and spore blossoms. */
public final class BlockBackport120CeilingPlant extends Block {
    private final boolean spores;

    public BlockBackport120CeilingPlant(String name, boolean spores) {
        super(Material.plants);
        this.spores = spores;
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + name);
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
        if (spores) setBlockBounds(-0.25F, 0.65F, -0.25F, 1.25F, 1.0F, 1.25F);
        else setBlockBounds(0.15F, 0.25F, 0.15F, 0.85F, 1.0F, 0.85F);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN);
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)) {
            dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        }
    }
    @SideOnly(Side.CLIENT)
    @Override public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        if (!spores) return;
        for (int i = 0; i < 2; ++i) {
            double px = x + random.nextDouble();
            double py = y - random.nextDouble() * 10.0D;
            double pz = z + random.nextDouble();
            world.spawnParticle("townaura", px, py, pz, 0.0D, -0.01D, 0.0D);
        }
    }
}
