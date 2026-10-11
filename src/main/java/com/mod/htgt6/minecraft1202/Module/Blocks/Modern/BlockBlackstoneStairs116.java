package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.*;
import net.minecraft.world.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import java.util.*;

/** Orientation occupies bits 0..2; bit 3 is the persistent water source. */
public final class BlockBlackstoneStairs116 extends BlockStairs implements IWaterloggedBlock150 {
    public BlockBlackstoneStairs116(Block source,String name) {
        super(source,0);setBlockName("backport_1_20."+name);setHarvestLevel("pickaxe",0);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabBlock);setStepSound(BuildingSounds116.STONE);setLightOpacity(0);useNeighborBrightness=true;
    }
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public int getRenderBlockPass(){return false?0:1;}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public boolean canRenderInPass(int pass){return com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.pass(pass,false);}
    @Override public int getLightOpacity(IBlockAccess w,int x,int y,int z){return false?255:isWaterlogged(w,x,y,z)?1:0;}
    public boolean isWaterlogged(IBlockAccess w,int x,int y,int z){return (w.getBlockMetadata(x,y,z)&8)!=0;}
    public boolean setWaterlogged(World w,int x,int y,int z,boolean wet){return ShapeWater116.set(w,x,y,z,this,8,wet);}
    @Override public int onBlockPlaced(World w,int x,int y,int z,int side,float hx,float hy,float hz,int meta){return super.onBlockPlaced(w,x,y,z,side,hx,hy,hz,0)|(ShapeWater116.present(w,x,y,z)?8:0);}
    @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase p,ItemStack stack) {
        int horizontal=net.minecraft.util.MathHelper.floor_double(p.rotationYaw*4F/360F+.5)&3;
        int orientation=new int[]{2,1,3,0}[horizontal];
        w.setBlockMetadataWithNotify(x,y,z,(w.getBlockMetadata(x,y,z)&12)|orientation,2);
    }
    @Override public float getExplosionResistance(net.minecraft.entity.Entity entity,World w,int x,int y,int z,double ex,double ey,double ez) {
        float solid=super.getExplosionResistance(entity,w,x,y,z,ex,ey,ez);
        return isWaterlogged(w,x,y,z)?Math.max(100F,solid):solid;
    }
    @Override public int damageDropped(int meta){return 0;}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){return ShapeWater116.bucket(this,w,x,y,z,p,side);}
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){return ShapeWater116.remove(w,x,y,z,isWaterlogged(w,x,y,z));}
    @Override public void onBlockExploded(World w,int x,int y,int z,Explosion explosion){boolean wet=isWaterlogged(w,x,y,z);super.onBlockExploded(w,x,y,z,explosion);if(wet&&w.isAirBlock(x,y,z))w.setBlock(x,y,z,net.minecraft.init.Blocks.water,0,3);}
    // Vanilla 1.7 ray tracing ignores the inner/outer corner collision shape.
    @Override public MovingObjectPosition collisionRayTrace(World w,int x,int y,int z,Vec3 start,Vec3 end) {
        List<AxisAlignedBB> boxes=new ArrayList<AxisAlignedBB>();
        addCollisionBoxesToList(w,x,y,z,AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+1,z+1),boxes,null);
        MovingObjectPosition nearest=null;double distance=Double.POSITIVE_INFINITY;
        for(AxisAlignedBB box:boxes) {
            MovingObjectPosition hit=box.calculateIntercept(start,end);
            if(hit!=null&&start.squareDistanceTo(hit.hitVec)<distance){distance=start.squareDistanceTo(hit.hitVec);nearest=new MovingObjectPosition(x,y,z,hit.sideHit,hit.hitVec);}
        }
        return nearest;
    }
    /** Hook also applies when a vanilla/mod stair examines one of our wet stairs. */
    public static boolean sameOrientation(BlockStairs owner,IBlockAccess w,int x,int y,int z,int expected) {
        Block other=w.getBlock(x,y,z);if(!(other instanceof BlockStairs))return false;
        int actual=w.getBlockMetadata(x,y,z);
        return owner instanceof BlockBlackstoneStairs116||other instanceof BlockBlackstoneStairs116 ? (actual&7)==(expected&7) : actual==expected;
    }
}
