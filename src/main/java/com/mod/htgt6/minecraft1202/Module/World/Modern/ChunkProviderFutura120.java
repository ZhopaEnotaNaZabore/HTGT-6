package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.asm.HTGT6Transformer;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.AquiferSampler120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.CaveDensityRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.NoiseChunk120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.CaveNoiseChunk120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceEstimator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Futura-only terrain, surface, cave and Forge/GT6 population pipeline. */
public final class ChunkProviderFutura120 implements IChunkProvider {
    private static final Logger LOGGER=LogManager.getLogger("HTGT6-Futura120");
    private static int profiledChunks;
    private static final AtomicInteger PROFILED_POPULATIONS=new AtomicInteger();
    private static final AtomicLong VILLAGE_POPULATION_CALLS=new AtomicLong();
    private static final AtomicLong ANCIENT_CITY_POPULATION_CALLS=new AtomicLong();
    /** Logical translated 1.20.2 build height; instance generation remains readiness-gated. */
    public static final int PHYSICAL_HEIGHT = com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.PHYSICAL_HEIGHT;
    public static final int SEA_LEVEL = FuturaMountainDensityRouter120.SEA_LEVEL;
    private static final int SOLID_DEEPSLATE_TOP = FuturaMountainDensityRouter120.modernToFuturaY(0);
    private static final int MIXED_DEEPSLATE_TOP = FuturaMountainDensityRouter120.modernToFuturaY(8);
    // DimensionType.WAY_BELOW_MIN_Y in 1.20.2.  Integer.MIN_VALUE is not an
    // interchangeable sentinel here: y - MIN_VALUE overflows and makes an
    // uninterrupted terrain column look like a cave ceiling to SurfaceRules.
    public static final int WAY_BELOW_MIN_Y_1202 = -536870656;
    private final World world;
    private final long seed;
    private final FuturaMountainDensityRouter120 terrain;
    private final FuturaWorldChunkManager120 biomeManager;
    private final FuturaSurfaceSystem120 surfaceSystem;
    private final CaveDensityRuntime120 caves;
    private final AquiferSampler120 aquifers;
    private final FuturaCarverRuntime120 carvers;
    private final FuturaBiomeDecorator120 biomeDecorator;
    private final FuturaOreVein120 oreVeins;
    private final boolean mapFeaturesEnabled;
    private final int generationHeight;
    private MapGenMineshaft mineshaftGenerator;
    private MapGenStronghold strongholdGenerator;
    private MapGenScatteredFeature scatteredFeatureGenerator;

    public ChunkProviderFutura120(World world) {
        if (!FuturaWorldType120.isFutura(world)) throw new IllegalArgumentException("Futura provider requires exact world type");
        this.world = world;
        generationHeight=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.generatorHeight(world);
        seed = world.getSeed();
        terrain = new FuturaMountainDensityRouter120(seed);
        biomeManager=(FuturaWorldChunkManager120)world.getWorldChunkManager();
        surfaceSystem = new FuturaSurfaceSystem120(seed);
        caves = new CaveDensityRuntime120(seed);
        aquifers = new AquiferSampler120(seed, SEA_LEVEL,
                CaveDensityRuntime120.modernToFuturaY(-54), terrain, generationHeight - 1);
        carvers = new FuturaCarverRuntime120(seed,aquifers);
        biomeDecorator = new FuturaBiomeDecorator120(terrain);
        oreVeins = new FuturaOreVein120(seed);
        mapFeaturesEnabled = world.getWorldInfo().isMapFeaturesEnabled();
        mineshaftGenerator = (MapGenMineshaft)TerrainGen.getModdedMapGen(
                new FuturaStructureGenerators120.Mineshaft(), InitMapGenEvent.EventType.MINESHAFT);
        strongholdGenerator = (MapGenStronghold)TerrainGen.getModdedMapGen(
                new FuturaStructureGenerators120.Stronghold(terrain), InitMapGenEvent.EventType.STRONGHOLD);
        scatteredFeatureGenerator = (MapGenScatteredFeature)TerrainGen.getModdedMapGen(
                new FuturaStructureGenerators120.Scattered(), InitMapGenEvent.EventType.SCATTERED_FEATURE);
    }

