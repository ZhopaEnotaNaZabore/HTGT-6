package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.CampfireWater157;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraftforge.common.util.ForgeDirection;
public final class Campfire157Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe u=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  TestWorld world=(TestWorld)u.allocateInstance(TestWorld.class);
  for(java.lang.reflect.Field f:net.minecraft.world.World.class.getDeclaredFields())if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(world,f.get(base));}
  world.cells=new java.util.HashMap<String,Object[]>();
  java.lang.reflect.Field accesses=net.minecraft.world.World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(world,new java.util.ArrayList());
  BlockCampfire149 fire=new BlockCampfire149();
  for(int side=0;side<6;side++){
   world.cells.clear();ForgeDirection d=ForgeDirection.getOrientation(side);int x=d.offsetX,y=10+d.offsetY,z=d.offsetZ;
   int[][] points={{0,10,0},{x,y,z},{x-1,y,z},{x+1,y,z},{x,y,z-1},{x,y,z+1}};
   for(int[] p:points)world.setBlock(p[0],p[1],p[2],fire,7,3);
   world.setBlock(x+3,y,z,fire,4,3);
   EntityPotion potion=new EntityPotion(world,0,10,0,new ItemStack(Items.potionitem,1,16384));
   MovingObjectPosition hit=new MovingObjectPosition(0,10,0,side,Vec3.createVectorHelper(0,10,0));
   CampfireWater157.onImpact(potion,hit);
   for(int[] p:points)check(world.getBlockMetadata(p[0],p[1],p[2])==3,"face footprint + facing "+side);
   check(world.getBlockMetadata(x+3,y,z)==4,"outside footprint untouched");
  }
  MovingObjectPosition hit=new MovingObjectPosition(0,10,0,1,Vec3.createVectorHelper(0,10,0));
  for(int mode=0;mode<6;mode++){
   world.cells.clear();world.setBlock(0,10,0,fire,4,3);
   ItemStack bottle=new ItemStack(Items.potionitem,1,mode==0?16385:16384);
   if(mode==1){NBTTagCompound tag=new NBTTagCompound();NBTTagList effects=new NBTTagList();NBTTagCompound effect=new NBTTagCompound();effect.setByte("Id",(byte)1);effect.setInteger("Duration",100);effects.appendTag(effect);tag.setTag("CustomPotionEffects",effects);bottle.setTagCompound(tag);}
   EntityPotion potion=new EntityPotion(world,0,10,0,bottle);
   world.isRemote=mode==2;world.unloaded=mode==3;world.rejectWrites=mode==4;
   if(mode==5)check(CampfireWater157.afterCancellation(true,potion,hit),"Bukkit cancellation preserved");else CampfireWater157.onImpact(potion,hit);
   check(world.getBlockMetadata(0,10,0)==4,"rejected mutation mode "+mode);
   world.isRemote=false;world.unloaded=false;world.rejectWrites=false;
  }
  check(!CampfireWater157.afterCancellation(false,new EntityPotion(world,0,10,0,16384),hit),"Bukkit allowed preserved");
  check(world.getBlockMetadata(0,10,0)==0,"allowed Bukkit impact extinguishes");
  System.out.println("PASS campfire157: "+checks);
 }
 public static class TestWorld extends Campfire149Fixture.TestWorld {
  java.util.Map<String,Object[]> cells;
  public net.minecraft.block.Block getBlock(int x,int y,int z){Object[] c=cells.get(x+","+y+","+z);return c==null?net.minecraft.init.Blocks.air:(net.minecraft.block.Block)c[0];}
  public int getBlockMetadata(int x,int y,int z){Object[] c=cells.get(x+","+y+","+z);return c==null?0:(Integer)c[1];}
  public boolean setBlock(int x,int y,int z,net.minecraft.block.Block b,int m,int flags){if(rejectWrites)return false;cells.put(x+","+y+","+z,new Object[]{b,m});return true;}
  public boolean setBlockMetadataWithNotify(int x,int y,int z,int m,int flags){return setBlock(x,y,z,getBlock(x,y,z),m,flags);}
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
}
