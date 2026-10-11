package com.mod.htgt6.minecraft1202.Module.World.Combat;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/** 1.10 overworld structures; End generation is owned by ChunkProviderCombatEnd. */
public final class CombatWorldGenerator implements IWorldGenerator {
    @Override public void generate(Random random,int chunkX,int chunkZ,World world,
                                   IChunkProvider generator,IChunkProvider provider){
        if(world.provider.dimensionId==0&&world.getWorldInfo().isMapFeaturesEnabled())
            IglooNbtGenerator.generate(chunkX,chunkZ,world);
    }
}