    @Override public Chunk provideChunk(int chunkX, int chunkZ) {
        requireFutura();
        long started=System.nanoTime();
        boolean profile=profiledChunks<8;
        if(profile)profiledChunks++;
        GenerationTimings timings=profile?new GenerationTimings():null;
        Block[] blocks = new Block[16 * 16 * generationHeight];
        byte[] metadata = new byte[blocks.length];
        int[] surfaces = new int[256];
        double[] densityColumn=new double[generationHeight];
        long phaseStarted=System.nanoTime();
        CaveNoiseChunk120 noiseChunk=terrain.createNoiseChunk(chunkX,chunkZ,generationHeight,caves);
        FuturaOreVein120.Chunk veinChunk=oreVeins.chunk(chunkX,chunkZ,generationHeight);
        if(profile)timings.noiseGrid=System.nanoTime()-phaseStarted;
        phaseStarted=System.nanoTime();
        for (int localX = 0; localX < 16; localX++) for (int localZ = 0; localZ < 16; localZ++) {
            int biomeIndex = localX + localZ * 16;
            int worldX = (chunkX << 4) + localX, worldZ = (chunkZ << 4) + localZ;
            surfaces[biomeIndex] = generateBaseColumn(blocks, localX, localZ, worldX, worldZ,
                    noiseChunk,veinChunk,metadata,densityColumn,timings);
        }
        // 1.20.2 samples the 3D noise biome at the block SurfaceSystem is
        // replacing.  Storing a fixed modern-Y=0 slice in 1.7 made mountain
        // identities bleed across ocean boundaries.  Collapse the 3D biome
        // volume only after the real surface of every column is known.
        phaseStarted=System.nanoTime();
        BiomeGenBase[] biomes=biomeManager.loadSurfaceBiomeData(null,chunkX<<4,chunkZ<<4,
                16,16,surfaces,terrain);
        if(profile)timings.biomes=System.nanoTime()-phaseStarted;
        // GT strata precede caves and surfaces, matching the raw-array vanilla ASM path.
        phaseStarted=System.nanoTime();
        if (HTGT6Config.FIX_GT6_STONE_GEN_LAG)
            HTGT6Transformer.generateGTFuturaStoneLayersInChunkArray(world, chunkX, chunkZ, blocks, metadata, biomes);
        if(profile)timings.gt6=System.nanoTime()-phaseStarted;
        phaseStarted=System.nanoTime();
        int originX = chunkX << 4, originZ = chunkZ << 4;
        int preliminaryNorthWest = clippedPreliminarySurface(originX, originZ);
        int preliminaryNorthEast = clippedPreliminarySurface(originX + 16, originZ);
        int preliminarySouthWest = clippedPreliminarySurface(originX, originZ + 16);
        int preliminarySouthEast = clippedPreliminarySurface(originX + 16, originZ + 16);
        for (int localX = 0; localX < 16; localX++) for (int localZ = 0; localZ < 16; localZ++) {
            int biomeIndex = localX + localZ * 16;
            int worldX = (chunkX << 4) + localX, worldZ = (chunkZ << 4) + localZ;
            applySurface(blocks, metadata, surfaces, localX, localZ, biomes[biomeIndex], worldX, worldZ,
                    preliminaryNorthWest, preliminaryNorthEast, preliminarySouthWest, preliminarySouthEast);
        }
        if (HTGT6Config.FIX_GT6_WATER_TICK_UPDATE) HTGT6Transformer.generateGTFuturaWaterInBiomeArray(blocks, biomes);
        // ChunkStatus.SURFACE precedes CARVERS in 1.20.2. Still independent of structures.
        carvers.carve(blocks,metadata,generationHeight,chunkX,chunkZ);
        if(profile)timings.surface=System.nanoTime()-phaseStarted;
        phaseStarted=System.nanoTime();
        if (mapFeaturesEnabled) {
            runRawMapGenerators(blocks,chunkX,chunkZ);
        }
        if(profile)timings.mapgen=System.nanoTime()-phaseStarted;
        phaseStarted=System.nanoTime();
        Chunk chunk = new Chunk(world, blocks, metadata, chunkX, chunkZ);
        int[] ids = HTGT6Transformer.getExtendedBiomeArray(chunk);
        for (int i = 0; i < 256; i++) ids[i] = biomes[i].biomeID;
        chunk.generateSkylightMap();
        if(profile){timings.chunk=System.nanoTime()-phaseStarted;LOGGER.info(
                "Futura chunk {},{} timings: grid={}ms, interpolate={}ms, caves={}ms, aquifer/material={}ms, biomes={}ms, GT6={}ms, surface={}ms, mapgen={}ms, chunk/skylight={}ms, total={}ms",
                chunkX,chunkZ,millis(timings.noiseGrid),millis(timings.interpolate),
                millis(timings.caves),millis(timings.aquifer),millis(timings.biomes),
                millis(timings.gt6),millis(timings.surface),millis(timings.mapgen),
                millis(timings.chunk),millis(System.nanoTime()-started));}
        return chunk;
    }

