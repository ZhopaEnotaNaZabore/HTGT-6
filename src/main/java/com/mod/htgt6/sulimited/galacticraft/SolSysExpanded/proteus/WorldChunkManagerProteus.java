package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.proteus;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public final class WorldChunkManagerProteus extends WorldChunkManager {
    public List<BiomeGenBase> getBiomesToSpawnIn() { return Arrays.<BiomeGenBase>asList(BiomeGenSpaceUltimate.proteus); }
    public BiomeGenBase getBiomeGenAt(int x, int z) { return BiomeGenSpaceUltimate.proteus; }
    public float[] getRainfall(float[] out,int x,int z,int width,int length) { if(out==null||out.length<width*length)out=new float[width*length]; Arrays.fill(out,0); return out; }
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] out,int x,int z,int width,int length) { if(out==null||out.length<width*length)out=new BiomeGenBase[width*length]; Arrays.fill(out,BiomeGenSpaceUltimate.proteus); return out; }
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] out,int x,int z,int width,int length){return getBiomesForGeneration(out,x,z,width,length);}
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] out,int x,int z,int width,int length,boolean cache){return getBiomesForGeneration(out,x,z,width,length);}
    public ChunkPosition findBiomePosition(int x,int z,int range,List list,Random random){return new ChunkPosition(x,64,z);}
    public boolean areBiomesViable(int x,int z,int radius,List list){return true;}
}
