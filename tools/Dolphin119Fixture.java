package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import java.lang.reflect.*;
import java.util.*;
public final class Dolphin119Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  Dolphin112Fixture.field(World.class,"worldInfo").set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  Dolphin112Fixture.field(net.minecraft.world.WorldProvider.class,"worldObj").set(world.provider,world);
  Dolphin112Fixture.field(World.class,"worldAccesses").set(world,new ArrayList());
  Dolphin112Fixture.field(World.class,"theProfiler").set(world,new net.minecraft.profiler.Profiler());
  for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=9;y<=14;y++)world.setBlock(x,y,z,Blocks.water,0,2);
  EntityDolphin120 d=new EntityDolphin120(world);d.setPosition(.5,11,.5);d.handleWaterMovement();
  check(d.isInWater(),"water detected");
  d.motionX=.2;d.motionY=.1;d.motionZ=-.3;d.moveEntityWithHeading(0,0);
  check(near(d.posX,.7)&&near(d.posY,11.1)&&near(d.posZ,.2),"actual displacement before drag");
  check(near(d.motionX,.18)&&near(d.motionY,.085)&&near(d.motionZ,-.27),"modern drag and idle sink");
  d.setAttackTarget(new EntityDolphin120(world));d.motionX=d.motionZ=0;d.motionY=.1;d.moveEntityWithHeading(0,0);
  check(near(d.motionY,.09),"target suppresses sink");
  d.setAIMoveSpeed(.12F);d.rotationYaw=0;d.motionX=d.motionY=d.motionZ=0;
  double z=d.posZ,speed=d.getAIMoveSpeed();d.moveEntityWithHeading(0,1);
  check(near(d.posZ-z,speed)&&near(d.motionZ,speed*.9),"input uses effective legacy AI speed");
  Method intent=EntityDolphin120.class.getDeclaredMethod("updateSwimmingIntent");intent.setAccessible(true);
  Field timer=EntityDolphin120.class.getDeclaredField("swimChange");timer.setAccessible(true);
  for(int air:new int[]{139,0,-19}){
   d.setAir(air);timer.setInt(d,0);d.motionX=.2;d.motionY=-.01;d.motionZ=-.3;
   intent.invoke(d);
   check(Double.isFinite(d.motionX)&&Double.isFinite(d.motionY)&&Double.isFinite(d.motionZ)&&d.motionX*d.motionX+d.motionY*d.motionY+d.motionZ*d.motionZ<=.022501D,"low air uses bounded navigation velocity "+air);
   check(timer.getInt(d)==0,"low air suspends wandering "+air);
  }
  d.setAir(140);intent.invoke(d);check(timer.getInt(d)>=15,"wandering resumes at breathing threshold");
  System.out.println("PASS dolphin119: "+checks+" checks");
 }
 static boolean near(double a,double b){return Math.abs(a-b)<1E-7;}
 static void check(boolean v,String n){if(!v)throw new AssertionError(n);checks++;}
}