    private void runRawMapGenerators(Block[] blocks,int chunkX,int chunkZ){
        // Owned structure generators only prepare starts here; they never edit the
        // raw terrain array. Retain 256-stride staging solely for Forge replacements.
        boolean legacyArrays=!(mineshaftGenerator instanceof FuturaStructureGenerators120.Mineshaft)
                ||!(strongholdGenerator instanceof FuturaStructureGenerators120.Stronghold)
                ||!(scatteredFeatureGenerator instanceof FuturaStructureGenerators120.Scattered);
        Block[] mapgenBlocks=legacyArrays?blocks:null;
        if(legacyArrays&&generationHeight!=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.LEGACY_HEIGHT){
            mapgenBlocks=new Block[16*16*com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.LEGACY_HEIGHT];
            copyLowerLegacyVolume(blocks,generationHeight,mapgenBlocks,false);
        }
        mineshaftGenerator.func_151539_a(this,world,chunkX,chunkZ,mapgenBlocks);
        strongholdGenerator.func_151539_a(this,world,chunkX,chunkZ,mapgenBlocks);
        scatteredFeatureGenerator.func_151539_a(this,world,chunkX,chunkZ,mapgenBlocks);
        if(mapgenBlocks!=null&&mapgenBlocks!=blocks)copyLowerLegacyVolume(blocks,generationHeight,mapgenBlocks,true);
    }

    public static void copyLowerLegacyVolume(Block[] extended,int height,Block[] legacy,boolean toExtended){
        int legacyHeight=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.LEGACY_HEIGHT;
        if(extended==null||legacy==null||height<legacyHeight||extended.length!=256*height
                ||legacy.length!=256*legacyHeight)throw new IllegalArgumentException("Invalid mapgen staging arrays");
        for(int column=0;column<256;column++){
            int extendedBase=column*height,legacyBase=column*legacyHeight;
            if(toExtended)System.arraycopy(legacy,legacyBase,extended,extendedBase,legacyHeight);
            else System.arraycopy(extended,extendedBase,legacy,legacyBase,legacyHeight);
        }
    }

    public static int rawArrayIndex(int height,int localX,int y,int localZ){
        if(height<256||localX<0||localX>=16||localZ<0||localZ>=16||y<0||y>=height)
            throw new IllegalArgumentException("Invalid raw chunk coordinate");
        return (localX*16+localZ)*height+y;
    }

