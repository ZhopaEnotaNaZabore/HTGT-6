package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.charon;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.charon.blocks.CharonBlocks;
import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
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

/** Seed-stable terrain scaled from Charon's measured relief and geology. */
public final class ChunkProviderCharon extends SolSysChunkProvider {
    private final World world;

    public ChunkProviderCharon(World world, long seed, boolean features) { super(world, seed, features); this.world = world; }
    protected BiomeGenBase.SpawnListEntry[] getMonsters() { return new BiomeGenBase.SpawnListEntry[0]; }
    protected BiomeGenBase.SpawnListEntry[] getCreatures() { return new BiomeGenBase.SpawnListEntry[0]; }
    protected List<MapGenBaseMeta> getWorldGenerators() { return Collections.emptyList(); }
    protected BiomeGenBase[] getBiomesForGeneration() { return new BiomeGenBase[] { BiomeGenSpaceUltimate.charon }; }
    protected int getSeaLevel() { return 0; }
    protected BlockMetaPair getGrassBlock() { return new BlockMetaPair(CharonBlocks.regolith, (byte)0); }
    protected BlockMetaPair getDirtBlock() { return new BlockMetaPair(CharonBlocks.crust, (byte)0); }
    protected BlockMetaPair getStoneBlock() { return new BlockMetaPair(CharonBlocks.crust, (byte)0); }

    public void onChunkProvide(int chunkX, int chunkZ, Block[] blocks, byte[] meta) {
        long seed = world.getSeed();
        for (int x=0; x<16; x++) for (int z=0; z<16; z++) {
            int wx=chunkX*16+x, wz=chunkZ*16+z;
            double basin = noise(seed+11, wx, wz, 0.0018, 4);
            double ridge = 1.0-Math.abs(noise(seed+37, wx, wz, 0.0065, 3));
            double canyon = Math.abs(noise(seed+71, wx, wz, 0.013, 2));
            double crater = craterField(seed, wx, wz);
            // Charon's ~20 km observed relief is compressed to a playable ~90-block range.
            int h = 76 + (int)(basin*17 + ridge*22 - crater*34);
            boolean chasm = canyon < 0.075;
            if (chasm) h -= 28 + (int)((0.075-canyon)*260);
            h = Math.max(18, Math.min(168, h));
            int base=(x*16+z)*256;
            blocks[base]=Blocks.bedrock;
            for(int y=1;y<=h;y++) {
                blocks[base+y] = y==h ? CharonBlocks.regolith : (chasm && y>h-8 ? CharonBlocks.canyonIce : CharonBlocks.crust);
            }
        }
    }
    public void onPopulate(IChunkProvider provider, int x, int z) { }
    public void populate(IChunkProvider provider, int x, int z) { super.populate(provider,x,z); }
    private double craterField(long seed,int x,int z) {
        int cell=192, cx=floorDiv(x,cell), cz=floorDiv(z,cell); double best=0;
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
            long h=hash(seed,cx+dx,cz+dz); double px=(cx+dx)*cell+((h>>>8)&255)/255.0*cell;
            double pz=(cz+dz)*cell+((h>>>24)&255)/255.0*cell; double radius=25+((h>>>40)&127)/127.0*65;
            double d=Math.sqrt((x-px)*(x-px)+(z-pz)*(z-pz))/radius;
            if(d<1) best=Math.max(best,(1-d*d)*(0.72+0.28*Math.cos(d*Math.PI*3)));
        } return best;
    }
    private double noise(long seed,double x,double z,double scale,int octaves) {
        double sum=0,amp=1,total=0; for(int i=0;i<octaves;i++){sum+=value(seed+i*1013,x*scale,z*scale)*amp;total+=amp;amp*=0.5;scale*=2;} return sum/total;
    }
    private double value(long seed,double x,double z){int x0=(int)Math.floor(x),z0=(int)Math.floor(z);double tx=smooth(x-x0),tz=smooth(z-z0);return lerp(lerp(rand(seed,x0,z0),rand(seed,x0+1,z0),tx),lerp(rand(seed,x0,z0+1),rand(seed,x0+1,z0+1),tx),tz);}
    private double rand(long seed,int x,int z){return (hash(seed,x,z)&0x7fffffffL)/1073741823.5-1;}
    private long hash(long seed,int x,int z){long h=seed+x*341873128712L+z*132897987541L;h=(h^(h>>>33))*0xff51afd7ed558ccdL;h=(h^(h>>>33))*0xc4ceb9fe1a85ec53L;return h^(h>>>33);}
    private double smooth(double v){return v*v*(3-2*v);} private double lerp(double a,double b,double t){return a+(b-a)*t;}
    private int floorDiv(int a,int b){int q=a/b;return (a^b)<0&&q*b!=a?q-1:q;}
    public double getHeightModifier(){return 0;} public double getSmallFeatureHeightModifier(){return 0;} public double getMountainHeightModifier(){return 0;} public double getValleyHeightModifier(){return 0;} public int getCraterProbability(){return 0;}
}
