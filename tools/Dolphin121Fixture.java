package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import net.minecraft.entity.Entity;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.util.*;
import java.lang.reflect.*;
import java.util.*;
public final class Dolphin121Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid grid=BountifulMechanicsFixture120.initialize();
  Dolphin112Fixture.field(World.class,"worldInfo").set(grid,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));grid.provider.worldObj=grid;
  Dolphin112Fixture.field(World.class,"worldAccesses").set(grid,new ArrayList());Dolphin112Fixture.field(World.class,"theProfiler").set(grid,new net.minecraft.profiler.Profiler());
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);
  WaterWorld w=(WaterWorld)((sun.misc.Unsafe)uf.get(null)).allocateInstance(WaterWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(grid));}
  w.cells=new HashMap<String,Block>();w.loaded=true;Dolphin112Fixture.field(World.class,"playerEntities").set(w,new ArrayList());
  Method intent=EntityDolphin120.class.getDeclaredMethod("updateSwimmingIntent");intent.setAccessible(true);
  // Roof above the dolphin, with one opening to the east. Only that opening gives air.
  pool(w);for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)w.put(x,12,z,Blocks.stone);w.put(1,12,0,Blocks.air);
  EntityDolphin120 d=spawn(w);d.setAir(100);boolean movedSideways=false;
  for(int t=0;t<80&&d.getAir()<140;t++){
   d.onEntityUpdate();if(d.getAir()>=140)break;
   d.onLivingUpdate();if(d.posX>.8)movedSideways=true;
   check(w.getCollidingBoundingBoxes(d,d.boundingBox.contract(.001,.001,.001)).isEmpty(),"full living tick never crosses roof");
  }
  check(movedSideways,"route moves around roof toward opening");check(d.getAir()==4800,"real entity air cycle refills at opening");check(d.getHealth()==10,"reaches air before drowning");
  pool(w);for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)for(int y=12;y<22;y++)w.put(x,y,z,Blocks.water);
  d=spawn(w);d.setAir(130);
  for(int t=0;t<120&&d.getAir()<140;t++){d.onEntityUpdate();if(d.getAir()<140)d.onLivingUpdate();}
  check(d.getAir()==4800&&d.posY>21,"deep water uses successive upward fallback routes");
  // Seal all openings. There must be no blind upward push and no traversal through stone.
  pool(w);for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)w.put(x,12,z,Blocks.stone);
  d=spawn(w);d.setAir(100);d.motionY=.4;intent.invoke(d);check(d.motionY<.01,"sealed roof brakes old upward momentum");
  // An opening outside the modern +/-1 candidate window is not accepted.
  w.put(3,12,0,Blocks.air);intent.invoke(d);check(d.motionY<.01,"air search retains local candidate window");
  // A route must be invalidated when a block is placed in its next segment.
  pool(w);d=spawn(w);d.setAir(100);intent.invoke(d);check(d.motionY>0,"open water has upward route");
  for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)w.put(x,12,z,Blocks.stone);
  intent.invoke(d);check(d.motionY<.01,"new roof invalidates route before travel");
  pool(w);d=spawn(w);d.setAir(100);intent.invoke(d);
  for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)w.put(x,12,z,Blocks.lava);
  intent.invoke(d);check(d.motionY<.01,"liquid replacement invalidates route even without solid collision");
  // Do not probe blocks or request chunks outside the loaded world.
  pool(w);d=spawn(w);d.setAir(100);w.loaded=false;w.reads=0;intent.invoke(d);check(w.reads==0,"unloaded search makes no block reads");w.loaded=true;
  w.isRemote=true;d.motionX=.31;d.motionY=.27;d.motionZ=.19;intent.invoke(d);check(d.motionX==.31&&d.motionY==.27&&d.motionZ==.19,"client does not steer");w.isRemote=false;
  d.setDead();intent.invoke(d);check(d.motionY==.27,"dead dolphin does not steer");
  System.out.println("PASS dolphin121: "+checks+" navigation and full living tick checks");
 }
 static void pool(WaterWorld w){w.cells.clear();for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)for(int y=7;y<12;y++)w.put(x,y,z,Blocks.water);}
 static EntityDolphin120 spawn(WaterWorld w){EntityDolphin120 d=new EntityDolphin120(w);d.setPosition(.5,11,.5);d.handleWaterMovement();return d;}
 static void check(boolean v,String n){if(!v)throw new AssertionError(n);checks++;}
 static final class WaterWorld extends World {
  Map<String,Block> cells;boolean loaded;int reads;
  private WaterWorld(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
  void put(int x,int y,int z,Block b){cells.put(x+","+y+","+z,b);}
  public Block getBlock(int x,int y,int z){reads++;Block b=cells.get(x+","+y+","+z);return b==null?Blocks.air:b;}
  public int getBlockMetadata(int x,int y,int z){return 0;}
  public boolean blockExists(int x,int y,int z){return loaded;}
  public boolean checkChunksExist(int a,int b,int c,int d,int e,int f){return loaded;}
  public boolean canLightningStrikeAt(int x,int y,int z){return false;}
  public List getEntitiesWithinAABBExcludingEntity(Entity e,AxisAlignedBB b){return new ArrayList();}
  public List getCollidingBoundingBoxes(Entity e,AxisAlignedBB box){List out=new ArrayList();
   for(int x=MathHelper.floor_double(box.minX);x<=MathHelper.floor_double(box.maxX);x++)for(int y=MathHelper.floor_double(box.minY);y<=MathHelper.floor_double(box.maxY);y++)for(int z=MathHelper.floor_double(box.minZ);z<=MathHelper.floor_double(box.maxZ);z++)getBlock(x,y,z).addCollisionBoxesToList(this,x,y,z,box,out,e);
   return out;
  }
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected int func_152379_p(){return 0;}
  public Entity getEntityByID(int id){return null;}
 }
}