    private int generateBaseColumn(Block[] blocks, int localX, int localZ, int x, int z,
                                   CaveNoiseChunk120 noiseChunk,FuturaOreVein120.Chunk veinChunk,byte[] metadata,
                                   double[] densities,GenerationTimings timings) {
        int base = (localX * 16 + localZ) * generationHeight;
        // Preserve the legacy y=0 sentinel; bedrock consumes that position.
        long phaseStarted=timings==null?0L:System.nanoTime();
        noiseChunk.fillColumn(localX,localZ,1,densities);
        if(timings!=null){timings.interpolate+=System.nanoTime()-phaseStarted;phaseStarted=System.nanoTime();}
        // Cave routing is already part of the shared 4x8x4 NoiseChunk grid.
        // Do not resample its complete NormalNoise graph for every block.
        if(timings!=null){timings.caves+=System.nanoTime()-phaseStarted;phaseStarted=System.nanoTime();}
        for (int y = 0; y < generationHeight; y++) {
            int index = base + y;
            if (surfaceSystem.verticalGradient("minecraft:bedrock_floor",x,y,z,-64,-59)) {
                blocks[index] = Blocks.bedrock; continue;
            }
            Block rock = baseRock(x, y, z);
            double routedDensity=densities[y];
            if (routedDensity > 0.0D) {
                FuturaOreVein120.State vein=veinChunk.sample(localX,y,localZ);
                blocks[index] = vein==null?rock:vein.block;
                if(vein!=null)metadata[index]=vein.metadata;
                continue;
            }
            AquiferSampler120.Result result = aquifers.sampleDensity(x,
                    CaveDensityRuntime120.futuraToModernY(y), z, routedDensity);
            switch (result.decision) {
                case SOLID:
                    FuturaOreVein120.State vein=veinChunk.sample(localX,y,localZ);
                    blocks[index]=vein==null?rock:vein.block;
                    if(vein!=null)metadata[index]=vein.metadata;
                    break;
                case WATER: blocks[index] = Backport120Content.aquiferWater; break;
                case LAVA: blocks[index] = Blocks.lava; break;
                default: blocks[index] = null; break;
            }
        }
        if(timings!=null)timings.aquifer+=System.nanoTime()-phaseStarted;
        // SurfaceSystem uses the final WORLD_SURFACE-like result, after caves
        // and aquifer material have replaced the preliminary terrain column.
        for (int y = generationHeight - 1; y > 0; y--)
            if (blocks[base + y] != null) return y;
        return 0;
    }

    private int clippedPreliminarySurface(int x, int z) {
        int surface = terrain.preliminarySurfaceLevel(x, z, generationHeight - 1);
        return surface == Integer.MAX_VALUE ? generationHeight - 1
                : Math.min(generationHeight - 1, surface);
    }

    private static final class GenerationTimings {
        long noiseGrid,interpolate,caves,aquifer,biomes,gt6,surface,mapgen,chunk;
    }

    /** Modern Y=0..8 deepslate blend mapped into Futura Y=64..72. */
    private Block baseRock(int x, int y, int z) {
        if (y < SOLID_DEEPSLATE_TOP) return Backport120Content.deepslate;
        if (y >= MIXED_DEEPSLATE_TOP) return Blocks.stone;
        return surfaceSystem.verticalGradient("minecraft:deepslate",x,y,z,0,8)
                ?Backport120Content.deepslate:Blocks.stone;
    }


