package com.mod.htgt6.minecraft1202.Module.World.Frostburn;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120Ore;

/** ore_magma: count 4, uniform inclusive Y 27..36, size 33, netherrack only. */
public final class FrostburnMagmaGenerator120 implements IWorldGenerator {
    private final WorldGenBackport120Ore ore =
            new WorldGenBackport120Ore(FrostburnContent.magmaBlock,33,Blocks.netherrack);
    @Override public void generate(Random random,int chunkX,int chunkZ,World world,
            IChunkProvider generator,IChunkProvider provider) {
        if (world.provider.dimensionId != -1) return;
        for (int attempt=0;attempt<4;attempt++) {
            int x=(chunkX<<4)+random.nextInt(16);
            int z=(chunkZ<<4)+random.nextInt(16);
            int y=27+random.nextInt(10);
            ore.generate(world,random,x,y,z);
        }
    }
}
