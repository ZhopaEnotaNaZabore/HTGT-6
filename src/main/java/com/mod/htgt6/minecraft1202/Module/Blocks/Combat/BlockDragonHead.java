package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Placeable dragon head with floor rotation and wall-facing metadata. */
public final class BlockDragonHead extends BlockContainer {
    public BlockDragonHead() {
        super(Material.circuits); setBlockName("dragonHead");
        // Vanilla BlockSkull's particle texture; the TESR/item model uses the dragon atlas.
        setBlockTextureName("soul_sand");
        setHardness(1.0F); setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.75F, 0.75F);
    }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityDragonHead(); }
    @Override public int onBlockPlaced(World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ, int metadata) { return side == 0 ? 1 : side; }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (world.getBlockMetadata(x, y, z) != 1) return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityDragonHead)
            ((TileEntityDragonHead) tile).setRotation(MathHelper.floor_double(placer.rotationYaw * 16.0F / 360.0F + 0.5D) & 15);
    }
    @Override public int getRenderType() { return -1; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return Item.getItemFromBlock(this); }
    @Override public int quantityDropped(Random random) { return 1; }
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int facing = world.getBlockMetadata(x, y, z);
        if (facing == 2) setBlockBounds(0.25F, 0.25F, 0.5F, 0.75F, 0.75F, 1.0F);
        else if (facing == 3) setBlockBounds(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 0.5F);
        else if (facing == 4) setBlockBounds(0.5F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
        else if (facing == 5) setBlockBounds(0.0F, 0.25F, 0.25F, 0.5F, 0.75F, 0.75F);
        else setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.75F, 0.75F);
    }
}
