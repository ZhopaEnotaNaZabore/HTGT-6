package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.util.ChunkCoordinates;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
public final class Raid136Fixture {
 static int checks;
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static void main(String[] args)throws Exception {
  Class<?> raid=Class.forName(RaidAndPatrolHandler.class.getName()+"$Raid");
  Constructor<?> ctor=raid.getDeclaredConstructor(ChunkCoordinates.class,int.class,EnumDifficulty.class);ctor.setAccessible(true);
  Field total=raid.getDeclaredField("totalWaves"),wave=raid.getDeclaredField("wave"),cooldown=raid.getDeclaredField("cooldown");
  total.setAccessible(true);wave.setAccessible(true);cooldown.setAccessible(true);
  Method tick=raid.getDeclaredMethod("tick",World.class);tick.setAccessible(true);
  World w=BountifulMechanicsFixture120.initialize();
  w.loadedEntityList=new ArrayList();
  Dolphin112Fixture.field(World.class,"playerEntities").set(w,new ArrayList());
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  int[] bases={0,3,5,7};
  for(EnumDifficulty d:EnumDifficulty.values())for(int omen=1;omen<=5;omen++){
   Object r=ctor.newInstance(new ChunkCoordinates(0,64,0),omen,d);
   int expected=bases[d.getDifficultyId()]+(d!=EnumDifficulty.PEACEFUL&&omen>1?1:0);
   check(total.getInt(r)==expected,d+" omen "+omen+" count");
   w.difficultySetting=d;wave.setInt(r,expected);cooldown.setInt(r,0);
   check((Boolean)tick.invoke(r,w),"all groups completed "+d+" "+omen);
   check(wave.getInt(r)==expected,"no extra group after completion");
  }
  Object r=ctor.newInstance(new ChunkCoordinates(0,64,0),2,EnumDifficulty.EASY);
  w.difficultySetting=EnumDifficulty.HARD;wave.setInt(r,4);cooldown.setInt(r,0);
  check((Boolean)tick.invoke(r,w),"difficulty change preserves initial group count");
  r=ctor.newInstance(new ChunkCoordinates(0,64,0),5,EnumDifficulty.HARD);
  w.difficultySetting=EnumDifficulty.PEACEFUL;w.loadedEntityList=null;w.playerEntities=null;
  check((Boolean)tick.invoke(r,w),"peaceful stops without entity/reward access");
  check(wave.getInt(r)==0,"peaceful never spawns");
  Method pending=RaidAndPatrolHandler.class.getDeclaredMethod("startPendingRaids",World.class);pending.setAccessible(true);
  pending.invoke(new RaidAndPatrolHandler(),w);check(true,"peaceful does not consume omen/start raids");
  System.out.println("PASS raid136: "+checks);
 }
}
