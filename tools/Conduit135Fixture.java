package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.TileEntityConduit120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import java.lang.reflect.*;
import java.util.*;
public class Conduit135Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static class Hostile extends EntitySlime {
  boolean wet=true;int hits;float damage;
  Hostile(World w){super(w);setHealth(4);}
  public boolean isWet(){return wet;}
  public boolean attackEntityFrom(DamageSource source,float value){hits++;damage=value;return true;}
 }
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  Field f=World.class.getDeclaredField("worldInfo");f.setAccessible(true);f.set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  f=World.class.getDeclaredField("worldAccesses");f.setAccessible(true);f.set(w,new ArrayList());w.provider.worldObj=w;
  TileEntityConduit120 c=new TileEntityConduit120();c.setWorldObj(w);c.xCoord=0;c.yCoord=10;c.zCoord=0;
  Hostile mob=new Hostile(w);
  for(int[] xyz:new int[][]{{0,10,0},{7,10,0},{8,10,0},{6,10,6},{-8,10,0},{0,18,0}}){mob.setPosition(xyz[0]+.5,xyz[1],xyz[2]+.5);int dx=xyz[0],dy=xyz[1]-10,dz=xyz[2];check(c.canAffect(mob,8)==(dx*dx+dy*dy+dz*dz<64),"sphere excludes cube corners and boundary");}
  mob.setPosition(1,10,1);mob.wet=false;check(!c.canAffect(mob,8),"dry excluded");mob.wet=true;check(c.canAffect(mob,8)&&!mob.isInWater(),"wet outside water accepted");
  mob.isDead=true;check(!c.canAffect(mob,8),"dead excluded");mob.isDead=false;
  for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)w.setBlock(x,10+y,z,Blocks.water,0,2);
  for(int x=-2;x<=2;x++)for(int y=-2;y<=2;y++)for(int z=-2;z<=2;z++)if(TileEntityConduit120.isFrameOffset(x,y,z))w.setBlock(x,10+y,z,BountifulContent.seaLantern,0,2);
  w.loadedEntityList.add(mob);c.updateEntity();check(mob.hits==1&&mob.damage==4F,"non EntityMob hostile receives attack");
  mob.setPosition(6,10,6);c.updateEntity();check(mob.hits==1,"outside sphere not attacked");
  mob.setPosition(1,10,1);w.isRemote=true;c.updateEntity();check(mob.hits==1,"client cannot attack");w.isRemote=false;
  w.setBlock(0,12,0,Blocks.stone,0,2);c.updateEntity();check(c.isActive()&&mob.hits==1,"incomplete offensive frame cannot attack");
  System.out.println("PASS conduit135: "+checks);
 }
}
