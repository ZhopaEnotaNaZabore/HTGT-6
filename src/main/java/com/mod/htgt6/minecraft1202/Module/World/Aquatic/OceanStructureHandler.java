package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenVanillaShipwreck;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import com.mod.htgt6.minecraft1202.Module.Data.Aquatic.AquaticStructureData;
import com.mod.htgt6.common.util.WorldSafety;
import com.mod.htgt6.common.util.StructurePlacement120;
import com.mod.htgt6.common.util.DeferredStructurePlan120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/** Deterministic, region-spaced shipwreck generation for oceans and beaches. */
public final class OceanStructureHandler {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Shipwrecks");
    /** Minecraft 1.20.2 shipwreck structure-set random-spread placement. */
    private static final int REGION_SIZE_CHUNKS = 24;
    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Post event) {
        if (event.world.provider.dimensionId != 0
                || !event.world.getWorldInfo().isMapFeaturesEnabled()) return;
        int chunkX = floorDiv(event.chunkX, 16);
        int chunkZ = floorDiv(event.chunkZ, 16);
        int regionX=floorDiv(chunkX,REGION_SIZE_CHUNKS),regionZ=floorDiv(chunkZ,REGION_SIZE_CHUNKS);
        for(int rx=regionX-1;rx<=regionX+1;rx++)for(int rz=regionZ-1;rz<=regionZ+1;rz++)
            tryDeferredStart(event.world,chunkX,chunkZ,rx,rz);
    }

    /** Replays starts missed because neighbouring chunks were not resident during their first decoration. */
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event){
        if(!(event.world instanceof World))return;
        World world=(World)event.world;
        if(world.isRemote||world.provider.dimensionId!=0||!world.getWorldInfo().isMapFeaturesEnabled()
                ||!event.getChunk().isTerrainPopulated)return;
        int chunkX=event.getChunk().xPosition,chunkZ=event.getChunk().zPosition;
        int regionX=floorDiv(chunkX,REGION_SIZE_CHUNKS),regionZ=floorDiv(chunkZ,REGION_SIZE_CHUNKS);
        for(int rx=regionX-1;rx<=regionX+1;rx++)for(int rz=regionZ-1;rz<=regionZ+1;rz++)
            tryDeferredStart(world,chunkX,chunkZ,rx,rz);
    }

    private static void tryDeferredStart(World world,int decoratingChunkX,int decoratingChunkZ,
                                         int regionX,int regionZ){
        int[] candidate=StructurePlacement120.candidate(world.getSeed(),regionX,regionZ,
                StructurePlacement120.SHIPWRECK);
        int centerX=(candidate[0]<<4)+8,centerZ=(candidate[1]<<4)+8;
        // Futura stores the actual 3-D-selected surface biome in the chunk.
        // WorldChunkManager#getBiomeGenAt still represents a generation query,
        // so only classify a deferred start after its centre chunk is resident.
        if(!WorldSafety.isChunkLoaded(world,candidate[0],candidate[1]))return;
        BiomeGenBase biome = world.getBiomeGenForCoords(centerX, centerZ);
        boolean beach = isBeach(biome);
        if (!beach && !isOcean(biome)) return;
        Random structureRandom=StructurePlacement120.randomAfterCandidate(world.getSeed(),regionX,regionZ,
                StructurePlacement120.SHIPWRECK);
        String[] choices = beach
                ? WorldGenVanillaShipwreck.BEACHED_TEMPLATES
                : WorldGenVanillaShipwreck.OCEAN_TEMPLATES;
        String template = choices[structureRandom.nextInt(choices.length)];
        WorldGenVanillaShipwreck generator = new WorldGenVanillaShipwreck(
                template, structureRandom.nextInt(4));

        int startX = centerX - generator.getWidth() / 2;
        int startZ = centerZ - generator.getDepth() / 2;
        int maxX=startX+generator.getWidth()-1,maxZ=startZ+generator.getDepth()-1;
        if(!DeferredStructurePlan120.chunkIntersects(decoratingChunkX,decoratingChunkZ,
                startX,startZ,maxX,maxZ))return;
        if(AquaticStructureData.isGeneratedTarget(world,AquaticStructureData.SHIPWRECK,centerX,centerZ))return;
        // Decoration must never request a neighbouring chunk. In 1.7.10 that
        // recursively starts population and ends in "Already decorating!!".
        if (!isAreaReady(world, startX, startZ, generator.getWidth(), generator.getDepth(),
                decoratingChunkX, decoratingChunkZ)) return;
        int baseY = beach
                ? findBeachBase(world, startX, startZ, generator.getWidth(), generator.getDepth(),
                        generator.getHeight(), structureRandom)
                : findOceanBase(world, startX, startZ, generator.getWidth(), generator.getDepth());
        if (baseY >= 2 && generator.generate(world, structureRandom, startX, baseY, startZ)) {
            AquaticStructureData.record(world, AquaticStructureData.SHIPWRECK, centerX, centerZ);
            LOGGER.info("Generated {} shipwreck '{}' at {},{},{} in biome '{}'",
                    beach ? "beached" : "ocean", template, startX, baseY, startZ, biome.biomeName);
        }
    }

    /** Nearest deterministic shipwreck start used when no generated shipwreck is indexed yet. */
    public static int[] findNearestShipwreckCandidate(World world, int blockX, int blockZ) {
        int sourceChunkX = floorDiv(blockX, 16);
        int sourceChunkZ = floorDiv(blockZ, 16);
        int sourceRegionX = floorDiv(sourceChunkX, REGION_SIZE_CHUNKS);
        int sourceRegionZ = floorDiv(sourceChunkZ, REGION_SIZE_CHUNKS);
        int[] nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (int rx = sourceRegionX - 5; rx <= sourceRegionX + 5; rx++) {
            for (int rz = sourceRegionZ - 5; rz <= sourceRegionZ + 5; rz++) {
                int[] candidate=StructurePlacement120.candidate(world.getSeed(),rx,rz,
                        StructurePlacement120.SHIPWRECK);
                int x = candidate[0] * 16 + 8;
                int z = candidate[1] * 16 + 8;
                BiomeGenBase biome = world.getWorldChunkManager().getBiomeGenAt(x, z);
                if (!isBeach(biome) && !isOcean(biome)) continue;
                long dx = (long)x - blockX;
                long dz = (long)z - blockZ;
                long distance = dx * dx + dz * dz;
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = new int[] {x, z};
                }
            }
        }
        return nearest;
    }

    private static boolean isBeach(BiomeGenBase biome) {
        if (biome == BiomeGenBase.stoneBeach) return false;
        return biome == BiomeGenBase.beach || biome == BiomeGenBase.coldBeach
                || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.BEACH);
    }

    /**
     * Explicit vanilla coverage avoids depending on a modpack's BiomeDictionary
     * bootstrap order. The dictionary fallback keeps compatible modded oceans,
     * while the two extended-ID warm oceans are accepted directly.
     */
    private static boolean isOcean(BiomeGenBase biome) {
        return biome == BiomeGenBase.ocean
                || biome == BiomeGenBase.deepOcean
                || biome == BiomeGenBase.frozenOcean
                || biome == OceanBiomeRegistry.warmOcean
                || biome == OceanBiomeRegistry.deepWarmOcean
                || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.OCEAN);
    }

    private static int findBeachBase(World world, int x, int z, int width, int depth,
                                     int height, Random random) {
        int minimum = Integer.MAX_VALUE;
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int top = WorldSafety.getSafeTopBlock(world, x + dx, z + dz) + 1;
                if (top <= 1) return -1;
                minimum = Math.min(minimum, top);
            }
        }
        // ShipwreckStructure.Piece lowers beached templates by half their
        // height plus the reference random 0..2 burial offset.
        return minimum - height / 2 - random.nextInt(3);
    }

    /** Averages the complete rotated footprint, matching OCEAN_FLOOR_WG placement. */
    private static int findOceanBase(World world, int x, int z, int width, int depth) {
        int total = 0;
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int floor = findSeaFloor(world, x + dx, z + dz);
                if (floor < 1 || !isWater(WorldSafety.getSafeBlock(world, x + dx, floor + 1, z + dz))) {
                    return -1;
                }
                total += floor;
            }
        }
        return total / (width * depth);
    }

    private static int findSeaFloor(World world, int x, int z) {
        if (!WorldSafety.isChunkLoaded(world, x >> 4, z >> 4)) return -1;
        int top=Math.min(world.getActualHeight()-2,
                Math.max(2,WorldSafety.getSafeTopBlock(world,x,z)));
        for (int y = top; y > 1; y--) {
            Block block = WorldSafety.getSafeBlock(world, x, y, z);
            if (block != Blocks.air && !isWater(block)
                    && block != Blocks.ice && block != Blocks.packed_ice) return y;
        }
        return -1;
    }

    private static boolean isAreaReady(World world, int x, int z, int width, int depth,
                                       int activeChunkX, int activeChunkZ) {
        int maxX = x + width - 1, maxZ = z + depth - 1;
        for (int chunkX = x >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = z >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                if (!WorldSafety.isChunkTerrainReady(world, chunkX, chunkZ,
                        activeChunkX, activeChunkZ)) return false;
            }
        }
        return true;
    }

    /** Includes GT6 sea, river and dirty water without a hard GT class dependency. */
    private static boolean isWater(Block block) {
        return block == Blocks.water || block == Blocks.flowing_water
                || block.getMaterial() == Material.water;
    }

    private static int floorDiv(int value, int divisor) {
        int result = value / divisor;
        return value < 0 && value % divisor != 0 ? result - 1 : result;
    }
}
