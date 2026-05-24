package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b;

import com.google.common.collect.Lists;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b.BiomeDecoratorGliese;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b.BiomeGenBaseGlieseB;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b.MapGenCaveGliese;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b.MapGenCavernGliese;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.blocks.B_database;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.ChunkProviderSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedCreeper;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;
import java.util.List;

public class ChunkProviderGlieseB extends ChunkProviderSpace {

    private final BiomeDecoratorGliese glieseDecorator = new BiomeDecoratorGliese();

    // FIXED
    private final MapGenCaveGliese caveGenerator = new MapGenCaveGliese();
    private final MapGenCavernGliese cavernGenerator = new MapGenCavernGliese();

    public ChunkProviderGlieseB(World world, long seed, boolean mapFeaturesEnabled) {
        super(world, seed, mapFeaturesEnabled);
    }

    @Override
    protected BiomeGenBase.SpawnListEntry[] getMonsters() {
        List<BiomeGenBase.SpawnListEntry> monsters = new ArrayList<BiomeGenBase.SpawnListEntry>();

        monsters.add(new BiomeGenBase.SpawnListEntry(EntityEvolvedZombie.class, 8, 2, 3));
        monsters.add(new BiomeGenBase.SpawnListEntry(EntityEvolvedSpider.class, 8, 2, 3));
        monsters.add(new BiomeGenBase.SpawnListEntry(EntityEvolvedSkeleton.class, 8, 2, 3));
        monsters.add(new BiomeGenBase.SpawnListEntry(EntityEvolvedCreeper.class, 8, 2, 3));

        return monsters.toArray(new BiomeGenBase.SpawnListEntry[monsters.size()]);
    }

    @Override
    protected List<MapGenBaseMeta> getWorldGenerators() {
        List<MapGenBaseMeta> generators = Lists.newArrayList();

        generators.add(this.caveGenerator);
        generators.add(this.cavernGenerator);

        return generators;
    }

    @Override
    protected BiomeGenBase.SpawnListEntry[] getCreatures() {
        return new BiomeGenBase.SpawnListEntry[0];
    }

    @Override
    protected BiomeGenBase[] getBiomesForGeneration() {
        return new BiomeGenBase[] {
                BiomeGenBaseGlieseB.glieseb
        };
    }

    @Override
    protected int getSeaLevel() {
        return 93;
    }

    @Override
    protected BiomeDecoratorSpace getBiomeGenerator() {
        return this.glieseDecorator;
    }

    @Override
    protected BlockMetaPair getGrassBlock() {
        return new BlockMetaPair(B_database.Gliese792bSurface, (byte) 5);
    }

    @Override
    protected BlockMetaPair getDirtBlock() {
        return new BlockMetaPair(B_database.Gliese792bSurface, (byte) 6);
    }

    @Override
    protected BlockMetaPair getStoneBlock() {
        return new BlockMetaPair(B_database.Gliese792bSurface, (byte) 9);
    }

    @Override
    public void onChunkProvide(int cX, int cZ, Block[] blocks, byte[] metadata) {

    }

    @Override
    public void onPopulate(IChunkProvider provider, int cX, int cZ) {

    }

    @Override
    public double getHeightModifier() {
        return 12.0;
    }

    @Override
    public double getSmallFeatureHeightModifier() {
        return 26.0;
    }

    @Override
    public double getMountainHeightModifier() {
        return 95.0;
    }

    @Override
    public double getValleyHeightModifier() {
        return 50.0;
    }

    @Override
    public int getCraterProbability() {
        return 2000;
    }
}