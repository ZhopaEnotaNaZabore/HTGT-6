package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
public final class Raid137Fixture {
 static int checks;
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 public static void main(String[] args)throws Exception {
  World base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe u=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  TestWorld w=(TestWorld)u.allocateInstance(TestWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  Dolphin112Fixture.field(World.class,"theProfiler").set(w,new net.minecraft.profiler.Profiler());
  Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());
  Dolphin112Fixture.field(World.class,"playerEntities").set(w,null); // Any accidental reward must fail.
  w.loadedEntityList=new ArrayList();w.provider.worldObj=w;w.rand=new Random(3);
  w.difficultySetting=EnumDifficulty.EASY;w.loaded=true;w.y=64;
  Class<?> c=Class.forName(RaidAndPatrolHandler.class.getName()+"$Raid");
  Constructor<?> ctor=c.getDeclaredConstructor(ChunkCoordinates.class,int.class,EnumDifficulty.class);ctor.setAccessible(true);
  Field wave=field(c,"wave"),cooldown=field(c,"cooldown"),failures=field(c,"failedWaveAttempts");
  Method tick=c.getDeclaredMethod("tick",World.class),spawn=c.getDeclaredMethod("spawnWave",World.class,int.class);tick.setAccessible(true);spawn.setAccessible(true);
  Object r=ctor.newInstance(new ChunkCoordinates(0,64,0),1,EnumDifficulty.EASY);
  wave.setInt(r,2);cooldown.setInt(r,0);w.reject=true;
  check(!(Boolean)tick.invoke(r,w),"first rejected final wave retries");
  check(wave.getInt(r)==2&&failures.getInt(r)==1,"rejected wave is not completed");
  int attempts=w.attempts;
  for(int i=0;i<8;i++)check(!(Boolean)tick.invoke(r,w),"retry cooldown "+i);
  check(w.attempts==attempts,"cooldown does not spam spawns");
  check(!(Boolean)tick.invoke(r,w)&&failures.getInt(r)==2,"second failed attempt remains pending");
  cooldown.setInt(r,0);
  check((Boolean)tick.invoke(r,w),"third failure stops raid without reward");
  check(wave.getInt(r)==2,"no free final wave");
  r=ctor.newInstance(new ChunkCoordinates(0,64,0),1,EnumDifficulty.EASY);
  failures.setInt(r,2);cooldown.setInt(r,0);w.reject=false;w.acceptLimit=1;w.accepted=0;
  check(!(Boolean)tick.invoke(r,w),"one accepted raider starts wave");
  check(wave.getInt(r)==1&&failures.getInt(r)==0,"successful spawn commits wave and resets failures");
  r=ctor.newInstance(new ChunkCoordinates(0,64,0),1,EnumDifficulty.HARD);
  w.acceptLimit=0;w.accepted=0;w.attempts=0;
  check((Integer)spawn.invoke(r,w,4)==11,"all four member types counted");
  check(w.attempts==11&&w.accepted==11,"all accepted members reported");
  w.reject=true;w.attempts=0;check((Integer)spawn.invoke(r,w,4)==0&&w.attempts==11,"all four types can reject");
  w.loaded=false;w.heightReads=0;w.attempts=0;
  check((Integer)spawn.invoke(r,w,1)==0&&w.heightReads==0&&w.attempts==0,"unloaded area never read or spawned");
  w.loaded=true;w.y=0;check((Integer)spawn.invoke(r,w,1)==0&&w.attempts==0,"invalid lower height rejected");
  w.y=255;check((Integer)spawn.invoke(r,w,1)==0&&w.attempts==0,"invalid upper height rejected");
  System.out.println("PASS raid137: "+checks);
 }
 static final class TestWorld extends World {
  boolean loaded,reject;int y,attempts,accepted,acceptLimit,heightReads;
  TestWorld(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
  public boolean checkChunksExist(int a,int b,int c,int d,int e,int f){return loaded;}
  public int getTopSolidOrLiquidBlock(int x,int z){heightReads++;return y;}
  public boolean spawnEntityInWorld(Entity e){attempts++;boolean ok=!reject&&(acceptLimit==0||accepted<acceptLimit);if(ok)accepted++;return ok;}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected int func_152379_p(){return 0;}
  public Entity getEntityByID(int id){return null;}
 }
}
