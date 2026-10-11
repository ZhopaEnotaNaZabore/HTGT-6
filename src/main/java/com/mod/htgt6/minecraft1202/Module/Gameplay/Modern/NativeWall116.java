package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneWall116;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.BlockWall;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.*;
import java.util.List;
/** Shared geometry keeps native and ported walls continuous across their boundary. */
public final class NativeWall116 {
    private static final class Holder {static final BlockBlackstoneWall116 SHAPE=new BlockBlackstoneWall116(Blocks.cobblestone,"wall_geometry_adapter");}
    public static boolean isNative(BlockWall wall){return wall==Blocks.cobblestone_wall;}
    public static int iconMetadata(int meta,BlockWall wall){return isNative(wall)?meta&1:meta;}
    public static BlockBlackstoneWall116.State state(IBlockAccess w,int x,int y,int z){return Holder.SHAPE.state(w,x,y,z);}
    public static void boxes(BlockWall wall,World w,int x,int y,int z,AxisAlignedBB mask,List out,Entity e){Holder.SHAPE.addCollisionBoxesToList(w,x,y,z,mask,out,e);}
    public static AxisAlignedBB bounds(BlockWall wall,World w,int x,int y,int z){return Holder.SHAPE.getCollisionBoundingBoxFromPool(w,x,y,z);}
    public static void selection(BlockWall wall,IBlockAccess w,int x,int y,int z){Holder.SHAPE.setBlockBoundsBasedOnState(w,x,y,z);wall.setBlockBounds((float)Holder.SHAPE.getBlockBoundsMinX(),(float)Holder.SHAPE.getBlockBoundsMinY(),(float)Holder.SHAPE.getBlockBoundsMinZ(),(float)Holder.SHAPE.getBlockBoundsMaxX(),(float)Holder.SHAPE.getBlockBoundsMaxY(),(float)Holder.SHAPE.getBlockBoundsMaxZ());}
    public static MovingObjectPosition ray(BlockWall wall,World w,int x,int y,int z,Vec3 from,Vec3 to){return Holder.SHAPE.collisionRayTrace(w,x,y,z,from,to);}
    public static int render(BlockWall wall){return wall==Blocks.cobblestone_wall?Backport120Content.getJsonRenderId():32;}
    public static void neighbor(BlockWall wall,World w,int x,int y,int z,net.minecraft.block.Block changed){Holder.SHAPE.onNeighborBlockChange(w,x,y,z,changed);}
    public static boolean wet(BlockWall wall,IBlockAccess w,int x,int y,int z){return wall==Blocks.cobblestone_wall&&(w.getBlockMetadata(x,y,z)&2)!=0;}
    public static boolean water(BlockWall wall,World w,int x,int y,int z,boolean wet){return wall==Blocks.cobblestone_wall&&ShapeWater116.set(w,x,y,z,wall,2,wet);}
    public static int placed(BlockWall wall,World w,int x,int y,int z,int side,float hx,float hy,float hz,int meta){return wall==Blocks.cobblestone_wall?(meta&1)|(ShapeWater116.present(w,x,y,z)?2:0):meta;}
    public static int dropped(BlockWall wall,int meta){return wall==Blocks.cobblestone_wall?meta&1:meta;}
    public static int picked(BlockWall wall,World w,int x,int y,int z){return dropped(wall,w.getBlockMetadata(x,y,z));}
    public static int opacity(BlockWall wall,IBlockAccess w,int x,int y,int z){return wet(wall,w,x,y,z)?1:0;}
    public static boolean activate(BlockWall wall,World w,int x,int y,int z,net.minecraft.entity.player.EntityPlayer p,int side,float hx,float hy,float hz){return wall==Blocks.cobblestone_wall&&ShapeWater116.bucket((com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150)wall,w,x,y,z,p,side);}
    public static boolean remove(BlockWall wall,World w,net.minecraft.entity.player.EntityPlayer p,int x,int y,int z,boolean harvest){return ShapeWater116.remove(w,x,y,z,wet(wall,w,x,y,z));}
    public static void explode(BlockWall wall,World w,int x,int y,int z,Explosion explosion){boolean water=wet(wall,w,x,y,z);w.setBlockToAir(x,y,z);wall.onBlockDestroyedByExplosion(w,x,y,z,explosion);if(water&&w.isAirBlock(x,y,z))w.setBlock(x,y,z,Blocks.water,0,3);}
    public static float resistance(BlockWall wall,Entity entity,World w,int x,int y,int z,double ex,double ey,double ez){float dry=wall.getExplosionResistance(entity);return wet(wall,w,x,y,z)?Math.max(100F,dry):dry;}
    public static int renderPass(BlockWall wall){return wall==Blocks.cobblestone_wall?1:0;}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public static boolean clientPass(BlockWall wall,int pass){return wall==Blocks.cobblestone_wall?com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.pass(pass,false):pass==0;}
    private NativeWall116(){}
}
