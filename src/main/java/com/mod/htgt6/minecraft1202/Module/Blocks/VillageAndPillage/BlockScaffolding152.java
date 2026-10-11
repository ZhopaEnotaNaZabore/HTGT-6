package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.world.*;import net.minecraft.entity.*;import net.minecraft.entity.item.EntityFallingBlock;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.util.AxisAlignedBB;import net.minecraft.init.Blocks;import java.util.*;import net.minecraftforge.common.util.ForgeDirection;

/** Distance occupies bits 0..2, source water bit 3; bottom is derived from the block below. */
public final class BlockScaffolding152 extends Block implements IWaterloggedBlock150 {
    public BlockScaffolding152(){super(Material.circuits);setBlockName("village_pillage_scaffolding");setBlockTextureName("htgt6:village_pillage/scaffolding_top");setHardness(0);setStepSound(soundTypeWood);setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int getRenderType(){return VillagePillageContent.getJsonRenderId();}
    @Override public int damageDropped(int meta){return 0;}
    @Override public boolean isSideSolid(IBlockAccess w,int x,int y,int z,ForgeDirection side){return false;}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){return null;}
    public boolean bottom(IBlockAccess w,int x,int y,int z){return (w.getBlockMetadata(x,y,z)&7)>0&&w.getBlock(x,y-1,z)!=this;}
    @Override public void addCollisionBoxesToList(World w,int x,int y,int z,AxisAlignedBB query,List out,Entity entity){
        // The top can be crossed from below. Sneaking deliberately descends through it.
        double feet=entity==null?query.minY:entity.boundingBox.minY;
        if(feet>=y+1-1E-5&&(entity==null||!entity.isSneaking()))add(query,out,AxisAlignedBB.getBoundingBox(x,y+.875,z,x+1,y+1,z+1));
        else if(bottom(w,x,y,z)&&feet>=y-1E-5)add(query,out,AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+.125,z+1));
    }
    private static void add(AxisAlignedBB query,List out,AxisAlignedBB box){if(query.intersectsWith(box))out.add(box);}
    public int distance(World w,int x,int y,int z){
        if(y<=0)return 7;
        if(!w.blockExists(x,y-1,z))return -1;
        Block below=w.getBlock(x,y-1,z);int distance=7;
        if(below==this)distance=w.getBlockMetadata(x,y-1,z)&7;
        else if(below.isSideSolid(w,x,y-1,z,ForgeDirection.UP))return 0;
        int[][] offsets={{1,0},{-1,0},{0,1},{0,-1}};
        for(int[] d:offsets){if(!w.blockExists(x+d[0],y,z+d[1]))return -1;if(w.getBlock(x+d[0],y,z+d[1])==this)distance=Math.min(distance,(w.getBlockMetadata(x+d[0],y,z+d[1])&7)+1);}
        return distance;
    }
    @Override public void onBlockAdded(World w,int x,int y,int z){if(!w.isRemote)w.scheduleBlockUpdate(x,y,z,this,1);}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block neighbor){if(!w.isRemote)w.scheduleBlockUpdate(x,y,z,this,1);}
    @Override public void updateTick(World w,int x,int y,int z,Random random){
        if(w.isRemote||w.getBlock(x,y,z)!=this)return;int old=w.getBlockMetadata(x,y,z),d=distance(w,x,y,z);
        if(d<0){w.scheduleBlockUpdate(x,y,z,this,20);return;}
        if(d==7){
            if((old&7)==7){
                EntityFallingBlock falling=new EntityFallingBlock(w,x+.5,y+.5,z+.5,this,7);
                // Remove the source only after accepted entity creation, preserving its source fluid.
                falling.field_145812_b=1;
                if(w.spawnEntityInWorld(falling)){
                    if(!w.setBlock(x,y,z,(old&8)!=0?Blocks.water:Blocks.air,0,3))falling.setDead();
                }else w.scheduleBlockUpdate(x,y,z,this,1);
            }
            else if(w.setBlock(x,y,z,(old&8)!=0?Blocks.water:Blocks.air,0,3))dropBlockAsItem(w,x,y,z,0,0);
        }else if((old&7)!=d)w.setBlockMetadataWithNotify(x,y,z,d|(old&8),3);
    }
    @Override public boolean isWaterlogged(IBlockAccess w,int x,int y,int z){return (w.getBlockMetadata(x,y,z)&8)!=0;}
    @Override public boolean setWaterlogged(World w,int x,int y,int z,boolean wet){int meta=w.getBlockMetadata(x,y,z);return w.setBlockMetadataWithNotify(x,y,z,(meta&7)|(wet?8:0),3);}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){return WaterloggedBuckets150.use(this,w,x,y,z,p,side);}
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){return w.setBlock(x,y,z,isWaterlogged(w,x,y,z)?Blocks.water:Blocks.air,0,3);}
    @Override public int getFlammability(IBlockAccess w,int x,int y,int z,ForgeDirection face){return 60;}
    @Override public int getFireSpreadSpeed(IBlockAccess w,int x,int y,int z,ForgeDirection face){return 60;}
}
