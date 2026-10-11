package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

/** Reusable ore pass analogous to WE_OreGen. */
public final class OreVeinConstructor implements SUPopulationConstructor {
    private final Block ore, host;
    private final int metadata, veinSize, attempts, minY, maxY;
    public OreVeinConstructor(Block ore,int metadata,int veinSize,int attempts,int minY,int maxY,Block host){this.ore=ore;this.metadata=metadata;this.veinSize=veinSize;this.attempts=attempts;this.minY=minY;this.maxY=Math.max(minY,maxY);this.host=host;}
    public void populate(World world,Random random,int chunkX,int chunkZ,IChunkProvider provider){WorldGenMinable generator=new WorldGenMinable(ore,metadata,veinSize,host);for(int i=0;i<attempts;i++){int x=chunkX*16+random.nextInt(16),z=chunkZ*16+random.nextInt(16),y=minY+random.nextInt(maxY-minY+1);generator.generate(world,random,x,y,z);}}
}
