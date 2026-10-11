package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b;

import com.google.common.collect.Lists;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.biomeGliese792b.BiomeDecoratorGliese;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.biomeGliese792b.MapGenCaveGliese;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.biomeGliese792b.MapGenCavernGliese;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.blocks.B_database;
import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import com.mod.htgt6.sulimited.world.GT6PlanetOreGeneration;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
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

public class ChunkProviderGlieseB extends com.mod.htgt6.sulimited.world.GlieseChunkProvider {

    private final BiomeDecoratorGliese glieseDecorator = new BiomeDecoratorGliese();
    private final MapGenCaveGliese caveGenerator = new MapGenCaveGliese();
    private final MapGenCavernGliese cavernGenerator = new MapGenCavernGliese();
    private final World localWorldObj;

    public ChunkProviderGlieseB(World world, long seed, boolean mapFeaturesEnabled) {
        super(world, seed, mapFeaturesEnabled);
        this.localWorldObj = world;
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
        // CHANGED: Now using the wrapper biome
        return new BiomeGenBase[] {
                BiomeGenSpaceUltimate.spaceUltimate
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
        return new BlockMetaPair(B_database.Gliese792bAsh, (byte) 0);
    }

    @Override
    protected BlockMetaPair getDirtBlock() {
        return new BlockMetaPair(B_database.Gliese792bSurface, (byte) 0);
    }

    @Override
    protected BlockMetaPair getStoneBlock() {
        return new BlockMetaPair(B_database.Gliese792bBasalt, (byte) 0);
    }

    @Override
    public void onChunkProvide(int chunkX, int chunkZ, Block[] blocks, byte[] metadata) {
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;
        long seed = this.localWorldObj.getSeed();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;
                com.mod.htgt6.sulimited.world.GlieseTerrain.VolcanicSample terrain = com.mod.htgt6.sulimited.world.GlieseTerrain.volcanic(seed, worldX, worldZ);
                fillColumn(blocks, metadata, localX, localZ, terrain);
            }
        }
    }

    @Override
    public void onPopulate(IChunkProvider provider, int chunkX, int chunkZ) {}

    @Override
    public void populate(IChunkProvider provider, int chunkX, int chunkZ) {
        super.populate(provider, chunkX, chunkZ);
        GT6PlanetOreGeneration.generate(this.localWorldObj, chunkX, chunkZ);
    }

    @Override
    protected void decorateTerrain(int x,int z,Block[] blocks,byte[] meta) {
        glieseDecorator.decorateColumns(localWorldObj.getSeed(),x,z,blocks,meta);
    }

    private void fillColumn(Block[] blocks,byte[] metadata,int x,int z,com.mod.htgt6.sulimited.world.GlieseTerrain.VolcanicSample t) {
        int column=(x*16+z)*256;
        blocks[column]=Blocks.bedrock;
        Block top=t.volcano?B_database.scoria:t.dryChannel?B_database.dryChannel:t.wasteland?B_database.Gliese792bAsh:B_database.Gliese792bSurface;
        for(int y=1;y<=t.height;y++) {
            blocks[column+y]=y<t.height-5?B_database.Gliese792bBasalt:y<t.height-1?B_database.Gliese792bSurface:top;
        }
        for(int y=t.height+1;y<=t.lavaLevel;y++) blocks[column+y]=Blocks.lava;
    }

    @Override
    public double getHeightModifier() { return 0.0; }
    @Override
    public double getSmallFeatureHeightModifier() { return 0.0; }
    @Override
    public double getMountainHeightModifier() { return 0.0; }
    @Override
    public double getValleyHeightModifier() { return 0.0; }
    @Override
    public int getCraterProbability() { return 0; }
}
