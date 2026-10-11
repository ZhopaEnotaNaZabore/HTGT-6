package com.mod.htgt6.minecraft1202.Module.Blocks.Shared;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Chain;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/** Standing/hanging lantern. Bit zero stores the attachment, without new registry entries. */
public abstract class BlockLantern120 extends Block implements com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150 {
    protected BlockLantern120(String name,String texture,float light){
        super(Material.iron);setBlockName(name);setBlockTextureName(texture);
        setHardness(3.5F);setResistance(Backport120Content.legacyResistance(3.5F));setLightLevel(light);
        setCreativeTab(CreativeTabs.tabDecorations);setBlockBoundsForItemRender();
    }
    @Override public boolean isWaterlogged(IBlockAccess w,int x,int y,int z){return(w.getBlockMetadata(x,y,z)&2)!=0;}
    @Override public boolean setWaterlogged(World w,int x,int y,int z,boolean wet){int meta=w.getBlockMetadata(x,y,z);return !w.isRemote&&isWaterlogged(w,x,y,z)!=wet&&w.setBlockMetadataWithNotify(x,y,z,wet?meta|2:meta&~2,3);}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,net.minecraft.entity.player.EntityPlayer p,int side,float hx,float hy,float hz){return com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.WaterloggedBuckets150.use(this,w,x,y,z,p,side);}
    @Override public void onBlockDestroyedByPlayer(World w,int x,int y,int z,int meta){if(!w.isRemote&&(meta&2)!=0&&w.isAirBlock(x,y,z))w.setBlock(x,y,z,net.minecraft.init.Blocks.water,0,3);}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int damageDropped(int meta){return 0;}
    private boolean supported(World w,int x,int y,int z,boolean hanging){
        int supportY=y+(hanging?1:-1);if(supportY<0||supportY>=w.getHeight()||!w.blockExists(x,supportY,z))return false;
        Block b=w.getBlock(x,supportY,z);
        return b.isSideSolid(w,x,supportY,z,hanging?ForgeDirection.DOWN:ForgeDirection.UP)
            ||(b instanceof BlockFence||b instanceof BlockWall)
            ||b instanceof BlockBackport120Chain&&(w.getBlockMetadata(x,supportY,z)&12)==0;
    }
    @Override public boolean canPlaceBlockAt(World w,int x,int y,int z){return super.canPlaceBlockAt(w,x,y,z)&&(supported(w,x,y,z,false)||supported(w,x,y,z,true));}
    @Override public boolean canPlaceBlockOnSide(World w,int x,int y,int z,int side){return canPlaceBlockAt(w,x,y,z);}
    @Override public int onBlockPlaced(World w,int x,int y,int z,int side,float hx,float hy,float hz,int meta){
        int wet=w.getBlock(x,y,z).getMaterial()==Material.water?2:0;
        if(side==0&&supported(w,x,y,z,true))return wet|1;
        return wet|(supported(w,x,y,z,false)?0:1);
    }
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block changed){
        if(!w.isRemote&&!supported(w,x,y,z,(w.getBlockMetadata(x,y,z)&1)!=0)){
            Block replacement=isWaterlogged(w,x,y,z)?net.minecraft.init.Blocks.water:net.minecraft.init.Blocks.air;
            if(w.setBlock(x,y,z,replacement,0,3))dropBlockAsItem(w,x,y,z,0,0);
        }
    }
    @Override public void setBlockBoundsForItemRender(){setBlockBounds(5F/16F,0,5F/16F,11F/16F,9F/16F,11F/16F);}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){float dy=(w.getBlockMetadata(x,y,z)&1)/16F;setBlockBounds(5F/16F,dy,5F/16F,11F/16F,9F/16F+dy,11F/16F);}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){setBlockBoundsBasedOnState(w,x,y,z);return super.getCollisionBoundingBoxFromPool(w,x,y,z);}
    @Override public void addCollisionBoxesToList(World w,int x,int y,int z,AxisAlignedBB mask,List out,Entity entity){
        double dy=(w.getBlockMetadata(x,y,z)&1)/16D;
        add(mask,out,AxisAlignedBB.getBoundingBox(x+5D/16,y+dy,z+5D/16,x+11D/16,y+dy+7D/16,z+11D/16));
        add(mask,out,AxisAlignedBB.getBoundingBox(x+6D/16,y+dy+7D/16,z+6D/16,x+10D/16,y+dy+9D/16,z+10D/16));
    }
    private static void add(AxisAlignedBB mask,List out,AxisAlignedBB box){if(mask.intersectsWith(box))out.add(box);}
}
