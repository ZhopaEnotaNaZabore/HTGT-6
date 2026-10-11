package com.mod.htgt6.common.crusher;

import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/** Internal-only, invisible and untargetable. Does not replace real structure blocks. */
public final class BlockCrusherCollision extends BlockContainer {
    public BlockCrusherCollision() {
        super(Material.iron);
        setBlockName("industrial_crusher_collision");
        setBlockUnbreakable();
        setResistance(6000000F);
        setLightOpacity(0);
    }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileCrusherCollision(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return -1; }
    @Override public boolean canCollideCheck(int meta, boolean liquid) { return false; }
    @Override public Item getItemDropped(int meta, Random random, int fortune) { return null; }
    @Override public int quantityDropped(Random random) { return 0; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }

    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB query,
                                       List result, Entity entity) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileCrusherCollision)) return;
        // Only this machine's captured entities may enter its roller envelope.
        MultiTileIndustrialCrusher captured=CrusherConveyor.owner(entity);
        if(entity instanceof net.minecraft.entity.EntityLiving&&captured!=null&&((TileCrusherCollision)tile).belongsTo(captured)){
            // Let captured mobs enter the rollers, but retain a support plane.
            // Clients still run gravity/interpolation; removing every collision
            // made them fall below the belt between server position updates.
            double top=captured.yCoord+CrusherConveyorRules.DECK_Y;
            if(y<=top&&y+1>top){
                AxisAlignedBB support=AxisAlignedBB.getBoundingBox(x,top-.1,z,x+1,top,z+1);
                if(support.intersectsWith(query))result.add(support);
            }
            return;
        }
        for (double[] b : ((TileCrusherCollision) tile).getBoxes()) {
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x+b[0], y+b[1], z+b[2], x+b[3], y+b[4], z+b[5]);
            if (box.intersectsWith(query)) result.add(box);
        }
    }

    @Override public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) world.scheduleBlockUpdate(x, y, z, this, 100);
    }

    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote) return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileCrusherCollision)) { world.setBlockToAir(x, y, z); return; }
        TileCrusherCollision cell = (TileCrusherCollision) tile;
        // Do not load a controller's chunk merely to clean up its collision cells.
        MultiTileIndustrialCrusher owner = cell.owner();
        if (!cell.isBound() || cell.ownerLoaded() && (owner == null || !owner.isStructureFormed())) {
            world.setBlockToAir(x, y, z);
            return;
        }
        world.scheduleBlockUpdate(x, y, z, this, 100);
    }
}
