package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemAquaticFishBucket120;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;
public final class Fish109Fixture {
 static int checks,oldFailures;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  field(World.class,"worldInfo").set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  field(net.minecraft.world.WorldProvider.class,"worldObj").set(world.provider,world);
  field(World.class,"worldAccesses").set(world,new ArrayList());
  field(World.class,"theProfiler").set(world,new net.minecraft.profiler.Profiler());
  for(Block water:new Block[]{Blocks.water,Blocks.flowing_water})for(int meta=0;meta<4;meta++)for(int state=0;state<(meta==2?3:1);state++)for(double y:new double[]{10.05,10.25,10.5,10.8,10.99}) {
   world.cells.clear();for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int h=9;h<=13;h++)world.setBlock(x,h,z,water,0,2);
   EntityAquaticFish120 fish=ItemAquaticFishBucket120.create(world,meta);
   if(fish instanceof EntityAquaticFish120.Pufferfish)((EntityAquaticFish120.Pufferfish)fish).setPuffState(state);
   fish.setPosition(.5,y,.5);
   boolean old=world.handleMaterialAcceleration(fish.boundingBox.expand(0,-.4000000059604645D,0).contract(.001,.001,.001),Material.water,fish);
   if(!old)oldFailures++;
   fish.setAir(1);fish.setFire(10);fish.fallDistance=7;
   check(fish.handleWaterMovement()&&fish.isInWater(),"submerged small fish detects water");
   check(!fish.isBurning()&&fish.fallDistance==0,"water extinguishes and clears fall distance");
   for(int tick=0;tick<360;tick++)fish.onEntityUpdate();
   check(fish.getAir()==300&&fish.getHealth()==3&&!fish.isDead,"360 underwater breathing ticks");
  }
  check(oldFailures>0,"legacy query reproduced missing water");
  for(int meta=0;meta<4;meta++) {
   world.cells.clear();EntityAquaticFish120 fish=ItemAquaticFishBucket120.create(world,meta);fish.setPosition(.5,10.8,.5);
   check(!fish.handleWaterMovement(),"dry fish remains dry");fish.setAir(1);
   for(int tick=0;tick<21;tick++)fish.onEntityUpdate();
   check(fish.getHealth()==1&&fish.getAir()==0,"dry fish still takes suffocation damage");
   world.setBlock(0,10,0,Blocks.water,0,2);world.setBlock(0,11,0,Blocks.water,0,2);fish.onEntityUpdate();
   check(fish.isInWater()&&fish.getAir()==300&&fish.getHealth()==1,"return to water restores air without healing");
  }
  world.cells.clear();world.setBlock(0,10,0,Blocks.lava,0,2);
  EntityAquaticFish120 fish=ItemAquaticFishBucket120.create(world,0);fish.setPosition(.5,10.2,.5);
  check(!fish.handleWaterMovement(),"lava is not breathable water");
  System.out.println("PASS fish109: "+checks+" checks, legacy false-dry reproduced "+oldFailures+" times; all fish/puff sizes, source/flowing water, heights, dry damage and re-entry");
 }
 static Field field(Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}
 static void check(boolean b,String text){if(!b)throw new AssertionError(text);checks++;}
}
