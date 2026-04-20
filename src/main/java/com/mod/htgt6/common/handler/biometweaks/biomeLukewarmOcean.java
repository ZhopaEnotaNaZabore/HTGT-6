package com.mod.htgt6.common.handler.biometweaks;

import com.mod.htgt6.common.handler.worldgeneration.shipwerk.WorldGenRightsideupBackhalf;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;


public class biomeLukewarmOcean extends BiomeGenOcean {

    public biomeLukewarmOcean (int id) {
        super(id);
        this.setBiomeName("Lukewarm Ocean");
       // this.waterColorMultiplier = 0x89FD89;
        this.topBlock = Blocks.sand;
        this.fillerBlock = Blocks.sand;
        this.rootHeight = -0.5F;
        this.heightVariation = 0.5F;
        this.theBiomeDecorator.treesPerChunk = 1; // Occasional trees on islands
        this.theBiomeDecorator.flowersPerChunk = 2;
        this.theBiomeDecorator.grassPerChunk = 5;
        this.theBiomeDecorator.sandPerChunk2 = 10;
        this.rootHeight = -1.0F;
        this.heightVariation = 0.1F;
        this.spawnableWaterCreatureList.add(new SpawnListEntry(net.minecraft.entity.passive.EntitySquid.class, 10, 4, 4));
    }
    @Override
    public WorldGenAbstractTree func_150567_a(Random rand) {
        // If the island is sand, maybe spawn a custom Palm Tree generator
        // Otherwise, return the standard Jungle or Oak tree
        return (rand.nextInt(3) == 0 ? worldGeneratorBigTree : worldGeneratorTrees);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public int getBiomeGrassColor(int x, int y, int z) {
        return 0x99FF33; // Bright Lime Green
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBiomeFoliageColor(int x, int y, int z) {
        return 0x99FF33; // Matches leaves to the grass
    }

    @Override
    public void decorate(World world, Random rand, int chunkX, int chunkZ) {
        // IMPORTANT: Always call super first to generate standard flowers/sand/gravel
        super.decorate(world, rand, chunkX, chunkZ);

        // 1 in 60 chance to spawn a ship stern in this chunk
        // Adjust the number (60) higher for more rarity
        if (rand.nextInt(60) == 0) {

            // Calculate random coordinates within the 16x16 chunk
            int x = chunkX + rand.nextInt(16) + 8;
            int z = chunkZ + rand.nextInt(16) + 8;

            // Find the ocean floor (looking for the first solid block under the water)
            int y = world.getTopSolidOrLiquidBlock(x, z);

            // Safety check: Only spawn if the floor is deep enough (below sea level)
            if (y < 60) {
                // Subtract a bit of Y if you want the ship to be "buried" in the sand
                new WorldGenRightsideupBackhalf().generate(world, rand, x, y, z);
            }
        }
    }
    }

