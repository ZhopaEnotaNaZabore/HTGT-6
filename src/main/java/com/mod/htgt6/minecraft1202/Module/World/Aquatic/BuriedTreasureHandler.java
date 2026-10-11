package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Data.Aquatic.AquaticStructureData;
import com.mod.htgt6.common.util.WorldSafety;
import com.mod.htgt6.common.util.StructurePlacement120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/** Deterministic 1.13-style buried treasure beneath beach terrain. */
public final class BuriedTreasureHandler {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-BuriedTreasure");
    private static final long SALT = 10387320L;
    public static final int EXPLORATION_MAP_SEARCH_RADIUS = 50;

    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Post event) {
        if (event.world.isRemote || event.world.provider.dimensionId != 0
                || !event.world.getWorldInfo().isMapFeaturesEnabled()) return;
        int chunkX = floorDiv(event.chunkX, 16);
        int chunkZ = floorDiv(event.chunkZ, 16);
        tryGenerate(event.world,chunkX,chunkZ);
    }

    /** Generates deterministic treasures in already-populated chunks which predate the handler. */
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event){
        if(!(event.world instanceof World))return;
        World world=(World)event.world;
        if(world.isRemote||world.provider.dimensionId!=0||!world.getWorldInfo().isMapFeaturesEnabled()
                ||!event.getChunk().isTerrainPopulated)return;
        tryGenerate(world,event.getChunk().xPosition,event.getChunk().zPosition);
    }

    private static void tryGenerate(World world,int chunkX,int chunkZ){
        int[] candidate = candidateForChunk(world, chunkX, chunkZ);
        if (candidate == null) return;
        if(AquaticStructureData.isGeneratedTarget(world,AquaticStructureData.BURIED_TREASURE,
                candidate[0],candidate[1]))return;
        BiomeGenBase biome = world.getBiomeGenForCoords(candidate[0], candidate[1]);
        if (!isBeach(biome)) return;
        int y = findBurialY(world, candidate[0], candidate[1]);
        if (y < 2) return;
        if (world.getBlock(candidate[0], y, candidate[1]) == Blocks.chest) {
            AquaticStructureData.record(world,AquaticStructureData.BURIED_TREASURE,candidate[0],candidate[1]);
            return;
        }

        prepareBurial(world, candidate[0], y, candidate[1]);
        if(!world.setBlock(candidate[0], y, candidate[1], Blocks.chest, 0, 2))return;
        TileEntity tile = world.getTileEntity(candidate[0], y, candidate[1]);
        if (!(tile instanceof TileEntityChest)) return;
        Random lootRandom = new Random(world.getSeed()
                ^ candidate[0] * 341873128712L ^ candidate[1] * 132897987541L ^ SALT);
        TileEntityChest chest = (TileEntityChest)tile;
        AquaticChestLoot120.fillBuriedTreasure(chest, lootRandom);

        // GT6 may replace the vanilla chest in the loot hook. The target is
        // its block coordinate, so the replacement remains the same treasure.
        AquaticStructureData.record(world, AquaticStructureData.BURIED_TREASURE,
                candidate[0], candidate[1]);
        LOGGER.info("Generated buried treasure at {},{},{} in biome '{}'",
                candidate[0], y, candidate[1], biome.biomeName);
    }

    /** Returns the exact candidate that its chunk decorator will generate. */
    public static int[] findNearestBuriedTreasureCandidate(World world, int blockX, int blockZ) {
        int sourceChunkX = floorDiv(blockX, 16);
        int sourceChunkZ = floorDiv(blockZ, 16);
        int[] nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (int chunkX = sourceChunkX - EXPLORATION_MAP_SEARCH_RADIUS;
             chunkX <= sourceChunkX + EXPLORATION_MAP_SEARCH_RADIUS; chunkX++) {
            for (int chunkZ = sourceChunkZ - EXPLORATION_MAP_SEARCH_RADIUS;
                 chunkZ <= sourceChunkZ + EXPLORATION_MAP_SEARCH_RADIUS; chunkZ++) {
                int[] candidate = candidateForChunk(world, chunkX, chunkZ);
                if (candidate == null) continue;
                if (!isBeach(world.getWorldChunkManager().getBiomeGenAt(candidate[0], candidate[1]))) continue;
                long dx = (long)candidate[0] - blockX;
                long dz = (long)candidate[1] - blockZ;
                long distance = dx * dx + dz * dz;
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = candidate;
                }
            }
        }
        return nearest;
    }

    private static int[] candidateForChunk(World world, int chunkX, int chunkZ) {
        // 1.20.2 LEGACY_TYPE_2 ignores the JSON salt and uses the historical
        // buried-treasure salt with one 1% frequency test per chunk.
        if (!StructurePlacement120.buriedTreasurePasses(world.getSeed(),chunkX,chunkZ)) return null;
        return new int[] {(chunkX << 4) + 9, (chunkZ << 4) + 9};
    }

    private static int findBurialY(World world, int x, int z) {
        if (!WorldSafety.isChunkLoaded(world, x >> 4, z >> 4)) return -1;
        // Heightmaps return the first position above the surface; WorldSafety returns the solid block itself.
        int top = Math.min(world.getActualHeight() - 2, WorldSafety.getSafeTopBlock(world, x, z)+1);
        for (int y = top; y > 1; y--) {
            Block below = WorldSafety.getSafeBlock(world, x, y - 1, z);
            if (isFoundation(below, world.getBlockMetadata(x, y - 1, z))) return y;
        }
        return -1;
    }

    /** Reference buried-treasure support shell around the chest position. */
    private static void prepareBurial(World world, int x, int y, int z) {
        Block current = WorldSafety.getSafeBlock(world, x, y, z);
        int currentMeta = world.getBlockMetadata(x, y, z);
        Block below = WorldSafety.getSafeBlock(world, x, y - 1, z);
        int belowMeta = world.getBlockMetadata(x, y - 1, z);
        Block cover = isReplaceable(current) ? Blocks.sand : current;
        int coverMeta = isReplaceable(current) ? 0 : currentMeta;
        int[][] directions = {{0,1,0},{0,-1,0},{1,0,0},{-1,0,0},{0,0,1},{0,0,-1}};
        for (int[] direction : directions) {
            int nx=x+direction[0], ny=y+direction[1], nz=z+direction[2];
            Block neighbour=WorldSafety.getSafeBlock(world,nx,ny,nz);
            if (!isReplaceable(neighbour)) continue;
            Block neighbourBelow=WorldSafety.getSafeBlock(world,nx,ny-1,nz);
            boolean useFoundation=direction[1] != 1 && isReplaceable(neighbourBelow);
            Block replacement=useFoundation ? below : cover;
            world.setBlock(nx,ny,nz,replacement,useFoundation ? belowMeta : coverMeta,2);
        }
    }

    private static boolean isFoundation(Block block, int metadata) {
        // Reference accepts sandstone, stone, granite, diorite and andesite.
        return block == Blocks.sandstone || block == Blocks.stone
                || block == BountifulContent.stone && (metadata == 0 || metadata == 2 || metadata == 4);
    }

    private static boolean isReplaceable(Block block) {
        return block == Blocks.air || block == Blocks.water || block == Blocks.flowing_water
                || block == Blocks.lava || block == Blocks.flowing_lava;
    }

    private static boolean isBeach(BiomeGenBase biome) {
        if (biome == BiomeGenBase.stoneBeach) return false;
        return biome == BiomeGenBase.beach || biome == BiomeGenBase.coldBeach
                || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.BEACH);
    }

    private static int floorDiv(int value, int divisor) {
        int result = value / divisor;
        return value < 0 && value % divisor != 0 ? result - 1 : result;
    }
}
