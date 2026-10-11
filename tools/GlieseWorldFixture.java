import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import com.mod.htgt6.sulimited.world.*;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.*;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.blocks.*;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.*;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.*;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;

public final class GlieseWorldFixture {
 public static final class NestedPopulation {
  final GlieseChunkProvider provider;
  int pre, post, depth;
  NestedPopulation(GlieseChunkProvider provider) { this.provider=provider; }
  @cpw.mods.fml.common.eventhandler.SubscribeEvent
  public void before(micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Pre event) { pre++; }
  @cpw.mods.fml.common.eventhandler.SubscribeEvent
  public void after(micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Post event) {
   post++;
   if(depth!=0) return;
   long seed=event.worldObj.getSeed(); Random initial=new Random(seed);
   long a=initial.nextLong()/2*2+1, b=initial.nextLong()/2*2+1;
   Random expected=new Random(((event.chunkX>>4)*a+(event.chunkZ>>4)*b)^seed);
   require(event.rand.nextLong()==expected.nextLong(),"population seed changed");
   depth++;
   try { provider.decoratePlanet(event.worldObj,event.rand,event.chunkX+16,event.chunkZ-16); }
   finally { depth--; }
   require(event.rand.nextLong()==expected.nextLong(),"nested population changes parent random state");
  }
 }
 static void checkNestedPopulation(GlieseChunkProvider provider, World world) throws Exception {
  final NestedPopulation listener=new NestedPopulation(provider);
  java.lang.reflect.Field busField=cpw.mods.fml.common.eventhandler.EventBus.class.getDeclaredField("busID"); busField.setAccessible(true);
  int busId=busField.getInt(net.minecraftforge.common.MinecraftForge.EVENT_BUS);
  cpw.mods.fml.common.eventhandler.IEventListener hook=new cpw.mods.fml.common.eventhandler.IEventListener() {
   public void invoke(cpw.mods.fml.common.eventhandler.Event event) {
    if(event instanceof micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Pre) listener.before((micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Pre)event);
    if(event instanceof micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Post) listener.after((micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Post)event);
   }
  };
  java.util.Set<cpw.mods.fml.common.eventhandler.ListenerList> lists=new java.util.HashSet<>();
  lists.add(new micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Pre(world,new Random(),0,0).getListenerList());
  lists.add(new micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Post(world,new Random(),0,0).getListenerList());
  for(cpw.mods.fml.common.eventhandler.ListenerList list:lists) list.register(busId,cpw.mods.fml.common.eventhandler.EventPriority.NORMAL,hook);
  try { provider.decoratePlanet(world,new Random(1),-32,48); }
  finally { for(cpw.mods.fml.common.eventhandler.ListenerList list:lists) list.unregister(busId,hook); }
  require(listener.pre==2 && listener.post==2,"nested compatibility event missing");
 }
 static void require(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void main(String[] args) throws Exception {
  net.minecraft.launchwrapper.Launch.classLoader=new net.minecraft.launchwrapper.LaunchClassLoader(((java.net.URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs());
  World world=BountifulMechanicsFixture120.initialize();
  net.minecraft.nbt.NBTTagCompound info=new net.minecraft.nbt.NBTTagCompound(); info.setLong("RandomSeed",0L);
  java.lang.reflect.Field wf=World.class.getDeclaredField("worldInfo"); wf.setAccessible(true); wf.set(world,new net.minecraft.world.storage.WorldInfo(info));
  java.lang.reflect.Field wp=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj"); wp.setAccessible(true); wp.set(world.provider,world);
  world.provider.worldChunkMgr=new net.minecraft.world.biome.WorldChunkManagerHell(net.minecraft.world.biome.BiomeGenBase.ocean,0.5F);
  java.lang.reflect.Field wa=World.class.getDeclaredField("worldAccesses"); wa.setAccessible(true); wa.set(world,new java.util.ArrayList());
  B_database.addStat(); blocksGliese792C.addStat(); SolSysGeology.saltWater=Blocks.water;
  int blockId=3500;
  for(Class<?> content:new Class<?>[]{B_database.class,blocksGliese792C.class}) for(java.lang.reflect.Field field:content.getFields()) {
   if(field.getType()!=Block.class) continue;
   Block block=(Block)field.get(null); if(block==null) continue;
   while(Block.blockRegistry.getObjectById(blockId)!=null && Block.blockRegistry.getObjectById(blockId)!=Blocks.air) blockId++;
   Block.blockRegistry.addObject(blockId++,"fixture:"+field.getName(),block);
  }
  BiomeGenSpaceUltimate.init(130); BiomeGenSpaceUltimate.initGlieseC(131);
  GlieseCSubBiomeGenerator sampler=new GlieseCSubBiomeGenerator();
  EnumMap<GlieseCSubBiomeGenerator.Type,int[]> sites=new EnumMap<>(GlieseCSubBiomeGenerator.Type.class);
  int[] volcanicSite=null;
  for(long seed:new long[]{0,12345,-91357}) {
   int[] counts=new int[GlieseCSubBiomeGenerator.Type.values().length];
   int lava=0,highLava=0,dry=0,ash=0,volcano=0,land=0,total=0,maxB=0,maxC=0;
   for(int x=-4096;x<=4096;x+=16) for(int z=-4096;z<=4096;z+=16) {
    GlieseTerrain.VolcanicSample b=GlieseTerrain.volcanic(seed,x,z);
    GlieseCSubBiomeGenerator.Sample c=sampler.sample(seed,x,z,160);
    counts[c.type.ordinal()]++; total++; if(c.height>160) land++;
    if(seed==world.getSeed() && !sites.containsKey(c.type)) sites.put(c.type,new int[]{x>>4,z>>4});
    if(b.lavaLevel>b.height) { lava++; if(b.lavaLevel>100) { highLava++; if(seed==world.getSeed()) volcanicSite=new int[]{x>>4,z>>4}; } }
    if(b.dryChannel) dry++; if(b.wasteland) ash++; if(b.volcano) volcano++;
    require(c.height>=14 && c.height<=188 && b.height>=12 && b.height<=230,"height bounds");
    for(int axis=0;axis<2;axis++) {
     int xx=x+(axis==0?1:0),zz=z+(axis==1?1:0);
     maxB=Math.max(maxB,Math.abs(b.height-GlieseTerrain.volcanic(seed,xx,zz).height));
     maxC=Math.max(maxC,Math.abs(c.height-sampler.sample(seed,xx,zz,160).height));
    }
   }
   for(GlieseCSubBiomeGenerator.Type t:GlieseCSubBiomeGenerator.Type.values()) require(counts[t.ordinal()]>0,"missing C region "+seed+" "+t);
   require(lava>0 && highLava>0 && dry>0 && ash>0 && volcano>0,"missing B terrain");
   require(land>total*0.1 && land<total*0.45,"ocean/land ratio");
   require(maxB<=9 && maxC<=5,"height discontinuity: "+maxB+","+maxC);
   System.out.println("seed="+seed+" C habitats="+Arrays.toString(counts)+" land="+(100*land/total)+"% B lava="+lava+" high calderas="+highLava+" slopes="+maxB+","+maxC);
  }
  ChunkProviderGlieseB b=new ChunkProviderGlieseB(world,world.getSeed(),true);
  ChunkProviderGlieseC c=new ChunkProviderGlieseC(world,world.getSeed(),true);
  checkNestedPopulation(b,world); checkNestedPopulation(c,world);
  System.out.println("Nested population checks passed for Gliese B/C: both Pre/Post events and independent RNG.");
  Class<?> asteroid=Class.forName("de.katzenpapst.amunra.world.asteroidWorld.AmunRaAsteroidWorldProvider$AsteroidSaveData");
  net.minecraft.world.WorldSavedData saved=(net.minecraft.world.WorldSavedData)asteroid.getConstructor(String.class).newInstance("fixture_asteroids");
  net.minecraft.nbt.NBTTagCompound asteroidNbt=new net.minecraft.nbt.NBTTagCompound();
  net.minecraft.nbt.NBTTagCompound contents=new net.minecraft.nbt.NBTTagCompound(); contents.setInteger("fixture",123);
  asteroidNbt.setTag("asteroids",contents); saved.readFromNBT(asteroidNbt);
  net.minecraft.nbt.NBTTagCompound restored=new net.minecraft.nbt.NBTTagCompound(); saved.writeToNBT(restored);
  require(restored.getCompoundTag("asteroids").getInteger("fixture")==123,"asteroid data lost on reload");
  System.out.println("AmunRa saved data reflection constructor and NBT round-trip passed.");
  int coral=0,deepCoral=0,algae=0,vents=0;
  for(int[] site:sites.values()) {
   Chunk first=c.provideChunk(site[0],site[1]);
   c.provideChunk(site[0]+1,site[1]);
   Chunk again=c.provideChunk(site[0],site[1]);
   for(int x=0;x<16;x++) for(int z=0;z<16;z++) for(int y=0;y<256;y++) {
    Block block=first.getBlock(x,y,z);
    require(block==again.getBlock(x,y,z) && first.getBlockMetadata(x,y,z)==again.getBlockMetadata(x,y,z),"generation order dependence");
    if(y==0) require(block==Blocks.bedrock,"bedrock");
    if(y>188) require(block==Blocks.air,"floating terrain");
    if(block==Blocks.water) require(y<=160,"ocean overflow");
    if(block==blocksGliese792C.GlieseCCoral) coral++;
    if(block==blocksGliese792C.deepCoral) deepCoral++;
    if(block==blocksGliese792C.GlieseCAlgae) algae++;
    if(block==blocksGliese792C.ventRock) vents++;
   }
  }
  require(coral>0 && deepCoral>0 && algae>0 && vents>0,"missing decorators");
  require(volcanicSite!=null,"caldera site");
  Chunk volcanic=b.provideChunk(volcanicSite[0],volcanicSite[1]); boolean elevatedLava=false;
  for(int x=0;x<16;x++) for(int z=0;z<16;z++) for(int y=100;y<180;y++) if(volcanic.getBlock(x,y,z)==Blocks.lava) elevatedLava=true;
  require(elevatedLava,"caldera lava lost in chunk pipeline");
  BlockGlieseCPlant plant=(BlockGlieseCPlant)blocksGliese792C.GlieseCAlgae;
  world.setBlock(0,40,0,Blocks.stone,0,2); world.setBlock(1,41,0,Blocks.water,0,2);
  world.setBlock(0,41,0,plant,0,2); world.setBlock(0,42,0,plant,0,2); world.setBlock(1,42,0,Blocks.water,0,2);
  require(plant.canBlockStay(world,0,42,0),"stacked kelp support");
  require(plant.getMaterial()==net.minecraft.block.material.Material.water,"underwater plant air pocket");
  world.setBlock(0,41,0,Blocks.air,0,2); require(!plant.canBlockStay(world,0,42,0),"floating kelp survives");
  new com.mod.htgt6.sulimited.client.SkyProviderGliese(false);
  new com.mod.htgt6.sulimited.client.SkyProviderGliese(true);
  System.out.println("Gliese checks passed: continuous fields, all regions, deterministic chunks, decoration, caldera lava, plant support and GL-free sky constructors.");
 }
}
