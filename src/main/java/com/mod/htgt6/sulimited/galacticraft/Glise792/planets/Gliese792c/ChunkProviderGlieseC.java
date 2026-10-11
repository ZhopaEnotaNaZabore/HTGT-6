package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c;

import com.google.common.collect.Lists;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.biomeGliese792c.BiomeDecoratorGlieseC;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.blocksGliese792C;
import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import com.mod.htgt6.sulimited.world.GlieseCSubBiomeGenerator;
import com.mod.htgt6.sulimited.world.GT6PlanetOreGeneration;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology;
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

/** Ocean-world terrain for Gliese 792 C. Sea level deliberately remains Y=160. */
public class ChunkProviderGlieseC extends com.mod.htgt6.sulimited.world.GlieseChunkProvider {
    private static final int SEA_LEVEL = 160;

    private final BiomeDecoratorGlieseC biomeDecorator = new BiomeDecoratorGlieseC();
    private final World localWorldObj;
    private final GlieseCSubBiomeGenerator subBiomeGenerator;

    public ChunkProviderGlieseC(World world, long seed, boolean mapFeaturesEnabled) {
        super(world, seed, mapFeaturesEnabled);
        this.localWorldObj = world;
        this.subBiomeGenerator = BiomeGenSpaceUltimate.glieseC.getGlieseCSubBiomeGenerator();
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
    protected BiomeGenBase.SpawnListEntry[] getCreatures() {
        return new BiomeGenBase.SpawnListEntry[0];
    }

    @Override
    protected List<MapGenBaseMeta> getWorldGenerators() {
        List<MapGenBaseMeta> generators = Lists.newArrayList();
        return generators;
    }

    @Override
    protected BiomeGenBase[] getBiomesForGeneration() {
        return new BiomeGenBase[] { BiomeGenSpaceUltimate.glieseC };
    }

    @Override
    protected BiomeDecoratorSpace getBiomeGenerator() {
        return this.biomeDecorator;
    }

    @Override
    protected int getSeaLevel() { return SEA_LEVEL; }

    @Override
    protected BlockMetaPair getGrassBlock() { return new BlockMetaPair(Blocks.grass, (byte) 0); }

    @Override
    protected BlockMetaPair getDirtBlock() { return new BlockMetaPair(blocksGliese792C.GliseCDirt, (byte) 0); }

    @Override
    protected BlockMetaPair getStoneBlock() { return new BlockMetaPair(blocksGliese792C.GlieseCstone, (byte) 0); }

    @Override
    public double getHeightModifier() { return 0.0D; }

    @Override
    public double getSmallFeatureHeightModifier() { return 0.0D; }

    @Override
    public double getMountainHeightModifier() { return 0.0D; }

    @Override
    public double getValleyHeightModifier() { return 0.0D; }

    @Override
    public int getCraterProbability() { return 0; }

    @Override
    public void onChunkProvide(int chunkX, int chunkZ, Block[] blocks, byte[] metadata) {
        long seed = this.localWorldObj.getSeed();
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;
                GlieseCSubBiomeGenerator.Sample sample = this.subBiomeGenerator.sample(seed, worldX, worldZ, SEA_LEVEL);
                fillColumn(blocks, localX, localZ, sample.height, sample.type);
            }
        }
    }

    private void fillColumn(Block[] blocks, int x, int z, int floorY, GlieseCSubBiomeGenerator.Type type) {
        floorY = Math.max(4, Math.min(210, floorY));
        blocks[(x * 16 + z) * 256] = Blocks.bedrock;
        for (int y = 1; y <= floorY; y++) {
            int index = (x * 16 + z) * 256 + y;
            if (y < floorY - 4) {
                blocks[index] = blocksGliese792C.GlieseCstone;
            } else if (y < floorY) {
                blocks[index] = isBeach(type) ? blocksGliese792C.beachSand : blocksGliese792C.GliseCDirt;
            } else if (y == floorY) {
                blocks[index] = surfaceBlock(type);
            }
        }
        for (int y = floorY + 1; y <= SEA_LEVEL; y++) blocks[(x * 16 + z) * 256 + y] = SolSysGeology.saltWater;
    }

    private boolean isBeach(GlieseCSubBiomeGenerator.Type type) {
        return type == GlieseCSubBiomeGenerator.Type.BEACHES || type == GlieseCSubBiomeGenerator.Type.LOW_SEA
                || type == GlieseCSubBiomeGenerator.Type.REEF;
    }

    private Block surfaceBlock(GlieseCSubBiomeGenerator.Type type) {
        if (type == GlieseCSubBiomeGenerator.Type.BEACHES || type == GlieseCSubBiomeGenerator.Type.LOW_SEA) return blocksGliese792C.beachSand;
        if (type == GlieseCSubBiomeGenerator.Type.LOWLANDS || type == GlieseCSubBiomeGenerator.Type.ISLANDS || type == GlieseCSubBiomeGenerator.Type.MOSSY_HILLS)
            return blocksGliese792C.GlieseCMossyPetrifiedEarth;
        if (type == GlieseCSubBiomeGenerator.Type.UNDERWATER_VOLCANO) return blocksGliese792C.ventRock;
        if (type == GlieseCSubBiomeGenerator.Type.REEF) return blocksGliese792C.reefRock;
        if (type == GlieseCSubBiomeGenerator.Type.DEEP_REEF) return blocksGliese792C.deepReefRock;
        return blocksGliese792C.GlieseCstone;
    }

    @Override
    public void onPopulate(IChunkProvider provider, int chunkX, int chunkZ) { }

    @Override
    public void populate(IChunkProvider provider, int chunkX, int chunkZ) {
        super.populate(provider, chunkX, chunkZ);
        GT6PlanetOreGeneration.generate(this.localWorldObj, chunkX, chunkZ);
    }
    @Override
    protected void decorateTerrain(int x,int z,Block[] blocks,byte[] meta) {
        biomeDecorator.decorateColumns(localWorldObj.getSeed(),x,z,blocks,meta,subBiomeGenerator);
    }
}
