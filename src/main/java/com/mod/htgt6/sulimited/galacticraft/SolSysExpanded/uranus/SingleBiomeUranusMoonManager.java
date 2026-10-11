package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

abstract class SingleBiomeUranusMoonManager extends WorldChunkManager {
    private final BiomeGenBase biome;
    SingleBiomeUranusMoonManager(BiomeGenBase biome){this.biome=biome;}
    public List<BiomeGenBase> getBiomesToSpawnIn(){return Arrays.asList(biome);} public BiomeGenBase getBiomeGenAt(int x,int z){return biome;}
    public float[] getRainfall(float[] out,int x,int z,int w,int l){if(out==null||out.length<w*l)out=new float[w*l];Arrays.fill(out,0);return out;}
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] out,int x,int z,int w,int l){if(out==null||out.length<w*l)out=new BiomeGenBase[w*l];Arrays.fill(out,biome);return out;}
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] out,int x,int z,int w,int l){return getBiomesForGeneration(out,x,z,w,l);} public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] out,int x,int z,int w,int l,boolean cache){return getBiomesForGeneration(out,x,z,w,l);}
    public ChunkPosition findBiomePosition(int x,int z,int range,List list,java.util.Random random){return new ChunkPosition(x,64,z);} public boolean areBiomesViable(int x,int z,int radius,List list){return true;}
}
