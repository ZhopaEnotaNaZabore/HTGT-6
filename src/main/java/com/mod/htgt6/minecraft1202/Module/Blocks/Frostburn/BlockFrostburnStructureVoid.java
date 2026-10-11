package com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/** Non-solid template placeholder matching the functional role of the 1.10 structure void. */
public final class BlockFrostburnStructureVoid extends Block {
    public BlockFrostburnStructureVoid() {
        // Portal is the closest 1.7 material contract: non-solid and non-colliding, but unlike
        // Material.air it is not replaceable, so the template placeholder survives placement.
        super(Material.portal);
        setBlockName("structure_void");
        setBlockTextureName("htgt6:frostburn/structure_void");
        setHardness(0.0F);
        setBlockBounds(5F/16F,5F/16F,5F/16F,11F/16F,11F/16F,11F/16F);
        setResistance(0.0F);
        setLightOpacity(0);
        disableStats();
    }

    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return -1; }
    @Override public int quantityDropped(java.util.Random random) { return 0; }
    @Override public boolean canEntityDestroy(net.minecraft.world.IBlockAccess world, int x, int y, int z, Entity entity) { return false; }
}
