package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c;

import com.google.common.collect.Lists;

import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.biomeGliese792c.BiomeDecoratorGlieseC;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.biomeGliese792c.BiomeGenBaseGlieseC;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b.MapGenCaveGliese;

import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.blocks.GlieseCwater;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.blocks.blocksGliese792C;
import gregapi.data.MT;
import gregapi.util.ST;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.ChunkProviderSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;

import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedCreeper;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChunkProviderGlieseC extends ChunkProviderSpace {

    private final BiomeDecoratorGlieseC biomeDecorator =
            new BiomeDecoratorGlieseC();

    private final MapGenCaveGliese caveGenerator =
            new MapGenCaveGliese();

    // Local reference to avoid private field access issues from parent classes
    private final World localWorldObj;

    public ChunkProviderGlieseC(
            World world,
            long seed,
            boolean mapFeaturesEnabled) {

        super(world, seed, mapFeaturesEnabled);
        this.localWorldObj = world;
    }

    @Override
    protected BiomeGenBase.SpawnListEntry[] getMonsters() {

        List<BiomeGenBase.SpawnListEntry> monsters =
                new ArrayList<BiomeGenBase.SpawnListEntry>();

        monsters.add(new BiomeGenBase.SpawnListEntry(
                EntityEvolvedZombie.class,
                8,
                2,
                3
        ));

        monsters.add(new BiomeGenBase.SpawnListEntry(
                EntityEvolvedSpider.class,
                8,
                2,
                3
        ));

        monsters.add(new BiomeGenBase.SpawnListEntry(
                EntityEvolvedSkeleton.class,
                8,
                2,
                3
        ));

        monsters.add(new BiomeGenBase.SpawnListEntry(
                EntityEvolvedCreeper.class,
                8,
                2,
                3
        ));

        return monsters.toArray(
                new BiomeGenBase.SpawnListEntry[monsters.size()]
        );
    }

    @Override
    protected BiomeGenBase.SpawnListEntry[] getCreatures() {
        return new BiomeGenBase.SpawnListEntry[0];
    }

    @Override
    protected List<MapGenBaseMeta> getWorldGenerators() {

        List<MapGenBaseMeta> generators = Lists.newArrayList();

        generators.add(this.caveGenerator);

        return generators;
    }

    @Override
    protected BiomeGenBase[] getBiomesForGeneration() {

        return new BiomeGenBase[] {
                BiomeGenBaseGlieseC.gliesec
        };
    }

    // =========================
    // BIOME DECORATOR
    // =========================

    @Override
    protected BiomeDecoratorSpace getBiomeGenerator() {
        return this.biomeDecorator;
    }

    // =========================
    // WORLD SETTINGS
    // =========================

    @Override
    protected int getSeaLevel() {
        return 160;
    }

    @Override
    protected BlockMetaPair getGrassBlock() {

        return new BlockMetaPair(
                Blocks.grass,
                (byte) 0
        );
    }

    @Override
    protected BlockMetaPair getDirtBlock() {

        return new BlockMetaPair(
                Blocks.dirt,
                (byte) 0
        );
    }

    @Override
    protected BlockMetaPair getStoneBlock() {

        return new BlockMetaPair(
                Blocks.stone,
                (byte) 0
        );
    }

    // =========================
    // TERRAIN MODIFIERS
    // =========================

    @Override
    public double getHeightModifier() {
        return 6.0D;
    }

    @Override
    public double getSmallFeatureHeightModifier() {
        return 18.0D;
    }

    @Override
    public double getMountainHeightModifier() {
        return 42.0D;
    }

    @Override
    public double getValleyHeightModifier() {
        return 80.0D;
    }

    @Override
    public int getCraterProbability() {
        return 999999;
    }

    // =========================
    // CUSTOM ISLAND GENERATION
    // =========================

    @Override
    public void onChunkProvide(
            int chunkX,
            int chunkZ,
            Block[] blocks,
            byte[] metadata) {

        Random rand = new Random(
                (chunkX * 341873128712L)
                        + (chunkZ * 132897987541L)
        );

        // ===================================
        // OCEAN BASE, BEDROCK & STONE LAYERS
        // ===================================

        for (int x = 0; x < 16; x++) {

            for (int z = 0; z < 16; z++) {

                for (int y = 0; y <= 160; y++) {

                    int index =
                            (x * 16 + z) * 256 + y;

                    if (index >= 0 && index < blocks.length) {

                        if (y <= 1) {
                            blocks[index] = Blocks.bedrock;
                        }
                        else if (y <= 40) {
                            blocks[index] = blocksGliese792C.GlieseCstone;
                        }
                        else {
                            blocks[index] = GlieseCwater.blockGlieseCwaterStill;

                        }
                    }
                }
            }
        }

        // =========================
        // ISLAND CHANCE
        // =========================

        boolean largeIsland =
                rand.nextInt(40) == 0;

        boolean smallIsland =
                rand.nextInt(6) == 0;

        if (!largeIsland && !smallIsland) {
            return;
        }

        int centerX = 8;
        int centerZ = 8;

        int radius = largeIsland
                ? 14 + rand.nextInt(8)
                : 4 + rand.nextInt(5);

        int height = largeIsland
                ? 20 + rand.nextInt(10)
                : 5 + rand.nextInt(4);

        for (int x = 0; x < 16; x++) {

            for (int z = 0; z < 16; z++) {

                double dx = x - centerX;
                double dz = z - centerZ;

                double dist =
                        Math.sqrt(dx * dx + dz * dz);

                if (dist <= radius) {

                    int terrainHeight =
                            (int)(160 + height - (dist * 1.5));

                    terrainHeight =
                            Math.max(
                                    1,
                                    Math.min(255, terrainHeight)
                            );

                    for (int y = 41; y < terrainHeight; y++) {

                        if (y < 0 || y >= 256) {
                            continue;
                        }

                        int index =
                                (x * 16 + z) * 256 + y;

                        if (index < 0
                                || index >= blocks.length) {
                            continue;
                        }

                        if (y == terrainHeight - 1) {

                            blocks[index] = Blocks.grass;
                        }
                        else if (y >= terrainHeight - 4) {

                            blocks[index] = blocksGliese792C.GliseCDirt;
                        }
                        else {

                            blocks[index] = blocksGliese792C.GlieseCstone;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPopulate(
            IChunkProvider provider,
            int chunkX,
            int chunkZ) {
    }

    @Override
    public void populate(IChunkProvider provider, int chunkX, int chunkZ) {
        super.populate(provider, chunkX, chunkZ);

        // ====================================================
        // FIX DECORATOR PART: REMOVE REPLACING WATER TO ICE
        // ====================================================
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 41; y <= 160; y++) {
                    // Uses localWorldObj here to bypass the access restriction error
                    if (this.localWorldObj.getBlock(blockX + x, y, blockZ + z) == Blocks.ice) {
                        this.localWorldObj.setBlock(blockX + x, y, blockZ + z, Blocks.water, 0, 2);
                    }
                }
            }
        }
    }
}