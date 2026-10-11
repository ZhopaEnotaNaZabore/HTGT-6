package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/** Small surface feature constructor for airless rubble-covered bodies. */
public final class BoulderConstructor implements SUPopulationConstructor {
    private final Block block;
    private final int metadata, chance, minRadius, maxRadius;
    public BoulderConstructor(Block block,int metadata,int chance,int minRadius,int maxRadius){this.block=block;this.metadata=metadata;this.chance=Math.max(1,chance);this.minRadius=Math.min(7,Math.max(1,minRadius));this.maxRadius=Math.min(7,Math.max(this.minRadius,maxRadius));}
    public void populate(World world,Random random,int chunkX,int chunkZ,IChunkProvider provider){if(random.nextInt(chance)!=0)return;int radius=minRadius+random.nextInt(maxRadius-minRadius+1),span=16-radius*2;int cx=chunkX*16+radius+random.nextInt(span),cz=chunkZ*16+radius+random.nextInt(span),cy=world.getHeightValue(cx,cz);if(cy<1||cy+radius>=256||!world.getBlock(cx,cy-1,cz).getMaterial().isSolid())return;for(int x=-radius;x<=radius;x++)for(int y=0;y<=radius;y++)for(int z=-radius;z<=radius;z++)if(x*x+y*y+z*z<=radius*radius)world.setBlock(cx+x,cy+y,cz+z,block,metadata,2);}
}
