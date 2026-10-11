package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public final class BlockEndGateway extends BlockContainer {
    public BlockEndGateway() {
        super(Material.portal);
        setBlockName("end_gateway");
        setBlockTextureName("minecraft:end_stone");
        setLightLevel(1.0F);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setBlockBounds(0, 0, 0, 1, 1, 1);
    }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityEndGateway(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType(){return -1;}
    @Override public int quantityDropped(Random random) { return 0; }
    /** Gateway has a selection box, but entities must be able to enter it to trigger teleportation. */
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (world.isRemote || entity.ridingEntity != null || entity.riddenByEntity != null) return;
        if (entity.getEntityData().getLong("HTGT6EndGateway") > world.getTotalWorldTime()) return;
        TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityEndGateway)((TileEntityEndGateway)tile).teleport(entity);
    }
}