    private void applySurface(Block[] blocks, byte[] metadata, int[] surfaceHeights,
                              int localX, int localZ, BiomeGenBase biome, int x, int z,
                              int preliminaryNorthWest, int preliminaryNorthEast,
                              int preliminarySouthWest, int preliminarySouthEast) {
        int base = (localX * 16 + localZ) * generationHeight;
        int biomeId=biome.biomeID;
        int extensionSurface=surfaceHeights[localX+localZ*16]+1;
        if(biomeId==165)surfaceSystem.erodedBadlandsExtension(blocks,base,x,z,
                extensionSurface,generationHeight);
        if(biome==BiomeGenBase.frozenOcean||biome==OceanBiomeRegistry.deepFrozenOcean)
            surfaceSystem.frozenOceanExtension(blocks,metadata,base,x,z,extensionSurface,
                    SEA_LEVEL,generationHeight,biome.getFloatTemperature(x,SEA_LEVEL,z)<.15F);
        int depth = surfaceSystem.surfaceDepth(x, z);
        int preliminarySurface = surfaceSystem.preliminarySurface(x, z,
                preliminaryNorthWest, preliminaryNorthEast, preliminarySouthWest, preliminarySouthEast, depth);
        FuturaSurfaceContext120 surface = new FuturaSurfaceContext120().setColumn(depth,
                surfaceSystem.surfaceSecondary(x, z), preliminarySurface,
                surfaceSystem.steep(localX, localZ, surfaceHeights));
        MutableDensityContext120 biomeSample=new MutableDensityContext120();
        int stoneDepthAbove = 0;
        int waterHeight = Integer.MIN_VALUE;
        int stoneSequenceBottom = Integer.MAX_VALUE;
        boolean primarySurfaceFound = false;
        boolean primarySurfaceSequence = false;
        for (int y = generationHeight - 1; y > 0; y--) {
            int index = base + y;
            Block block = blocks[index];
            if (block == null) {
                stoneDepthAbove = 0;
                waterHeight = Integer.MIN_VALUE;
                stoneSequenceBottom = Integer.MAX_VALUE;
                if (primarySurfaceFound) primarySurfaceSequence = false;
                continue;
            }
            Material material = block.getMaterial();
            if (material.isLiquid()) {
                if (waterHeight == Integer.MIN_VALUE) waterHeight = y + 1;
                stoneSequenceBottom = Integer.MAX_VALUE;
                if (primarySurfaceFound) primarySurfaceSequence = false;
                continue;
            }
            if (!primarySurfaceFound) {
                primarySurfaceFound = true;
                primarySurfaceSequence = true;
            }
            if (stoneSequenceBottom >= y) {
                stoneSequenceBottom = findStoneSequenceBottom(blocks, base, y);
            }
            stoneDepthAbove++;
            int stoneDepthBelow = y - stoneSequenceBottom + 1;
            surface.setBlock(y, stoneDepthAbove, stoneDepthBelow, waterHeight, false);
            // Vanilla applies the graph only to its exact default stone.  The
            // port additionally permits shifted deepslate and actual GT layer
            // hosts on the first external sequence, never arbitrary solids
            // such as iceberg packed ice or snow.
            if (!surface.mayApply(isSurfaceReplaceable(block,metadata[index],primarySurfaceSequence),
                    block == Blocks.bedrock || !material.isSolid())) continue;
            BiomeGenBase ruleBiome=biomeManager.getSurfaceBiomeAt(x,y,z,terrain,biomeSample);
            surface.setBlock(y, stoneDepthAbove, stoneDepthBelow, waterHeight,
                    ruleBiome.getFloatTemperature(x, y, z) < 0.15F);
            FuturaSurfaceRules120.State selected=FuturaOverworldSurfaceRules120.select(
                    surfaceSystem,surface,ruleBiome,x,z);
            if(selected!=null){blocks[index]=selected.block;metadata[index]=(byte)selected.metadata;}
        }
    }

    public static int findStoneSequenceBottom(Block[] blocks, int base, int y) {
        int bottom = WAY_BELOW_MIN_Y_1202;
        for (int belowY = y - 1; belowY > 0; belowY--) {
            Block below = blocks[base + belowY];
            if (below == null || below.getMaterial().isLiquid()) return belowY + 1;
        }
        return bottom;
    }

