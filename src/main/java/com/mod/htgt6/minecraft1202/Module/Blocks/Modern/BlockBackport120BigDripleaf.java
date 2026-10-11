package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

/** Leaf stores facing in bits 0..1 and the four modern tilt values in bits 2..3. */
public final class BlockBackport120BigDripleaf extends Block implements IGrowable {
    public BlockBackport120BigDripleaf() {
        super(Material.plants);
        setBlockName("backport_1_20.big_dripleaf");
        setBlockTextureName("htgt6:1_20_backport/big_dripleaf_top");
        setHardness(0.1F);
        setResistance(Backport120Content.legacyResistance(0.1F));
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0, 0.6875F, 0, 1, 0.9375F, 1);
    }
    static int facing(int metadata) { return metadata & 3; }
    static int tilt(int metadata) { return (metadata >>> 2) & 3; }
    static int state(int facing, int tilt) { return (facing & 3) | ((tilt & 3) << 2); }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int facing = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, state(facing, 0), 2);
    }
    @Override public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        int metadata = world.getBlockMetadata(x, y, z);
        if (!world.isRemote && tilt(metadata) == 0 && entity instanceof EntityLivingBase) {
            world.setBlockMetadataWithNotify(x, y, z, state(facing(metadata), 1), 3);
            world.scheduleBlockUpdate(x, y, z, this, 10);
        }
    }
    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        int metadata = world.getBlockMetadata(x, y, z);
        int tilt = tilt(metadata);
        if (tilt == 1 || tilt == 2) {
            world.setBlockMetadataWithNotify(x, y, z, state(facing(metadata), tilt + 1), 3);
            world.scheduleBlockUpdate(x, y, z, this, tilt == 1 ? 10 : 100);
        } else if (tilt == 3) world.setBlockMetadataWithNotify(x, y, z, state(facing(metadata), 0), 3);
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        Block below = world.getBlock(x, y - 1, z);
        if (below != Backport120Content.bigDripleafStem && !BlockBackport120SmallDripleaf.isSoil(below)) {
            dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlockToAir(x, y, z);
        }
    }
    @Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
        return y + 1 < world.getHeight() && world.isAirBlock(x, y + 1, z);
    }
    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
    @Override public void func_149853_b(World world, Random random, int x, int y, int z) {
        if (!world.isAirBlock(x, y + 1, z)) return;
        int facing = facing(world.getBlockMetadata(x, y, z));
        world.setBlock(x, y, z, Backport120Content.bigDripleafStem, facing, 2);
        world.setBlock(x, y + 1, z, this, state(facing, 0), 3);
    }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }
}
