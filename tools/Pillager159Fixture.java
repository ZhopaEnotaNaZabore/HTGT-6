package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import net.minecraft.entity.*;import net.minecraft.item.*;import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;
public final class Pillager159Fixture {
 static int checks;
 public static void main(String[]args)throws Exception{
  Crossbow139Fixture.main(args);
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  java.lang.reflect.Field profiler=World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(w,new net.minecraft.profiler.Profiler());
  java.lang.reflect.Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));w.provider.worldObj=w;
  EntityPillager mob=new EntityPillager(w);mob.setPosition(0,10,0);
  Swimming115Fixture.Player target=new Swimming115Fixture.Player(w);target.setPosition(20,10,0);mob.setAttackTarget(target);
  Recorder nav=new Recorder(mob,w);java.lang.reflect.Field navigator=EntityLiving.class.getDeclaredField("navigator");navigator.setAccessible(true);navigator.set(mob,nav);
  ItemStack bow=new ItemStack(new ItemCrossbow());mob.setCurrentItemOrArmor(0,bow);
  PillagerCrossbowGoal139 goal=new PillagerCrossbowGoal139(mob);
  goal.updateTask();check(nav.calls==1,"initial path");
  for(int i=0;i<19;i++)goal.updateTask();check(nav.calls==1,"no per-tick path rebuild");
  for(int i=0;i<22;i++)goal.updateTask();check(nav.calls>=2&&nav.calls<=3,"bounded 20..40 retries");
  mob.setCrossbowChargeTicks(12);mob.setCurrentItemOrArmor(0,new ItemStack(bow.getItem()));goal.updateTask();check(mob.getCrossbowChargeTicks()==0,"replacement cancels progress");
  java.lang.reflect.Field delay=PillagerCrossbowGoal139.class.getDeclaredField("delay");delay.setAccessible(true);delay.setInt(goal,10);
  int calls=nav.calls;goal.updateTask();check(nav.calls==calls&&nav.clears>0,"waiting phase stops pursuit");
  delay.setInt(goal,0);java.lang.reflect.Field path=PillagerCrossbowGoal139.class.getDeclaredField("pathDelay");path.setAccessible(true);path.setInt(goal,0);
  mob.setCrossbowChargeTicks(3);goal.updateTask();check(nav.speed==.5D,"charging approach speed");
  mob.setAttackTarget(null);goal.updateTask();check(mob.getCrossbowChargeTicks()==0,"missing target cancels pose");
  check(path.getInt(goal)==0&&delay.getInt(goal)==0,"reset clears scheduling");
  float[] values={-1,0,.5F,.999F,1,2,Float.NaN};int[] widths={0,0,91,181,182,182,0};
  for(int i=0;i<values.length;i++)check(com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RaidHud150.progressWidth(values[i])==widths[i],"HUD width "+i);
  ItemStack banner=EntityPillager.createCaptainBanner();net.minecraft.nbt.NBTTagList layers=banner.getTagCompound().getCompoundTag("BlockEntityTag").getTagList("Patterns",10);
  String[] codes={"mr","bs","cs","bo","ms","hh","mc","bo"};int[] colors={6,7,8,7,0,7,7,0};
  check(banner.getItemDamage()==15&&layers.tagCount()==8,"white captain banner with eight layers");
  for(int i=0;i<8;i++)check(codes[i].equals(layers.getCompoundTagAt(i).getString("Pattern"))&&colors[i]==layers.getCompoundTagAt(i).getInteger("Color"),"captain layer "+i);
  check(com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer.visiblePatterns(layers).tagCount()==8,"renderer retains all captain layers");
  com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120 data=new com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120();
  check(!data.hasGuard(-2,3,0),"new guard unclaimed");data.recordGuard(-2,3,0);
  check(data.hasGuard(-2,3,0)&&!data.hasGuard(-2,3,1),"partial spawn tracked individually");
  NBTTagCompound saved=new NBTTagCompound();data.writeToNBT(saved);data=new com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120();data.readFromNBT(saved);
  check(data.hasGuard(-2,3,0)&&!data.hasGuard(-2,3,1),"partial spawn survives save");
  data.claimGuards(4,5);for(int i=0;i<4;i++)check(data.hasGuard(4,5,i),"legacy claim prevents duplicate "+i);
  w.loaded=false;check(!com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage.PillagerOutpostGenerator.safeGuardPosition(w,mob,0,10,0),"unloaded spawn rejected");
  System.out.println("PASS pillager159: "+checks);
 }
 static class Recorder extends PathNavigate {
  int calls,clears;double speed;
  Recorder(EntityLiving mob,World world){super(mob,world);}
  @Override public boolean tryMoveToEntityLiving(Entity target,double value){calls++;speed=value;return true;}
  @Override public void clearPathEntity(){clears++;super.clearPathEntity();}
 }
 static void check(boolean value,String name){if(!value)throw new AssertionError(name);checks++;}
}
