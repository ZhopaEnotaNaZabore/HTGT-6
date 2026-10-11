package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.proteus;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.proteus.blocks.ProteusBlocks;
import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysChunkProvider;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

/** Faceted, rubble-rich and impact-saturated terrain unlike Charon's icy canyon world. */
public final class ChunkProviderProteus extends SolSysChunkProvider {
    private final World world;
    public ChunkProviderProteus(World world,long seed,boolean features){super(world,seed,features);this.world=world;}
    protected BiomeGenBase.SpawnListEntry[] getMonsters(){return new BiomeGenBase.SpawnListEntry[0];}
    protected BiomeGenBase.SpawnListEntry[] getCreatures(){return new BiomeGenBase.SpawnListEntry[0];}
    protected List<MapGenBaseMeta> getWorldGenerators(){return Collections.emptyList();}
    protected BiomeGenBase[] getBiomesForGeneration(){return new BiomeGenBase[]{BiomeGenSpaceUltimate.proteus};}
    protected int getSeaLevel(){return 0;}
    protected BlockMetaPair getGrassBlock(){return new BlockMetaPair(ProteusBlocks.regolith,(byte)0);}
    protected BlockMetaPair getDirtBlock(){return new BlockMetaPair(ProteusBlocks.impactBreccia,(byte)0);}
    protected BlockMetaPair getStoneBlock(){return new BlockMetaPair(ProteusBlocks.bedrock,(byte)0);}

    public void onChunkProvide(int chunkX,int chunkZ,Block[] blocks,byte[] meta){
        long seed=world.getSeed();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++){
            int wx=chunkX*16+x,wz=chunkZ*16+z;
            double body=noise(seed+5,wx,wz,0.0028,4);
            double facets=Math.rint(noise(seed+17,wx,wz,0.010,2)*5.0)/5.0;
            Crater small=crater(seed+41,wx,wz,104,18,42);
            Crater pharos=crater(seed+97,wx,wz,640,105,190);
            double relief=body*14.0+facets*18.0+small.rim*11.0+pharos.rim*27.0-small.depth*24.0-pharos.depth*58.0;
            int height=Math.max(12,Math.min(178,72+(int)Math.round(relief)));
            boolean ejecta=small.ejecta>0.22||pharos.ejecta>0.10;
            int base=(x*16+z)*256; blocks[base]=Blocks.bedrock;
            for(int y=1;y<=height;y++){
                if(y==height)blocks[base+y]=ejecta?ProteusBlocks.impactBreccia:ProteusBlocks.regolith;
                else if(y>height-4&&ejecta)blocks[base+y]=ProteusBlocks.impactBreccia;
                else blocks[base+y]=ProteusBlocks.bedrock;
            }
        }
    }
    public void onPopulate(IChunkProvider provider,int x,int z){}
    public void populate(IChunkProvider provider,int chunkX,int chunkZ){
        super.populate(provider,chunkX,chunkZ);
        Random random=new Random(world.getSeed()^chunkX*341873128712L^chunkZ*132897987541L);
        if(random.nextInt(3)==0){int radius=1+random.nextInt(2);int x=chunkX*16+radius+random.nextInt(16-radius*2),z=chunkZ*16+radius+random.nextInt(16-radius*2);int y=world.getHeightValue(x,z);
            for(int dx=-radius;dx<=radius;dx++)for(int dy=0;dy<=radius;dy++)for(int dz=-radius;dz<=radius;dz++)if(dx*dx+dy*dy+dz*dz<=radius*radius)world.setBlock(x+dx,y+dy,z+dz,ProteusBlocks.bedrock,0,2);
        }
    }
    private Crater crater(long seed,int x,int z,int cell,int minRadius,int maxRadius){
        int cx=floorDiv(x,cell),cz=floorDiv(z,cell);double bestDepth=0,bestRim=0,bestEjecta=0;
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){long h=hash(seed,cx+dx,cz+dz);double px=(cx+dx)*cell+((h>>>8)&255)/255.0*cell,pz=(cz+dz)*cell+((h>>>24)&255)/255.0*cell;double radius=minRadius+((h>>>40)&127)/127.0*(maxRadius-minRadius);double d=Math.sqrt((x-px)*(x-px)+(z-pz)*(z-pz))/radius;
            if(d<1.0)bestDepth=Math.max(bestDepth,1.0-d*d);else if(d<1.18)bestRim=Math.max(bestRim,1.0-Math.abs(d-1.08)/0.10);else if(d<2.3)bestEjecta=Math.max(bestEjecta,(2.3-d)/1.12);
        }return new Crater(bestDepth,bestRim,bestEjecta);
    }
    private double noise(long seed,double x,double z,double scale,int octaves){double sum=0,amp=1,total=0;for(int i=0;i<octaves;i++){sum+=value(seed+i*1013,x*scale,z*scale)*amp;total+=amp;amp*=0.5;scale*=2;}return sum/total;}
    private double value(long seed,double x,double z){int x0=(int)Math.floor(x),z0=(int)Math.floor(z);double tx=smooth(x-x0),tz=smooth(z-z0);return lerp(lerp(rand(seed,x0,z0),rand(seed,x0+1,z0),tx),lerp(rand(seed,x0,z0+1),rand(seed,x0+1,z0+1),tx),tz);}
    private double rand(long seed,int x,int z){return(hash(seed,x,z)&0x7fffffffL)/1073741823.5-1;}
    private long hash(long seed,int x,int z){long h=seed+x*341873128712L+z*132897987541L;h=(h^(h>>>33))*0xff51afd7ed558ccdL;h=(h^(h>>>33))*0xc4ceb9fe1a85ec53L;return h^(h>>>33);}
    private double smooth(double v){return v*v*(3-2*v);}private double lerp(double a,double b,double t){return a+(b-a)*t;}private int floorDiv(int a,int b){int q=a/b;return(a^b)<0&&q*b!=a?q-1:q;}
    private static final class Crater{final double depth,rim,ejecta;Crater(double d,double r,double e){depth=d;rim=r;ejecta=e;}}
    public double getHeightModifier(){return 0;}public double getSmallFeatureHeightModifier(){return 0;}public double getMountainHeightModifier(){return 0;}public double getValleyHeightModifier(){return 0;}public int getCraterProbability(){return 0;}
}
