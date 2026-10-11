package com.mod.htgt6.minecraft1202.Module.Gameplay.Shared;
import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.entity.Entity;import net.minecraft.util.*;import net.minecraft.world.World;
/** Position-aware fallback for 1.7 fluid queries. No current, chunk loading or material replacement. */
public final class WaterloggedFluid150 {
 private WaterloggedFluid150(){}
 public static boolean wet(World world,int x,int y,int z){
  if(y<0||y>=world.getHeight()||!world.blockExists(x,y,z))return false;
  Block block=world.getBlock(x,y,z);return block instanceof IWaterloggedBlock150&&((IWaterloggedBlock150)block).isWaterlogged(world,x,y,z);
 }
 private static double top(World world,int x,int y,int z){
  boolean above=world.blockExists(x,y+1,z)&&(world.getBlock(x,y+1,z).getMaterial()==Material.water||wet(world,x,y+1,z));
  return y+(above?1D:8D/9D);
 }
 public static boolean material(boolean original,World world,AxisAlignedBB box,Material material){
  if(original||material!=Material.water)return original;
  if(world==null||box==null||!(box.maxX>box.minX&&box.maxY>box.minY&&box.maxZ>box.minZ))return false;
  int x0=MathHelper.floor_double(box.minX),x1=MathHelper.floor_double(box.maxX-1E-7);
  int y0=Math.max(0,MathHelper.floor_double(box.minY)),y1=Math.min(world.getHeight()-1,MathHelper.floor_double(box.maxY-1E-7));
  int z0=MathHelper.floor_double(box.minZ),z1=MathHelper.floor_double(box.maxZ-1E-7);
  if(x1==Integer.MAX_VALUE||z1==Integer.MAX_VALUE||x0==Integer.MIN_VALUE||z0==Integer.MIN_VALUE)return false;
  long dx=(long)x1-x0+1,dy=(long)y1-y0+1,dz=(long)z1-z0+1;
  if(dx<=0||dy<=0||dz<=0||dx>4096||dy>4096||dz>4096||dx*dy*dz>1048576||!world.checkChunksExist(x0,y0,z0,x1,y1,z1))return false;
  for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++)if(wet(world,x,y,z)&&box.minY<top(world,x,y,z))return true;
  return false;
 }
 public static boolean anyLiquid(boolean original,World world,AxisAlignedBB box){return material(original,world,box,Material.water);}
 public static boolean eyes(boolean original,Entity entity,Material material){
  if(original||material!=Material.water||entity.worldObj==null)return original;
  double eye=entity.posY+entity.getEyeHeight();int x=MathHelper.floor_double(entity.posX),y=MathHelper.floor_double(eye),z=MathHelper.floor_double(entity.posZ);
  return wet(entity.worldObj,x,y,z)&&eye<top(entity.worldObj,x,y,z);
 }
}