    public static boolean isSurfaceReplaceable(Block block,int metadata,boolean primarySurfaceSequence){
        if(block==null)return false;
        if(block==Blocks.stone)return true;
        if(!primarySurfaceSequence||block==Blocks.packed_ice||block==Blocks.snow)return false;
        if(block==Backport120Content.deepslate)return true;
        for(gregapi.worldgen.StoneLayer layer:gregapi.worldgen.StoneLayer.LAYERS)
            if(layer!=null&&layer.mStone==block&&layer.mMetaStone==metadata)return true;
        return false;
    }

    public static int estimateSurfaceHeight(long seed, int x, int z) {
        return FuturaSurfaceEstimator120.estimate(seed,x,z,PHYSICAL_HEIGHT-1);
    }

    static int estimateSurfaceHeight(FuturaMountainDensityRouter120 router,int x,int z) {
        return FuturaSurfaceEstimator120.estimate(router,x,z,PHYSICAL_HEIGHT-1);
    }

    static int estimateSurfaceHeight(FuturaMountainDensityRouter120 router,MutableDensityContext120 sample,int x,int z) {
        return FuturaSurfaceEstimator120.estimate(router,sample,x,z,PHYSICAL_HEIGHT-1);
    }

    private final FuturaPopulationQueue120 populationQueue=new FuturaPopulationQueue120();

