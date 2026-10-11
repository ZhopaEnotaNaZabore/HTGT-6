package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
public final class BlockBountifulFenceGate extends net.minecraft.block.BlockFenceGate {
    private final String wood;
    public BlockBountifulFenceGate(String wood){this.wood=wood;setBlockName(wood+"_fence_gate");setHardness(2);setResistance(5);setStepSound(soundTypeWood);setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabRedstone);}
    public void registerBlockIcons(IIconRegister r){blockIcon=r.registerIcon("minecraft:planks_"+wood);}
    public IIcon getIcon(int side,int meta){return blockIcon;}
    public boolean canPlaceBlockAt(World w,int x,int y,int z){return w.getBlock(x,y,z).isReplaceable(w,x,y,z);}
    public static int poweredState(int meta,boolean powered){return (meta&3)|(powered?12:0);}
    public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase e,ItemStack item){int direction=MathHelper.floor_double(e.rotationYaw*4/360+.5)&3;w.setBlockMetadataWithNotify(x,y,z,poweredState(direction,w.isBlockIndirectlyGettingPowered(x,y,z)),3);}
    public void onNeighborBlockChange(World w,int x,int y,int z,Block b){
        if(w.isRemote)return;int meta=w.getBlockMetadata(x,y,z);boolean powered=w.isBlockIndirectlyGettingPowered(x,y,z);
        if(powered!=((meta&8)!=0)){int next=poweredState(meta,powered);w.setBlockMetadataWithNotify(x,y,z,next,3);if((next&4)!=(meta&4))sound(w,x,y,z);}
    }
    public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){
        if(!p.capabilities.allowEdit)return false;if(w.isRemote)return true;
        int meta=w.getBlockMetadata(x,y,z),direction=meta&3;
        if((meta&4)==0){int face=MathHelper.floor_double(p.rotationYaw*4/360+.5)&3;if(direction==((face+2)&3))meta=(meta&~3)|face;}
        w.setBlockMetadataWithNotify(x,y,z,meta^4,3);sound(w,x,y,z);return true;
    }
    private void sound(World w,int x,int y,int z){w.playAuxSFXAtEntity(null,1003,x,y,z,0);}
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){int m=w.getBlockMetadata(x,y,z);if((m&4)!=0)return null;return (m&1)==0?AxisAlignedBB.getBoundingBox(x,y,z+.375,x+1,y+1.5,z+.625):AxisAlignedBB.getBoundingBox(x+.375,y,z,x+.625,y+1.5,z+1);}
    public static boolean inWall(IBlockAccess w,int x,int y,int z){int m=w.getBlockMetadata(x,y,z);return (m&1)==0?w.getBlock(x-1,y,z) instanceof BlockWall||w.getBlock(x+1,y,z) instanceof BlockWall:w.getBlock(x,y,z-1) instanceof BlockWall||w.getBlock(x,y,z+1) instanceof BlockWall;}
    public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){float top=inWall(w,x,y,z)?.8125F:1; if((w.getBlockMetadata(x,y,z)&1)==0)setBlockBounds(0,0,.375F,1,top,.625F);else setBlockBounds(.375F,0,0,.625F,top,1);}
}
