package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;
public final class Fish110Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  Field sounds=World.class.getDeclaredField("worldAccesses");sounds.setAccessible(true);sounds.set(world,new ArrayList());
  Method motion=EntityAquaticFish120.class.getDeclaredMethod("updateFishMotion");motion.setAccessible(true);
  Fish fish=new Fish(world);fish.wet=true;fish.motionX=.2;fish.motionY=.1;fish.motionZ=-.3;
  fish.moveEntityWithHeading(0,0);
  check(near(fish.dx,.2)&&near(fish.dy,.1)&&near(fish.dz,-.3),"movement uses pre-drag velocity");
  check(near(fish.motionX,.18)&&near(fish.motionY,.085)&&near(fish.motionZ,-.27),"single modern drag and idle sinking");
  fish.setAttackTarget(new Fish(world));fish.motionY=.1;fish.moveEntityWithHeading(0,0);
  check(near(fish.motionY,.09),"target suppresses idle sinking");
  fish.motionX=fish.motionY=fish.motionZ=0;fish.rotationYaw=0;fish.moveEntityWithHeading(0,1);
  check(near(fish.dz,.01)&&near(fish.motionZ,.009),"swim input acceleration");
  fish.wet=false;fish.onGround=true;fish.isCollidedVertically=true;fish.motionX=fish.motionY=fish.motionZ=0;
  motion.invoke(fish);check(near(fish.motionY,.4)&&Math.abs(fish.motionX)<=.05&&Math.abs(fish.motionZ)<=.05,"dry flop impulse");
  check(!fish.onGround&&fish.isAirBorne,"flop releases ground");
  double vertical=fish.motionY;motion.invoke(fish);check(fish.motionY==vertical,"no repeated impulse in air or extra gravity");
  fish.onGround=true;fish.isCollidedVertically=false;motion.invoke(fish);check(fish.motionY==vertical,"flop requires vertical collision");
  world.isRemote=true;fish.wet=true;fish.motionX=fish.motionY=fish.motionZ=0;motion.invoke(fish);
  check(fish.motionX==0&&fish.motionY==0&&fish.motionZ==0,"client does not invent random swim motion");
  fish.wet=false;fish.onGround=fish.isCollidedVertically=true;motion.invoke(fish);
  check(fish.motionY==0&&fish.onGround,"client does not independently flop");
  world.isRemote=false;fish.setDead();motion.invoke(fish);check(fish.motionY==0,"dead fish does not flop");
  System.out.println("PASS fish110: "+checks+" checks for water travel, drag, target/input, dry flop and server authority");
 }
 static boolean near(double a,double b){return Math.abs(a-b)<1E-7;}
 static void check(boolean value,String msg){if(!value)throw new AssertionError(msg);checks++;}
 static final class Fish extends EntityAquaticFish120 {
  boolean wet;double dx,dy,dz;
  Fish(World world){super(world,.5F,.3F,3);}
  public int species(){return 0;}
  public boolean isInWater(){return wet;}
  public void moveEntity(double x,double y,double z){dx=x;dy=y;dz=z;}
 }
}
