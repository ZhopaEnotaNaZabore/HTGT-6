package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149;
import net.minecraft.world.*;import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;import java.lang.reflect.*;
public final class Campfire165Fixture {
 static int checks;
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe u=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  TestWorld w=(TestWorld)u.allocateInstance(TestWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  w.provider.worldObj=w;BlockCampfire149 fire=new BlockCampfire149();w.block=fire;
  EntityArrow arrow=new EntityArrow(w);arrow.setFire(10);arrow.shootingEntity=new EntityZombie(w);
  w.getGameRules().setOrCreateGameRule("mobGriefing","false");
  w.meta=3;fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==3&&w.lights==0,"mob cannot ignite when griefing disabled");
  w.getGameRules().setOrCreateGameRule("mobGriefing","true");fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==7&&w.lights==1,"allowed mob ignites and preserves facing");
  w.getGameRules().setOrCreateGameRule("mobGriefing","false");arrow.shootingEntity=null;w.meta=0;
  fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==4,"ownerless dispenser arrow allowed");
  arrow.shootingEntity=new Swimming115Fixture.Player(w);w.meta=1;w.denyPlayer=true;
  fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==1,"protected player position rejected");
  w.denyPlayer=false;fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==5,"allowed player independent of mobGriefing");
  w.meta=10;fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==10,"waterlogged remains unlit");
  w.meta=2;w.rejectWrites=true;int lights=w.lights;fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==2&&w.lights==lights,"rejected write does not relight");
  w.rejectWrites=false;w.isRemote=true;fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==2,"client no mutation");
  w.isRemote=false;arrow.extinguish();fire.onEntityCollidedWithBlock(w,0,10,0,arrow);check(w.meta==2,"cold arrow no ignition");
  System.out.println("PASS campfire165: "+checks);
 }
 public static class TestWorld extends Campfire149Fixture.TestWorld{
  boolean denyPlayer;
  protected TestWorld(){super();}
  @Override public boolean canMineBlock(EntityPlayer player,int x,int y,int z){return !denyPlayer;}
 }
}
