package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120.Pufferfish;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import net.minecraft.entity.EnumCreatureType;
import java.lang.reflect.*;
import java.util.*;
public final class Dolphin112Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  field(World.class,"worldInfo").set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  field(net.minecraft.world.WorldProvider.class,"worldObj").set(world.provider,world);
  field(World.class,"worldAccesses").set(world,new ArrayList());field(World.class,"theProfiler").set(world,new net.minecraft.profiler.Profiler());
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=9;y<=13;y++)world.setBlock(x,y,z,Blocks.water,0,2);
  EntityDolphin120 dolphin=new EntityDolphin120(world);dolphin.setPosition(.5,10.5,.5);dolphin.onEntityUpdate();
  if(args.length>0){System.out.println("BASELINE: air after one submerged tick="+dolphin.getAir()+", maxHealth="+dolphin.getMaxHealth());return;}
  check(dolphin.getAir()==4799,"submerged air consumes one tick instead of reset300");
  check(dolphin.getMaxHealth()==10&&dolphin.getHealth()==10,"ten maximum health");
  for(int tick=1;tick<4800;tick++)dolphin.onEntityUpdate();
  check(dolphin.getAir()==0&&dolphin.getHealth()==10,"full240second reserve");
  for(int tick=0;tick<20;tick++)dolphin.onEntityUpdate();
  check(dolphin.getHealth()==8&&dolphin.getAir()==0,"submerged drowning after air depletion");
  world.cells.clear();dolphin.onEntityUpdate();
  check(dolphin.getAir()==4800&&dolphin.getHealth()==8,"surface refills reserve without healing");
  for(int tick=0;tick<400;tick++)dolphin.onEntityUpdate();
  check(dolphin.getAir()==4800&&dolphin.getHealth()==8,"no fish-style dry suffocation");
  check(dolphin.isCreatureType(EnumCreatureType.waterCreature,true)&&!dolphin.isCreatureType(EnumCreatureType.creature,true),"water spawn counting retained");
  for(double y:new double[]{10.05,10.5,10.8}){
   world.cells.clear();world.setBlock(0,10,0,Blocks.water,0,2);world.setBlock(0,11,0,Blocks.water,0,2);dolphin.setPosition(.5,y,.5);
   check(dolphin.handleWaterMovement(),"small dolphin bounds detect water");
  }
  world.cells.clear();dolphin.setPosition(.5,10.8,.5);world.setBlock(0,10,0,Blocks.water,0,2);dolphin.setAir(12);dolphin.onEntityUpdate();
  check(dolphin.isInWater()&&dolphin.getAir()==4800,"body wet with breathing opening above surface");
  Pufferfish puffer=new Pufferfish(world);puffer.setPosition(.5,10.8,.5);world.loadedEntityList.add(dolphin);
  Method tick=Pufferfish.class.getDeclaredMethod("tickPuff");tick.setAccessible(true);tick.invoke(puffer);
  check(puffer.getPuffState()==0,"water-classified dolphin does not scare pufferfish");
  System.out.println("PASS dolphin112: "+checks+" checks, 4800 air, depletion/surface, health, water bounds, spawn type and puffer compatibility");
 }
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void check(boolean v,String n){if(!v)throw new AssertionError(n);checks++;}
}
