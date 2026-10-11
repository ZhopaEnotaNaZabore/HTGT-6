package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.lang.reflect.*;
import java.util.*;
public final class Composter174Fixture {
 static int checks,failures;
 static void check(boolean ok,String name){checks++;if(!ok){failures++;System.out.println("FAIL "+name);}}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe u=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  TestWorld w=(TestWorld)u.allocateInstance(TestWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;w.spawns=new ArrayList();w.sounds=new ArrayList<String>();
  BlockVillagePoi120 b=(BlockVillagePoi120)VillagePillageContent.composter;w.block=b;
  TileEntityVillageComposter120 tile=new TileEntityVillageComposter120();tile.setWorldObj(w);tile.yCoord=10;w.tile=tile;
  tile.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds));check(w.meta==1&&w.heard("fill_success"),"guaranteed first layer success sound");
  w.sounds.clear();w.rand.setSeed(0);tile.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds));check(w.meta==1&&w.heard("fill"),"failed compost chance still sounds");
  w.meta=6;w.sounds.clear();tile.setInventorySlotContents(0,new ItemStack(Items.cake));check(w.meta==7&&w.scheduled==1&&w.delay==20&&w.heard("fill_success"),"seventh layer schedules twenty ticks");
  w.sounds.clear();b.updateTick(w,0,10,0,w.rand);check(w.meta==8&&w.heard("ready"),"maturation sound");
  w.sounds.clear();b.updateTick(w,0,10,0,w.rand);check(w.sounds.isEmpty(),"ready does not repeat");
  tile.decrStackSize(0,1);tile.markDirty();check(w.meta==0&&w.sounds.isEmpty(),"hopper output remains silent as modern output container");
  w.meta=8;Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);b.onBlockActivated(w,0,10,0,p,1,0,0,0);check(w.meta==0&&w.heard("empty"),"manual output sound");
  w.meta=6;w.rejectWrites=true;w.sounds.clear();int before=w.scheduled;tile.setInventorySlotContents(0,new ItemStack(Items.cake));check(w.meta==6&&w.scheduled==before&&!w.heard("fill_success"),"rejected write neither schedules nor reports success");
  w.meta=7;w.sounds.clear();b.updateTick(w,0,10,0,w.rand);check(w.meta==7&&w.sounds.isEmpty(),"rejected maturation has no ready sound");
  w.rejectWrites=false;w.isRemote=true;b.updateTick(w,0,10,0,w.rand);check(w.meta==7&&w.sounds.isEmpty(),"client no maturation sound");
  System.out.println("Composter174: checks="+checks+", failures="+failures);if(failures>0)throw new AssertionError(failures);
 }
 public static class TestWorld extends Campfire149Fixture.TestWorld {
  List<String> sounds;int scheduled,delay;
  protected TestWorld(){super();}
  boolean heard(String suffix){return sounds.contains("htgt6:block.composter."+suffix);}
  @Override public void playSoundEffect(double x,double y,double z,String sound,float volume,float pitch){sounds.add(sound);}
  @Override public void scheduleBlockUpdate(int x,int y,int z,Block block,int ticks){scheduled++;delay=ticks;}
 }
}
