package com.mod.htgt6.common.handler.worldgeneration.shipwerk.structurehandler;

import com.mod.htgt6.common.handler.worldgeneration.shipwerk.WorldGenRightsideupBackhalf;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

public class OceanStructureHandler {
    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Post event) {
        // We use .Post to ensure the terrain is finished generating

        // 1. Get the biome at the current decoration coordinates
        BiomeGenBase biome = event.world.getBiomeGenForCoords(event.chunkX, event.chunkZ);

        // 2. Check if the biome is an Ocean (works for vanilla and most modded oceans)
        if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.OCEAN)) {

            // 3. Set your rarity (e.g., 1 in 100 chunks)
            if (event.rand.nextInt(100) == 0) {

                int x = event.chunkX + event.rand.nextInt(16) + 8;
                int z = event.chunkZ + event.rand.nextInt(16) + 8;

                // Find the floor
                int y = event.world.getTopSolidOrLiquidBlock(x, z);

                // Only spawn if it's deep water
                if (y < 60) {
                    new WorldGenRightsideupBackhalf().generate(event.world, event.rand, x, y - 1, z);
                }
            }
        }
    }
}

