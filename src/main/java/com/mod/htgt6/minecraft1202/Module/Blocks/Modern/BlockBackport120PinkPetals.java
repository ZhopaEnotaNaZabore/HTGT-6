package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Random;

/** Facing uses bits 0..1; flower amount 1..4 is stored as bits 2..3. */
public final class BlockBackport120PinkPetals extends Block implements IGrowable {
    public BlockBackport120PinkPetals() {
        super(Material.plants);
        setBlockName("backport_1_20.pink_petals");
        setBlockTextureName("htgt6:1_20_backport/pink_petals");
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0, 0, 0, 1, 0.1875F, 1);
    }
    static int facing(int metadata) { return metadata & 3; }
    static int amount(int metadata) { return ((metadata >>> 2) & 3) + 1; }
    static int state(int facing, int amount) { return (facing & 3) | ((Math.max(1, Math.min(4, amount)) - 1) << 2); }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        Block below = world.getBlock(x, y - 1, z);
        return super.canPlaceBlockAt(world, x, y, z) && (below == net.minecraft.init.Blocks.grass
                || below == net.minecraft.init.Blocks.dirt || below == Backport120Content.mossBlock);
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!canPlaceBlockAt(world, x, y, z)) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }
    }
    @Override public int quantityDropped(int metadata, int fortune, Random random) { return amount(metadata); }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return Item.getItemFromBlock(this); }
    @Override public int damageDropped(int metadata) { return 0; }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                               int side, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || held.getItem() != Item.getItemFromBlock(this)) return false;
        int metadata = world.getBlockMetadata(x, y, z), amount = amount(metadata);
        if (amount >= 4) return false;
        if (!world.isRemote) {
            world.setBlockMetadataWithNotify(x, y, z, state(facing(metadata), amount + 1), 3);
            if (!player.capabilities.isCreativeMode) --held.stackSize;
        }
        return true;
    }
    @Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) { return amount(world.getBlockMetadata(x, y, z)) < 4; }
    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
    @Override public void func_149853_b(World world, Random random, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x, y, z);
        if (amount(metadata) < 4) world.setBlockMetadataWithNotify(x, y, z, state(facing(metadata), amount(metadata) + 1), 3);
        else dropBlockAsItem(world, x, y, z, new ItemStack(this));
    }
}
