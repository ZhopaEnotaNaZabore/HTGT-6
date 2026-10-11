package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.lang.reflect.*;
import java.util.*;
public final class Composter175Fixture {
 static int checks;
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe u=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  TestWorld w=(TestWorld)u.allocateInstance(TestWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  w.provider.worldObj=w;w.sounds=new ArrayList<String>();w.points=new ArrayList<double[]>();
  BlockVillagePoi120 b=(BlockVillagePoi120)VillagePillageContent.composter;w.block=b;
  TileEntityVillageComposter120 tile=new TileEntityVillageComposter120();tile.setWorldObj(w);tile.yCoord=10;w.tile=tile;
  tile.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds));check(w.events==1&&w.level==1&&w.points.isEmpty(),"hopper publishes level without server particles");
  w.rand.setSeed(0);tile.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds));check(w.events==2&&w.level==1,"failed chance also publishes event");
  w.meta=7;tile.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds));check(w.events==2,"no event while maturing");
  check(b.onBlockEventReceived(w,0,10,0,1,1)&&w.points.isEmpty(),"server accepts event without rendering");
  w.isRemote=true;
  for(int level=0;level<=8;level++){
   w.points.clear();check(b.onBlockEventReceived(w,0,10,0,1,level)&&w.points.size()==10,"ten particles per event");
   double floor=Math.max(2,1+2*Math.min(level,7))/16D+.03125D;
   for(double[] p:w.points)check(p[0]>=.13125&&p[0]<=.86875&&p[2]>=.13125&&p[2]<=.86875&&p[1]>=10+floor&&p[1]<=11,"particle inside cavity above compost");
  }
  w.points.clear();b.onBlockEventReceived(w,0,10,0,1,-1);b.onBlockEventReceived(w,0,10,0,1,9);b.onBlockEventReceived(w,0,10,0,2,1);check(w.points.isEmpty(),"invalid events do not render");
  System.out.println("PASS composter175: "+checks);
 }
 public static class TestWorld extends Composter174Fixture.TestWorld{
  int events,level;List<double[]> points;
  protected TestWorld(){super();}
  @Override public void addBlockEvent(int x,int y,int z,Block b,int id,int data){if(b!=block||id!=1)throw new AssertionError("event routing");events++;level=data;}
  @Override public void spawnParticle(String name,double x,double y,double z,double vx,double vy,double vz){if(!"happyVillager".equals(name))throw new AssertionError(name);points.add(new double[]{x,y,z});}
 }
}
