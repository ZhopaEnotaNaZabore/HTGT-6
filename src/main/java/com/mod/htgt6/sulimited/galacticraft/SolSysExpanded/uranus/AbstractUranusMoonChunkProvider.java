package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus;

import com.mod.htgt6.sulimited.world.GT6PlanetOreGeneration;
import com.mod.htgt6.sulimited.worldgen.constructor.SUChunkContext;
import com.mod.htgt6.sulimited.worldgen.constructor.SUWorldConstructorPipeline;
import java.util.Collections;
import java.util.List;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysChunkProvider;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

abstract class AbstractUranusMoonChunkProvider extends SolSysChunkProvider {
    protected final World world;
    private final BiomeGenBase biome;
    private final Block surface,subsurface,core;
    private SUWorldConstructorPipeline constructors;
    AbstractUranusMoonChunkProvider(World world,long seed,boolean features,BiomeGenBase biome,Block surface,Block subsurface,Block core){super(world,seed,features);this.world=world;this.biome=biome;this.surface=surface;this.subsurface=subsurface;this.core=core;}
    protected abstract Terrain sample(long seed,int x,int z);
    protected void configureConstructors(SUWorldConstructorPipeline pipeline){}
    private SUWorldConstructorPipeline constructors(){if(constructors==null){constructors=new SUWorldConstructorPipeline();configureConstructors(constructors);}return constructors;}
    protected BiomeGenBase.SpawnListEntry[] getMonsters(){return new BiomeGenBase.SpawnListEntry[0];} protected BiomeGenBase.SpawnListEntry[] getCreatures(){return new BiomeGenBase.SpawnListEntry[0];}
    protected List<MapGenBaseMeta> getWorldGenerators(){return Collections.emptyList();} protected BiomeGenBase[] getBiomesForGeneration(){return new BiomeGenBase[]{biome};} protected int getSeaLevel(){return 0;}
    protected BlockMetaPair getGrassBlock(){return new BlockMetaPair(surface,(byte)0);} protected BlockMetaPair getDirtBlock(){return new BlockMetaPair(subsurface,(byte)0);} protected BlockMetaPair getStoneBlock(){return new BlockMetaPair(core,(byte)0);}
    public final void onChunkProvide(int chunkX,int chunkZ,Block[] blocks,byte[] meta){long seed=world.getSeed();for(int x=0;x<16;x++)for(int z=0;z<16;z++){Terrain t=sample(seed,chunkX*16+x,chunkZ*16+z);int h=Math.max(8,Math.min(196,t.height)),base=(x*16+z)*256;for(int y=1;y<256;y++){blocks[base+y]=null;meta[base+y]=0;}blocks[base]=Blocks.bedrock;for(int y=1;y<=h;y++)blocks[base+y]=y==h?t.surface:y>=h-4?t.subsurface:core;}constructors().construct(new SUChunkContext(world,blocks,meta,chunkX,chunkZ));}
    public void onPopulate(IChunkProvider provider,int x,int z){} public void populate(IChunkProvider provider,int x,int z){super.populate(provider,x,z);GT6PlanetOreGeneration.generate(world,x,z);constructors().populate(world,x,z,provider);}
    public double getHeightModifier(){return 0;} public double getSmallFeatureHeightModifier(){return 0;} public double getMountainHeightModifier(){return 0;} public double getValleyHeightModifier(){return 0;} public int getCraterProbability(){return 0;}
    protected final double noise(long seed,double x,double z,double scale,int octaves){double sum=0,amp=1,total=0;for(int i=0;i<octaves;i++){sum+=value(seed+i*1013,x*scale,z*scale)*amp;total+=amp;amp*=.5;scale*=2;}return sum/total;}
    protected final Crater crater(long seed,int x,int z,int cell,int minRadius,int maxRadius){int cx=floorDiv(x,cell),cz=floorDiv(z,cell);double depth=0,rim=0,ejecta=0;for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){long h=hash(seed,cx+dx,cz+dz);double px=(cx+dx)*cell+((h>>>8)&255)/255.0*cell,pz=(cz+dz)*cell+((h>>>24)&255)/255.0*cell,r=minRadius+((h>>>40)&127)/127.0*(maxRadius-minRadius),d=Math.sqrt((x-px)*(x-px)+(z-pz)*(z-pz))/r;if(d<1)depth=Math.max(depth,1-d*d);else if(d<1.18)rim=Math.max(rim,1-Math.abs(d-1.08)/.10);else if(d<2.2)ejecta=Math.max(ejecta,(2.2-d)/1.02);}return new Crater(depth,rim,ejecta);}
    private double value(long seed,double x,double z){int x0=(int)Math.floor(x),z0=(int)Math.floor(z);double tx=smooth(x-x0),tz=smooth(z-z0);return lerp(lerp(random(seed,x0,z0),random(seed,x0+1,z0),tx),lerp(random(seed,x0,z0+1),random(seed,x0+1,z0+1),tx),tz);} private double random(long seed,int x,int z){return(hash(seed,x,z)&0x7fffffffL)/1073741823.5-1;} private long hash(long seed,int x,int z){long h=seed+x*341873128712L+z*132897987541L;h=(h^(h>>>33))*0xff51afd7ed558ccdL;h=(h^(h>>>33))*0xc4ceb9fe1a85ec53L;return h^(h>>>33);} private double smooth(double v){return v*v*(3-2*v);} private double lerp(double a,double b,double t){return a+(b-a)*t;} private int floorDiv(int a,int b){int q=a/b;return(a^b)<0&&q*b!=a?q-1:q;}
    protected static final class Crater{final double depth,rim,ejecta;Crater(double d,double r,double e){depth=d;rim=r;ejecta=e;}} protected static final class Terrain{final int height;final Block surface,subsurface;Terrain(int h,Block s,Block u){height=h;surface=s;subsurface=u;}}
}
