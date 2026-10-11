package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelp;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaTreeFeatureRuntime120;
import com.mod.htgt6.minecraft1202.Module.Data.Aquatic.AquaticStructureData;
import com.mod.htgt6.common.util.WorldSafety;
import com.mod.htgt6.common.util.StructurePlacement120;
import com.mod.htgt6.common.util.DeferredStructurePlan120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/** Ocean Ruins plus noise-patched 1.20-style kelp forests. */
public final class OceanAquaticHandler {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-OceanAquatic");
    private static final int RUIN_SPACING = 20;
    /** Covers the farthest 23-block satellite center plus every official small-ruin footprint. */
    private static final int RUIN_REPLAY_RADIUS = 40;

    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Post event) {
        if (event.world.provider.dimensionId != 0) return;
        int centerX = event.chunkX + 8;
        int centerZ = event.chunkZ + 8;
        BiomeGenBase biome = event.world.getBiomeGenForCoords(centerX, centerZ);
        int chunkX = floorDiv(event.chunkX, 16);
        int chunkZ = floorDiv(event.chunkZ, 16);
        if(event.world.getWorldInfo().isMapFeaturesEnabled()){
            int regionX=floorDiv(chunkX,RUIN_SPACING),regionZ=floorDiv(chunkZ,RUIN_SPACING);
            for(int rx=regionX-1;rx<=regionX+1;rx++)for(int rz=regionZ-1;rz<=regionZ+1;rz++)
                tryDeferredRuin(event.world,chunkX,chunkZ,rx,rz);
        }
        if (!isOcean(biome)) return;
        // Futura executes these placed features in its ordered decorator with their own seeds.
        if(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120.isFutura(event.world))return;
        generateKelp(event.world, chunkX, chunkZ, event.chunkX, event.chunkZ, biome);
        generateMarinePlants(event.world, chunkX, chunkZ, event.chunkX, event.chunkZ, biome);
    }

    /** Catch-up for populated chunks loaded after an earlier deferred ruin could not see its full area. */
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event){
        if(!(event.world instanceof World))return;
        World world=(World)event.world;
        if(world.isRemote||world.provider.dimensionId!=0||!world.getWorldInfo().isMapFeaturesEnabled()
                ||!event.getChunk().isTerrainPopulated)return;
        int chunkX=event.getChunk().xPosition,chunkZ=event.getChunk().zPosition;
        int regionX=floorDiv(chunkX,RUIN_SPACING),regionZ=floorDiv(chunkZ,RUIN_SPACING);
        for(int rx=regionX-1;rx<=regionX+1;rx++)for(int rz=regionZ-1;rz<=regionZ+1;rz++)
            tryDeferredRuin(world,chunkX,chunkZ,rx,rz);
    }

    private static void tryDeferredRuin(World world,int decoratingChunkX,int decoratingChunkZ,
                                        int regionX,int regionZ){
        int[] candidate=StructurePlacement120.candidate(world.getSeed(),regionX,regionZ,
                StructurePlacement120.OCEAN_RUIN);
        int centerX=(candidate[0]<<4)+8,centerZ=(candidate[1]<<4)+8;
        if(!DeferredStructurePlan120.chunkIntersectsRadius(decoratingChunkX,decoratingChunkZ,
                centerX,centerZ,RUIN_REPLAY_RADIUS))return;
        if(AquaticStructureData.isGeneratedTarget(world,AquaticStructureData.RUIN,centerX,centerZ))return;
        if(!WorldSafety.isChunkLoaded(world,candidate[0],candidate[1]))return;
        BiomeGenBase biome=world.getBiomeGenForCoords(centerX,centerZ);
        if(!isOcean(biome)||!isReplayAreaReady(world,centerX,centerZ,RUIN_REPLAY_RADIUS,
                decoratingChunkX,decoratingChunkZ))return;
        Random random=StructurePlacement120.randomAfterCandidate(world.getSeed(),regionX,regionZ,
                StructurePlacement120.OCEAN_RUIN);
        generateRuinComplex(world,random,centerX,centerZ,biome);
    }

    private static void generateRuinComplex(World world, Random random, int centerX, int centerZ,
                                            BiomeGenBase biome) {
        boolean warm = usesWarmRuins(biome);
        boolean large = random.nextFloat() < 0.30F;
        int floor = findSeaFloor(world, centerX, centerZ);
        if (!isSubmerged(world, centerX, floor, centerZ)) return;
        RuinResult main = generateRuinAt(world, random, centerX, floor, centerZ, warm, large);
        if (main == null) return;
        AquaticStructureData.record(world, AquaticStructureData.RUIN, centerX, centerZ);

        int pieces = 1;
        // Vanilla's 0.9 cluster probability is applied to large ruins.
        if (large && random.nextFloat() < 0.90F) {
            int satellites = 4 + random.nextInt(5);
            for (int i = 0; i < satellites; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                int distance = 10 + random.nextInt(14);
                int pieceCenterX = centerX + (int)Math.round(Math.cos(angle) * distance);
                int pieceCenterZ = centerZ + (int)Math.round(Math.sin(angle) * distance);
                BiomeGenBase pieceBiome = world.getBiomeGenForCoords(pieceCenterX, pieceCenterZ);
                if (!isOcean(pieceBiome)) continue;

                int pieceFloor = findSeaFloor(world, pieceCenterX, pieceCenterZ);
                if (!isSubmerged(world, pieceCenterX, pieceFloor, pieceCenterZ)) continue;
                if (generateRuinAt(world, random, pieceCenterX, pieceFloor, pieceCenterZ,
                        usesWarmRuins(pieceBiome), false) != null) pieces++;
            }
        }

        LOGGER.info("Generated {} ocean ruin complex '{}' with {} piece(s) at {},{},{} in biome '{}'",
                warm ? "warm" : "cold", main.template, pieces, main.x, main.y, main.z, biome.biomeName);
    }

    /**
     * Cold ruins are three aligned vanilla layers: brick at main integrity,
     * cracked at 0.7 and mossy at 0.5. Warm ruins use one sandstone template.
     */
    private static RuinResult generateRuinAt(World world, Random random, int centerX, int floor, int centerZ,
                                              boolean warm, boolean large) {
        int rotation = random.nextInt(4);
        String baseName;
        String crackedName = null;
        String mossyName = null;
        if (warm) {
            String[] choices = large ? WorldGenOceanRuin.WARM_BIG : WorldGenOceanRuin.WARM_SMALL;
            baseName = choices[random.nextInt(choices.length)];
        } else if (large) {
            int[] variants = {1, 2, 3, 8};
            int number = variants[random.nextInt(variants.length)];
            baseName = "big_brick_" + number;
            crackedName = "big_cracked_" + number;
            mossyName = "big_mossy_" + number;
        } else {
            int number = 1 + random.nextInt(8);
            baseName = "brick_" + number;
            crackedName = "cracked_" + number;
            mossyName = "mossy_" + number;
        }

        WorldGenOceanRuin base = new WorldGenOceanRuin(baseName, rotation);
        int x = centerX - base.getWidth() / 2;
        int z = centerZ - base.getDepth() / 2;
        int placementFloor = findRuinFloor(world, x, z, base.getWidth(), base.getDepth());
        if (placementFloor < 2
                || !base.generate(world, random, x, placementFloor, z, large ? 0.90F : 0.80F)) return null;
        if (!warm) {
            new WorldGenOceanRuin(crackedName, rotation).generate(
                    world, random, x, placementFloor, z, 0.70F, false);
            new WorldGenOceanRuin(mossyName, rotation).generate(
                    world, random, x, placementFloor, z, 0.50F, false);
        }
        return new RuinResult(baseName, x, placementFloor, z);
    }

    private static void generateKelp(World world, int chunkX, int chunkZ, int blockX, int blockZ,
                                     BiomeGenBase biome) {
        Random random = new Random(world.getSeed() ^ chunkX * 341873128712L ^ chunkZ * 132897987541L
                ^ 0x4B454C50L);
        // 1.20 configured placement uses ratios 120 (cold) and 80 (warm).
        // Vanilla warm oceans are coral-dominated and do not generate kelp.
        int ratio=OceanBiomeRegistry.isWarm(biome)||isFrozenOcean(biome) ? 0
                : OceanBiomeRegistry.isLukewarm(biome) ? 80 : 120;
        int attempts=ratio==0?0:FuturaTreeFeatureRuntime120.noiseBasedCount(
                blockX,blockZ,80D,0D,ratio);
        for (int i = 0; i < attempts; i++) {
            int x = blockX + random.nextInt(16);
            int z = blockZ + random.nextInt(16);
            if (world.getBiomeGenForCoords(x,z)!=biome) continue;
            int floor = findSeaFloor(world, x, z);
            if (floor < 2 || !BlockKelp.isOpenWater(WorldSafety.getSafeBlock(world, x, floor + 1, z))) continue;

            int requestedHeight = 1 + random.nextInt(10);
            int placed = 0;
            for (int y = floor + 1; y <= floor + requestedHeight; y++) {
                if (!BlockKelp.isOpenWater(WorldSafety.getSafeBlock(world, x, y, z))) break;
                world.setBlock(x, y, z, OceanContent.kelpPlant, 0, 2);
                placed++;
            }
            if (placed > 0) {
                int topY = floor + placed;
                world.setBlock(x, topY, z, OceanContent.kelp, 8 + random.nextInt(4), 2);
            }
        }
    }

    private static void generateMarinePlants(World world, int chunkX, int chunkZ, int blockX, int blockZ,
                                              BiomeGenBase biome) {
        Random random = new Random(world.getSeed() ^ chunkX * 67542527L ^ chunkZ * 13579117L ^ 0x504C414E54L);
        boolean warm = OceanBiomeRegistry.isWarm(biome);
        if(isFrozenOcean(biome))return;
        // Exact 1.20.2 configured/placed features: shallow oceans use the
        // 0.3 short config; deep oceans use the 0.8 tall config.
        int attempts=aquaticVegetationCount(biome);
        float tallProbability=isDeepOcean(biome)?.8F:.3F;
        for (int i = 0; i < attempts; i++) {
            int x = blockX + random.nextInt(16), z = blockZ + random.nextInt(16);
            if(world.getBiomeGenForCoords(x,z)!=biome)continue;
            int floor = findSeaFloor(world, x, z), y = floor + 1;
            if (floor < 2 || !BlockKelp.isOpenWater(WorldSafety.getSafeBlock(world, x, y, z))) continue;
            if (random.nextFloat()<tallProbability
                    && BlockKelp.isOpenWater(WorldSafety.getSafeBlock(world, x, y + 1, z))) {
                world.setBlock(x, y, z, OceanContent.marinePlant, 1, 2);
                world.setBlock(x, y + 1, z, OceanContent.marinePlant, 2, 2);
                continue;
            }
            world.setBlock(x, y, z, OceanContent.marinePlant, 0, 2);
        }
        if(warm){
            // warm_ocean_vegetation: noise_factor=400, ratio=20.
            int coralAttempts=FuturaTreeFeatureRuntime120.noiseBasedCount(
                    blockX,blockZ,400D,0D,20);
            for(int i=0;i<coralAttempts;i++)generateCoral(
                    world,random,blockX,blockZ,biome);
            // SeaPickleFeature(count=20) behind one in-square rarity_filter(16).
            if(random.nextInt(16)==0)generateSeaPickleCluster(world,random,blockX,blockZ,biome);
        }
    }

    private static void generateSeaPickleCluster(World world,Random random,int blockX,int blockZ,
                                                 BiomeGenBase biome){
        int originX=blockX+random.nextInt(16),originZ=blockZ+random.nextInt(16);
        for(int attempt=0;attempt<20;attempt++){
            int x=originX+random.nextInt(8)-random.nextInt(8);
            int z=originZ+random.nextInt(8)-random.nextInt(8);
            if(world.getBiomeGenForCoords(x,z)!=biome)continue;
            int floor=findSeaFloor(world,x,z),y=floor+1;
            if(floor>=2&&BlockKelp.isOpenWater(WorldSafety.getSafeBlock(world,x,y,z)))
                world.setBlock(x,y,z,OceanContent.marinePlant,8+random.nextInt(4),2);
        }
    }

    private static void generateCoral(World world,Random random,int blockX,int blockZ,
                                      BiomeGenBase biome){
        int x=blockX+random.nextInt(16),z=blockZ+random.nextInt(16);
        if(world.getBiomeGenForCoords(x,z)!=biome)return;
        int floor=findSeaFloor(world,x,z),y=floor+1;
        if(floor>=2&&BlockKelp.isOpenWater(WorldSafety.getSafeBlock(world,x,y,z))) {
            WorldGenCoralFeature120.generate(world,random,x,y,z,random.nextInt(5),random.nextInt(3));
        }
    }

    private static boolean isReplayAreaReady(World world,int centerX,int centerZ,int radius,
                                             int activeChunkX,int activeChunkZ){
        int minChunkX=(centerX-radius)>>4,maxChunkX=(centerX+radius)>>4;
        int minChunkZ=(centerZ-radius)>>4,maxChunkZ=(centerZ+radius)>>4;
        for(int chunkX=minChunkX;chunkX<=maxChunkX;chunkX++)for(int chunkZ=minChunkZ;chunkZ<=maxChunkZ;chunkZ++)
            if(!WorldSafety.isChunkTerrainReady(world,chunkX,chunkZ,activeChunkX,activeChunkZ))return false;
        return true;
    }

    /** Nearest deterministic ruin start used when no generated ruin is indexed yet. */
    public static int[] findNearestRuinCandidate(World world, int blockX, int blockZ) {
        int sourceChunkX = floorDiv(blockX, 16);
        int sourceChunkZ = floorDiv(blockZ, 16);
        int sourceRegionX = floorDiv(sourceChunkX, RUIN_SPACING);
        int sourceRegionZ = floorDiv(sourceChunkZ, RUIN_SPACING);
        int[] nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (int rx = sourceRegionX - 4; rx <= sourceRegionX + 4; rx++) {
            for (int rz = sourceRegionZ - 4; rz <= sourceRegionZ + 4; rz++) {
                int[] candidate=StructurePlacement120.candidate(world.getSeed(),rx,rz,
                        StructurePlacement120.OCEAN_RUIN);
                int x = candidate[0] * 16 + 8;
                int z = candidate[1] * 16 + 8;
                if (!isOcean(world.getWorldChunkManager().getBiomeGenAt(x, z))) continue;
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

    private static boolean isOcean(BiomeGenBase biome) {
        return biome == BiomeGenBase.ocean || biome == BiomeGenBase.deepOcean
                || biome == BiomeGenBase.frozenOcean
                || biome == OceanBiomeRegistry.warmOcean
                || biome == OceanBiomeRegistry.deepWarmOcean
                || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.OCEAN);
    }

    private static boolean isFrozenOcean(BiomeGenBase biome){
        return biome==BiomeGenBase.frozenOcean||biome==OceanBiomeRegistry.deepFrozenOcean;
    }

    private static boolean isDeepOcean(BiomeGenBase biome){
        return biome==BiomeGenBase.deepOcean||biome==OceanBiomeRegistry.deepWarmOcean
                ||biome==OceanBiomeRegistry.deepLukewarmOcean
                ||biome==OceanBiomeRegistry.deepColdOcean||biome==OceanBiomeRegistry.deepFrozenOcean;
    }

    public static int aquaticVegetationCount(BiomeGenBase biome){
        if(isFrozenOcean(biome))return 0;
        if(biome==OceanBiomeRegistry.deepColdOcean)return 40;
        if(OceanBiomeRegistry.isWarm(biome)||OceanBiomeRegistry.isLukewarm(biome))return 80;
        if(biome==OceanBiomeRegistry.coldOcean)return 32;
        return 48;
    }

    private static boolean usesWarmRuins(BiomeGenBase biome) {
        return OceanBiomeRegistry.usesWarmRuins(biome)
                || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.HOT);
    }

    private static int findSeaFloor(World world, int x, int z) {
        // Never call getTopSolidOrLiquidBlock for an unloaded neighbour while
        // BiomeDecorator is active. Doing so recursively provides/decorates the
        // neighbour and Forge aborts with "Already decorating!!".
        if (!WorldSafety.isChunkLoaded(world, x >> 4, z >> 4)) return -1;
        int top = Math.min(world.getActualHeight() - 2,
                Math.max(2, WorldSafety.getSafeTopBlock(world, x, z)));
        for (int y = top; y > 1; y--) {
            Block block = WorldSafety.getSafeBlock(world, x, y, z);
            if (block != Blocks.air && !BlockKelp.isWater(block)
                    && block != Blocks.ice && block != Blocks.packed_ice
                    && !BlockKelp.isKelp(block)) return y;
        }
        return -1;
    }

    private static boolean isSubmerged(World world, int x, int floor, int z) {
        return floor > 1 && BlockKelp.isWater(WorldSafety.getSafeBlock(world, x, floor + 1, z))
                && BlockKelp.isWater(WorldSafety.getSafeBlock(world, x, floor + 3, z));
    }

    /** Full-footprint port of OceanRuinPiece#getHeight slope correction. */
    private static int findRuinFloor(World world, int x, int z, int width, int depth) {
        int originFloor = findSeaFloor(world, x, z);
        if (!isSubmerged(world, x, originFloor, z)) return -1;
        int minimum = Integer.MAX_VALUE;
        int lowColumns = 0;
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int floor = findSeaFloor(world, x + dx, z + dz);
                if (!isSubmerged(world, x + dx, floor, z + dz)) return -1;
                minimum = Math.min(minimum, floor);
                if (floor < originFloor - 2) lowColumns++;
            }
        }
        // The reference keeps the origin height on ordinary slopes and drops
        // to minimum+1 only when enough columns fall more than two blocks.
        return originFloor - minimum > 2 && lowColumns > width - 3
                ? minimum + 1 : originFloor + 1;
    }

    private static int floorDiv(int value, int divisor) {
        int result = value / divisor;
        return value < 0 && value % divisor != 0 ? result - 1 : result;
    }

    private static final class RuinResult {
        final String template;
        final int x, y, z;
        RuinResult(String template, int x, int y, int z) {
            this.template = template; this.x = x; this.y = y; this.z = z;
        }
    }
}