    @Override public void populate(IChunkProvider provider, int chunkX, int chunkZ) {
        requireFutura();
        if(!populationQueue.begin(world,chunkX,chunkZ))return;
        boolean previousFalling=BlockFalling.fallInstantly;
        long populationStarted=System.nanoTime(),villageNanos=0L,ancientCityNanos=0L;
        BlockFalling.fallInstantly = true;
        try {
            for(int dx=-FuturaGenerationRegion120.READ_RADIUS;dx<=FuturaGenerationRegion120.READ_RADIUS;dx++)
                for(int dz=-FuturaGenerationRegion120.READ_RADIUS;dz<=FuturaGenerationRegion120.READ_RADIUS;dz++)
                    world.getChunkFromChunkCoords(chunkX+dx,chunkZ+dz);
            FuturaGenerationRegion120 region=new FuturaGenerationRegion120(world,chunkX,chunkZ);
            int x = chunkX << 4, z = chunkZ << 4;
            Random random = new Random(seed);
            long oddX = random.nextLong() / 2L * 2L + 1L, oddZ = random.nextLong() / 2L * 2L + 1L;
            random.setSeed((long)chunkX * oddX + (long)chunkZ * oddZ ^ seed);
            BiomeGenBase biome = world.getBiomeGenForCoords(x + 16, z + 16);
            boolean villageGenerated = false;
            MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(provider, world, random,
                    chunkX, chunkZ, false));
            if (mapFeaturesEnabled) {
                mineshaftGenerator.generateStructuresInChunk(world, random, chunkX, chunkZ);
                long villageStarted=System.nanoTime();
                VILLAGE_POPULATION_CALLS.incrementAndGet();
                villageGenerated = FuturaVillageGenerator120.populateChunkAndEnsureResidents(
                        world,chunkX,chunkZ,terrain);
                villageNanos=System.nanoTime()-villageStarted;
                strongholdGenerator.generateStructuresInChunk(world, random, chunkX, chunkZ);
                scatteredFeatureGenerator.generateStructuresInChunk(world, random, chunkX, chunkZ);
                long ancientCityStarted=System.nanoTime();
                ANCIENT_CITY_POPULATION_CALLS.incrementAndGet();
                FuturaAncientCityGenerator120.populateChunk(world,chunkX,chunkZ,terrain);
                ancientCityNanos=System.nanoTime()-ancientCityStarted;
            }
            biomeDecorator.decorate(region,this,provider,chunkX,chunkZ,generationHeight,villageGenerated);
            if (TerrainGen.populate(provider, world, random, chunkX, chunkZ,
                    villageGenerated, PopulateChunkEvent.Populate.EventType.ANIMALS))
                SpawnerAnimals.performWorldGenSpawning(world, biome, x + 8, z + 8, 16, 16, random);
            MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(provider, world, random,
                    chunkX, chunkZ, villageGenerated));
            if (mapFeaturesEnabled) {
                // Populate.Post is the last point at which third-party worldgen can
                // replace village blocks. Replaying the owned chunk slice here makes
                // every official NBT block independent of generator/event ordering.
                FuturaVillageGenerator120.restoreChunkAfterWorldGenerators(
                        world,chunkX,chunkZ,terrain);
            }
            int profileIndex=PROFILED_POPULATIONS.getAndIncrement();
            if(profileIndex<8)LOGGER.info(
                    "Futura population {},{} structure timings: village={}ms, ancient_city={}ms, total={}ms, calls={}/{}",
                    chunkX,chunkZ,millis(villageNanos),millis(ancientCityNanos),
                    millis(System.nanoTime()-populationStarted),VILLAGE_POPULATION_CALLS.get(),
                    ANCIENT_CITY_POPULATION_CALLS.get());
        } catch(RuntimeException|Error failure){
            Chunk failed=com.mod.htgt6.common.util.WorldSafety.getLoadedChunk(world,chunkX,chunkZ);
            if(failed!=null){failed.isTerrainPopulated=false;failed.setChunkModified();}
            throw failure;
        } finally { BlockFalling.fallInstantly=previousFalling;populationQueue.end(); }
        populationQueue.drain(world,provider);
    }

    private static long millis(long nanos){return nanos/1000000L;}
    private void requireFutura() { if (!FuturaWorldType120.isFutura(world)) throw new IllegalStateException("Futura provider attached to another world type"); }
    @Override public boolean chunkExists(int x, int z) { return true; }
    @Override public Chunk loadChunk(int x, int z) { return provideChunk(x, z); }
    @Override public boolean saveChunks(boolean all, IProgressUpdate progress) { return true; }
    @Override public void saveExtraData() { }
    @Override public boolean unloadQueuedChunks() { return false; }
    @Override public boolean canSave() { return true; }
    @Override public String makeString() { return "Futura120LevelSource"; }
    @Override public int getLoadedChunkCount() { return 0; }
    @Override public List getPossibleCreatures(EnumCreatureType type, int x, int y, int z) {
        if(type==EnumCreatureType.monster&&scatteredFeatureGenerator.func_143030_a(x,y,z))
            return scatteredFeatureGenerator.getScatteredFeatureSpawnList();
        BiomeGenBase biome=biomeManager.getUndergroundBiomeAt(x,y,z,terrain,new MutableDensityContext120());
        // The 1.20 Deep Dark biome has empty spawner maps in every creature category.
        if(biome!=null&&biome.biomeID==FuturaBiomeIds120.DEEP_DARK)return Collections.emptyList();
        if(type==EnumCreatureType.waterCreature&&biome!=null){
            if(biome.biomeID==FuturaBiomeIds120.LUSH_CAVES)return FuturaCaveCreatureSpawns120.LUSH_WATER;
            if(biome.biomeID==FuturaBiomeIds120.DRIPSTONE_CAVES)return FuturaCaveCreatureSpawns120.DRIPSTONE_WATER;
        }
        return (biome==null?world.getBiomeGenForCoords(x,z):biome).getSpawnableList(type);
    }
    @Override public ChunkPosition func_147416_a(World world, String structure, int x, int y, int z) {
        return "Stronghold".equals(structure) ? strongholdGenerator.func_151545_a(world, x, y, z) : null;
    }
    @Override public void recreateStructures(int x, int z) {
        if (!mapFeaturesEnabled) return;
        mineshaftGenerator.func_151539_a(this, world, x, z, null);
        strongholdGenerator.func_151539_a(this, world, x, z, null);
        scatteredFeatureGenerator.func_151539_a(this, world, x, z, null);
    }
}
