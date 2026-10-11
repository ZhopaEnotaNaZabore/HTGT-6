package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
public final class Raid138Fixture {
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
  Object r=ctor.newInstance(new ChunkCoordinates(7,64,-9),3,EnumDifficulty.HARD);
  Set<UUID> members=(Set<UUID>)field(c,"members").get(r);
  Set<UUID> heroes=(Set<UUID>)field(c,"heroes").get(r);
  UUID missing=UUID.randomUUID(),hero=UUID.randomUUID();members.add(missing);heroes.add(hero);
  wave.setInt(r,8);cooldown.setInt(r,0);failures.setInt(r,2);field(c,"ticksActive").setInt(r,120);
  check(!(Boolean)tick.invoke(r,w),"unloaded final member prevents false victory");
  check(members.contains(missing),"missing member retained");
  w.loaded=false;int elapsed=field(c,"ticksActive").getInt(r);
  check(!(Boolean)tick.invoke(r,w)&&field(c,"ticksActive").getInt(r)==elapsed,"unloaded village pauses elapsed time");
  w.loaded=true;
  RaidAndPatrolHandler.RaidData data=new RaidAndPatrolHandler.RaidData();
  List active=(List)field(data.getClass(),"active").get(data);active.add(r);
  net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();data.writeToNBT(tag);
  java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
  net.minecraft.nbt.CompressedStreamTools.writeCompressed(tag,bytes);
  tag=net.minecraft.nbt.CompressedStreamTools.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()));
  RaidAndPatrolHandler.RaidData restored=new RaidAndPatrolHandler.RaidData();restored.readFromNBT(tag);
  List loaded=(List)field(restored.getClass(),"active").get(restored);check(loaded.size()==1,"gzip save restores raid");
  Object copy=loaded.get(0);
  for(String n:new String[]{"omen","totalWaves","wave","cooldown","failedWaveAttempts","ticksActive"})
   check(field(c,n).getInt(copy)==field(c,n).getInt(r),"preserve "+n);
  ChunkCoordinates center=(ChunkCoordinates)field(c,"center").get(copy);
  check(center.posX==7&&center.posY==64&&center.posZ==-9,"center preserved");
  check(field(c,"members").get(copy).equals(members),"member UUIDs preserved");
  check(field(c,"heroes").get(copy).equals(heroes),"hero UUIDs preserved");
  check(!(Boolean)tick.invoke(copy,w),"restart cannot turn missing member into victory");
  restored.readFromNBT(tag);check(loaded.size()==1,"repeated read replaces rather than duplicates");
  Method living=c.getDeclaredMethod("livingRaiders",World.class);living.setAccessible(true);
  com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager foreign=
   new com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager(w);
  w.loadedEntityList.add(foreign);members.clear();
  check((Integer)living.invoke(r,w)==0,"unrelated nearby raider cannot block raid");
  members.add(foreign.getUniqueID());check((Integer)living.invoke(r,w)==1,"exact member counted");
  w.loadedEntityList.clear();check((Integer)living.invoke(r,w)==1,"entity unload preserves member");
  w.loadedEntityList.add(foreign);check((Integer)living.invoke(r,w)==1,"entity reload matches UUID");
  foreign.setHealth(0);check((Integer)living.invoke(r,w)==1,"zero health alone retains member");
  foreign.setDead();check((Integer)living.invoke(r,w)==0,"confirmed removal after death removes member");
  members.add(missing);field(c,"ticksActive").setInt(r,47980);
  check((Boolean)tick.invoke(r,w),"unresolved missing member times out without reward");
  net.minecraft.world.storage.MapStorage storage=new net.minecraft.world.storage.MapStorage(null);
  field(World.class,"perWorldStorage").set(w,storage);storage.setData("HTGT6Raids120",data);
  Swimming115Fixture.Player killer=new Swimming115Fixture.Player(w);
  members.clear();members.add(foreign.getUniqueID());foreign.setCaptain(true);
  RaidAndPatrolHandler handler=new RaidAndPatrolHandler();
  net.minecraftforge.event.entity.living.LivingDeathEvent death=new net.minecraftforge.event.entity.living.LivingDeathEvent(
    foreign,net.minecraft.util.DamageSource.causePlayerDamage(killer)){ public boolean isCancelable(){return true;} };
  foreign.isDead=false;field(c,"ticksActive").setInt(r,120);
  death.setCanceled(true);handler.death(death);check(members.contains(foreign.getUniqueID()),"cancelled death keeps member");
  check(!(Boolean)tick.invoke(r,w)&&members.contains(foreign.getUniqueID()),"tick after cancelled zero-health death keeps pending member");
  death.setCanceled(false);handler.death(death);
  check(!members.contains(foreign.getUniqueID()),"confirmed death retires exact UUID");
  check(heroes.contains(killer.getUniqueID()),"killer recorded as hero");
  check(killer.getEntityData().getInteger("HTGT6VillagePillageBadOmen")==0,"raid captain grants no new omen");
  check(data.isDirty(),"death persists changes");
  Swimming115Fixture.Player bystander=new Swimming115Fixture.Player(w);
  w.playerEntities=new ArrayList();w.playerEntities.add(killer);w.playerEntities.add(bystander);
  Method reward=c.getDeclaredMethod("rewardHeroes",World.class);reward.setAccessible(true);
  int attemptsBefore=w.attempts;reward.invoke(r,w);
  check(killer.getEntityData().getLong("HTGT6VillagePillageHeroUntil")==w.getTotalWorldTime()+48000,"confirmed hero rewarded");
  check(!bystander.getEntityData().hasKey("HTGT6VillagePillageHeroUntil"),"bystander receives no hero reward");
  check(w.attempts==attemptsBefore,"victory does not invent emerald item drops");
  System.out.println("PASS raid138: "+checks);
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
