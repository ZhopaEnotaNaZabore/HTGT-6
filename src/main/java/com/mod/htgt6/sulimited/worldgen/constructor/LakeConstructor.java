package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenLakes;

/** Optional populate-stage lake constructor for atmospheric or liquid worlds. */
public final class LakeConstructor implements SUPopulationConstructor {
    private final Block liquid;
    private final int chance, minY, maxY;
    public LakeConstructor(Block liquid,int chance,int minY,int maxY){this.liquid=liquid;this.chance=Math.max(1,chance);this.minY=minY;this.maxY=Math.max(minY,maxY);}
    public void populate(World world,Random random,int chunkX,int chunkZ,IChunkProvider provider){if(random.nextInt(chance)!=0)return;int x=chunkX*16+8+random.nextInt(16),z=chunkZ*16+8+random.nextInt(16),y=minY+random.nextInt(maxY-minY+1);new WorldGenLakes(liquid).generate(world,random,x,y,z);}
}
