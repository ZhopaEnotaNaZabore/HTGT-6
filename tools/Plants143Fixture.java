package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Bamboo;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.util.Random;
public final class Plants143Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static Random two=new Random(){public int nextInt(int n){return n==2?1:0;}public float nextFloat(){return 1;}};
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  BlockBackport120Bamboo b=(BlockBackport120Bamboo)Backport120Content.bamboo;
  BlockBackport120BambooSapling sap=(BlockBackport120BambooSapling)Backport120Content.bambooSapling;
  ItemBlockBackport120Bamboo item=new ItemBlockBackport120Bamboo(b);
  w.setBlock(0,9,0,Blocks.dirt,0,2);
  check(item.placeBlockAt(null,null,w,0,10,0,1,0,0,0,0)&&w.getBlock(0,10,0)==sap,"ground sapling");
  check(item.placeBlockAt(null,null,w,0,11,0,1,0,0,0,0)&&w.getBlock(0,10,0)==b&&w.getBlock(0,11,0)==b,"stack above sapling");
  w.setBlockMetadataWithNotify(0,11,0,1,2);
  check(item.placeBlockAt(null,null,w,0,12,0,1,0,0,0,0)&&w.getBlockMetadata(0,12,0)==1,"stack preserves age");
  w.light=8;b.updateTick(w,0,12,0,two);check(w.isAirBlock(0,13,0),"dark no growth");
  w.light=9;b.updateTick(w,0,12,0,two);check(w.getBlock(0,13,0)==b,"light9 grows");
  check(b.func_149851_a(w,0,10,0,false),"bottom accepts bonemeal");
  b.func_149853_b(w,two,0,10,0);check(w.getBlock(0,15,0)==b,"bottom bonemeal grows top twice");
  w.rejectWrites=true;int a=w.getBlockMetadata(0,13,0),c=w.getBlockMetadata(0,14,0);
  b.func_149853_b(w,two,0,10,0);check(w.getBlockMetadata(0,13,0)==a&&w.getBlockMetadata(0,14,0)==c,"rejected growth preserves foliage");w.rejectWrites=false;
  for(int y=10;y<25;y++)w.setBlock(0,y,0,b,0,2);
  b.func_149853_b(w,two,0,10,0);check(w.getBlock(0,25,0)==b&&(w.getBlockMetadata(0,25,0)&8)!=0&&w.isAirBlock(0,26,0),"height16 terminal");
  check(!b.func_149851_a(w,0,10,0,false),"maxheight rejects bonemeal");
  w.cells.clear();w.setBlock(0,9,0,Blocks.dirt,0,2);w.setBlock(0,10,0,sap,0,2);w.rejectWrites=true;
  sap.func_149853_b(w,two,0,10,0);check(w.getBlock(0,10,0)==sap,"rejected sapling growth keeps sapling");w.rejectWrites=false;
  sap.func_149853_b(w,two,0,10,0);check(w.getBlockMetadata(0,10,0)==0&&w.getBlockMetadata(0,11,0)==2,"sapling modern initial leaves");
  BlockBackport120SweetBerryBush berry=(BlockBackport120SweetBerryBush)Backport120Content.sweetBerryBush;
  Cow cow=new Cow(w);cow.posX=.004;cow.lastTickPosX=0;cow.motionX=0;cow.motionY=1;
  w.setBlock(0,10,0,berry,1,2);berry.onEntityCollidedWithBlock(w,0,10,0,cow);check(cow.hits==1,"age1 actual movement damages");check(com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.BerryMovement147.consume(cow,false)&&cow.motionY==0,"queued slowdown clears inertia");
  cow.posX=0;cow.motionX=1;berry.onEntityCollidedWithBlock(w,0,10,0,cow);check(cow.hits==1,"motion without displacement harmless");
  cow.posX=.0029;berry.onEntityCollidedWithBlock(w,0,10,0,cow);check(cow.hits==1,"below threshold");
  w.setBlockMetadataWithNotify(0,10,0,0,2);cow.posX=.1;cow.motionX=1;berry.onEntityCollidedWithBlock(w,0,10,0,cow);check(cow.hits==1&&cow.motionX==1&&com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.BerryMovement147.consume(cow,false),"age0 queues slowdown without damage");
  net.minecraft.entity.item.EntityItem dropped=new net.minecraft.entity.item.EntityItem(w);dropped.motionX=1;berry.onEntityCollidedWithBlock(w,0,10,0,dropped);check(dropped.motionX==1,"nonliving unaffected");
  w.isRemote=true;w.setBlockMetadataWithNotify(0,10,0,3,2);berry.onEntityCollidedWithBlock(w,0,10,0,cow);check(cow.hits==1,"client no damage");
  System.out.println("PASS plants143: "+checks);
 }
 static final class Cow extends net.minecraft.entity.passive.EntityCow {int hits;Cow(World w){super(w);}public boolean attackEntityFrom(net.minecraft.util.DamageSource s,float a){hits++;return true;}}
}
