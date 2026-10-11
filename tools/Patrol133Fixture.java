package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import net.minecraft.world.*;import net.minecraft.entity.*;import net.minecraft.block.Block;import net.minecraft.init.Blocks;import net.minecraft.util.*;import java.lang.reflect.*;import java.util.*;
public class Patrol133Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static void main(String[] args)throws Exception{
  World base=BountifulMechanicsFixture120.initialize();
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);
  TestWorld w=(TestWorld)u.allocateInstance(TestWorld.class);for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  Dolphin112Fixture.field(World.class,"theProfiler").set(w,new net.minecraft.profiler.Profiler());
  Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());
  Dolphin112Fixture.field(World.class,"playerEntities").set(w,new ArrayList());
  w.provider.worldObj=w;w.loaded=true;w.biome=net.minecraft.world.biome.BiomeGenBase.plains;w.floor=Blocks.stone;w.day=true;w.time=120000;w.difficultySetting=EnumDifficulty.NORMAL;
  Method attempt=RaidAndPatrolHandler.class.getDeclaredMethod("trySpawnPatrol",World.class);attempt.setAccessible(true);
  Method spawn=RaidAndPatrolHandler.class.getDeclaredMethod("spawnPatrolMember",World.class,int.class,int.class,boolean.class);spawn.setAccessible(true);
  // Invalid worlds must return before touching the random generator/player list.
  w.rand=null;RaidAndPatrolHandler h=new RaidAndPatrolHandler();
  w.difficultySetting=EnumDifficulty.PEACEFUL;attempt.invoke(h,w);check(w.reads==0,"peaceful gate");w.difficultySetting=EnumDifficulty.NORMAL;
  w.time=119999;attempt.invoke(h,w);check(w.reads==0,"before day five gate");w.time=120000;
  w.day=false;attempt.invoke(h,w);check(w.reads==0,"night gate");w.day=true;
  w.getGameRules().setOrCreateGameRule("doMobSpawning","false");attempt.invoke(h,w);check(w.reads==0,"gamerule gate");w.getGameRules().setOrCreateGameRule("doMobSpawning","true");
  w.isRemote=true;attempt.invoke(h,w);check(w.reads==0,"client gate");w.isRemote=false;
  w.rand=new Random(1);w.loaded=false;check(!(Boolean)spawn.invoke(null,w,0,0,true)&&w.reads==0,"no unloaded reads");w.loaded=true;
  w.floor=Blocks.water;check(!(Boolean)spawn.invoke(null,w,0,0,true),"water not solid support");w.floor=Blocks.bedrock;check(!(Boolean)spawn.invoke(null,w,0,0,true),"bedrock rejected");w.floor=Blocks.stone;
  w.light=9;check(!(Boolean)spawn.invoke(null,w,0,0,true),"block light 9 rejected");w.light=8;
  w.collides=true;check(!(Boolean)spawn.invoke(null,w,0,0,true),"blocked headroom rejected");w.collides=false;
  w.liquid=true;check(!(Boolean)spawn.invoke(null,w,0,0,true),"liquid body rejected");w.liquid=false;
  w.occupied=true;check(!(Boolean)spawn.invoke(null,w,0,0,true),"occupied position rejected");w.occupied=false;
  w.reject=true;check(!(Boolean)spawn.invoke(null,w,0,0,true)&&w.attempts==1,"spawn denial propagated");
  w.reject=false;check((Boolean)spawn.invoke(null,w,3,4,true),"daylight patrol succeeds");check(w.last.isCaptain()&&w.last.posY==14&&w.last.posX==3.5,"captain gets own column height");
  check((Boolean)spawn.invoke(null,w,7,8,false)&&!w.last.isCaptain()&&w.last.posY==18,"follower height recomputed");
  Dolphin112Fixture.field(World.class,"perWorldStorage").set(w,new net.minecraft.world.storage.MapStorage(null));w.villageCollectionObj=new net.minecraft.village.VillageCollection(w);
  Swimming115Fixture.Player player=new Swimming115Fixture.Player(w);player.setPosition(0,10,0);w.playerEntities.add(player);
  w.rand=new Random(){public int nextInt(int bound){return 0;}public boolean nextBoolean(){return false;}};
  w.reject=true;w.attempts=0;attempt.invoke(h,w);check(w.attempts==1,"denied captain aborts followers");
  w.reject=false;w.attempts=0;attempt.invoke(h,w);check(w.attempts==3,"accepted captain permits fixed-size group");
  w.biome=net.minecraft.world.biome.BiomeGenBase.mushroomIsland;w.attempts=0;attempt.invoke(h,w);check(w.attempts==0,"mushroom biome excluded");
  w.biome=net.minecraft.world.biome.BiomeGenBase.plains;w.loaded=false;w.reads=0;attempt.invoke(h,w);check(w.reads==0&&w.attempts==0,"whole patrol never probes unloaded chunks");
  System.out.println("PASS patrol133: "+checks);
 }
 static final class TestWorld extends World{
  boolean loaded,day,collides,liquid,occupied,reject;int reads,light,attempts;long time;Block floor;EntityPillager last;
  TestWorld(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
  net.minecraft.world.biome.BiomeGenBase biome=net.minecraft.world.biome.BiomeGenBase.plains;
  public net.minecraft.world.biome.BiomeGenBase getBiomeGenForCoords(int x,int z){return biome;}
  public long getWorldTime(){return time;}public boolean isDaytime(){return day;}
  public boolean checkChunksExist(int a,int b,int c,int d,int e,int f){return loaded;}
  public int getTopSolidOrLiquidBlock(int x,int z){return 10+z;}
  public Block getBlock(int x,int y,int z){reads++;return y==9+z?floor:Blocks.air;}
  public int getBlockMetadata(int x,int y,int z){return 0;}
  public int getSavedLightValue(EnumSkyBlock t,int x,int y,int z){return light;}
  public boolean checkNoEntityCollision(AxisAlignedBB b){return !occupied;}
  public List getCollidingBoundingBoxes(Entity e,AxisAlignedBB b){return collides?Arrays.asList(b):new ArrayList();}
  public boolean isAnyLiquid(AxisAlignedBB b){return liquid;}
  public boolean spawnEntityInWorld(Entity e){attempts++;last=(EntityPillager)e;return !reject;}
  public float func_147462_b(double x,double y,double z){return 0;}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected int func_152379_p(){return 0;}public Entity getEntityByID(int id){return null;}
 }
}

