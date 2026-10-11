package com.mod.htgt6.minecraft1202.Module.World.Modern;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.Random;

/**
 * Native 1.7 implementation of the 1.20.2 biome feature pipeline.
 *
 * <p>The implementation deliberately does not call {@link BiomeGenBase#decorate}.
 * Modern configured/placed features are represented by the named passes below and
 * receive an independent decorator seed.  The generators themselves only use the
 * 1.7 World/WorldGenerator API.</p>
 */
public final class FuturaBiomeDecorator120 {
    public enum DecorationStep {
        RAW_GENERATION,
        LAKES,
        LOCAL_MODIFICATIONS,
        UNDERGROUND_STRUCTURES,
        SURFACE_STRUCTURES,
        STRONGHOLDS,
        UNDERGROUND_ORES,
        UNDERGROUND_DECORATION,
        FLUID_SPRINGS,
        VEGETAL_DECORATION,
        TOP_LAYER_MODIFICATION
    }

    private final FuturaUndergroundFeatures120 underground;
    private final FuturaDripstoneCavesRuntime120 dripstone;
    private final FuturaLushCavesRuntime120 lush;
    private final FuturaDeepDarkRuntime120 deepDark;
    private final FuturaVegetationPlacement120 vegetation;

    public FuturaBiomeDecorator120(FuturaMountainDensityRouter120 terrain){
        underground=new FuturaUndergroundFeatures120(terrain);
        dripstone=new FuturaDripstoneCavesRuntime120(terrain);
        lush=new FuturaLushCavesRuntime120(terrain);
        deepDark=new FuturaDeepDarkRuntime120(terrain);
        vegetation=new FuturaVegetationPlacement120(underground);
    }

    public void decorate(World world,IChunkProvider generator,IChunkProvider provider,
                         int chunkX,int chunkZ,int generationHeight,boolean villageGenerated){
        int originX=chunkX<<4,originZ=chunkZ<<4;
        Random eventRandom=featureRandom(world.getSeed(),chunkX,chunkZ,DecorationStep.RAW_GENERATION,0);
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(FuturaGenerationRegion120.unwrap(world),eventRandom,originX,originZ));

        FuturaPlacementData120 data=FuturaPlacementData120.INSTANCE;
        for(int step=0;step<DecorationStep.values().length;step++){
            Random stageRandom=featureRandom(world.getSeed(),chunkX,chunkZ,DecorationStep.values()[step],0);
            if(step==6||step==7)MinecraftForge.ORE_GEN_BUS.post(new net.minecraftforge.event.terraingen.OreGenEvent.Pre(FuturaGenerationRegion120.unwrap(world),stageRandom,originX,originZ));
            for(String key:data.features(step))dispatch(world,provider,chunkX,chunkZ,generationHeight,villageGenerated,step,key);
            if(step==6||step==7)MinecraftForge.ORE_GEN_BUS.post(new net.minecraftforge.event.terraingen.OreGenEvent.Post(FuturaGenerationRegion120.unwrap(world),stageRandom,originX,originZ));
        }

        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(FuturaGenerationRegion120.unwrap(world),eventRandom,originX,originZ));
        // 1.7 has no stage-aware mod worldgen API. Keep it as an explicit compatibility
        // boundary after owned placed features rather than mixing it into their RNG.
        GameRegistry.generateWorld(chunkX,chunkZ,FuturaGenerationRegion120.unwrap(world),generator,provider);
    }

    private static Random namedRandom(World world,int x,int z,int step,String name){
        return FuturaPlacementData120.INSTANCE.random(world.getSeed(),x,z,step,name);
    }

    private void dispatch(World world,IChunkProvider provider,int cx,int cz,int height,boolean village,int step,String key){
        Random r=namedRandom(world,cx,cz,step,key);
        if(key.startsWith("minecraft:ore_")){underground.populateOre(world,cx,cz,step,key);return;}
        if(key.equals("minecraft:amethyst_geode")){underground.populateGeodes(world,r,cx,cz);return;}
        if(key.equals("minecraft:large_dripstone")){dripstone.populateLarge(world,cx,cz,height);return;}
        if(key.equals("minecraft:dripstone_cluster")||key.equals("minecraft:pointed_dripstone")){dripstone.populateFeature(world,r,cx,cz,height,key);return;}
        if(key.equals("minecraft:sculk_vein")||key.equals("minecraft:sculk_patch_deep_dark")){deepDark.populateFeature(world,r,cx,cz,height,key);return;}
        if(step==9){
            boolean owned=false;
            if(FuturaVegetationPlacement120.supported(key.substring(10))){vegetation.populateFeature(world,cx,cz,key);owned=true;}
            if(FuturaLushCavesRuntime120.owns(key)){lush.populateFeature(world,r,cx,cz,height,key);owned=true;}
            if(owned)return;
        }
        if(key.equals("minecraft:freeze_top_layer")){freezeTopLayer(world,provider,cx,cz,village,r);return;}
        if(key.startsWith("minecraft:lake_lava")){
            if(TerrainGen.populate(provider,FuturaGenerationRegion120.unwrap(world),r,cx,cz,village,PopulateChunkEvent.Populate.EventType.LAVA))
                FuturaPlacementExecutor120.execute(world,cx,cz,step,key,underground,(w,random,x,y,z)->FuturaLakeFeature120.generate(w,random,FuturaPlacementData120.INSTANCE.configured("lake_lava").getAsJsonObject("config"),x,y,z));
            return;
        }
        if(key.startsWith("minecraft:monster_room")){
            if(TerrainGen.populate(provider,FuturaGenerationRegion120.unwrap(world),r,cx,cz,village,PopulateChunkEvent.Populate.EventType.DUNGEON))
                FuturaPlacementExecutor120.execute(world,cx,cz,step,key,underground,(w,random,x,y,z)->new WorldGenDungeons().generate(w,random,x,y,z));
            return;
        }
        if(key.startsWith("minecraft:spring_")){
            if(TerrainGen.decorate(FuturaGenerationRegion120.unwrap(world),r,cx<<4,cz<<4,DecorateBiomeEvent.Decorate.EventType.LAKE))
                FuturaPlacementExecutor120.execute(world,cx,cz,step,key,underground,(w,random,x,y,z)->spring(w,key,x,y,z));
            return;
        }
        FuturaMiscFeatures120.populate(world,cx,cz,step,key,underground);
    }
    private static void spring(World w,String key,int x,int y,int z){
        if(y<1||y>=w.getHeight()-1)return;
        com.google.gson.JsonObject config=FuturaPlacementData120.INSTANCE.configured(FuturaPlacementData120.INSTANCE.placed(key).get("feature").getAsString()).getAsJsonObject("config");
        com.google.gson.JsonObject predicate=new com.google.gson.JsonObject();predicate.addProperty("type","minecraft:matching_blocks");predicate.add("blocks",config.get("valid_blocks"));
        if(!FuturaBlockState120.predicate(w,predicate,x,y+1,z))return;
        if(config.get("requires_block_below").getAsBoolean()&&!FuturaBlockState120.predicate(w,predicate,x,y-1,z))return;
        if(!w.isAirBlock(x,y,z)&&!FuturaBlockState120.predicate(w,predicate,x,y,z))return;
        int rock=0,air=0;int[][] offsets={{-1,0,0},{1,0,0},{0,0,-1},{0,0,1},{0,-1,0}};
        for(int[] d:offsets){if(FuturaBlockState120.predicate(w,predicate,x+d[0],y+d[1],z+d[2]))rock++;if(w.isAirBlock(x+d[0],y+d[1],z+d[2]))air++;}
        if(rock==config.get("rock_count").getAsInt()&&air==config.get("hole_count").getAsInt()){
            net.minecraft.block.Block liquid=config.getAsJsonObject("state").get("Name").getAsString().equals("minecraft:water")?Blocks.flowing_water:Blocks.flowing_lava;
            w.setBlock(x,y,z,liquid,0,2);w.scheduleBlockUpdate(x,y,z,liquid,0);
        }
    }
    private static void freezeTopLayer(World world,IChunkProvider provider,int chunkX,int chunkZ,
                                       boolean village,Random random){
        if(!TerrainGen.populate(provider,FuturaGenerationRegion120.unwrap(world),random,chunkX,chunkZ,village,
                PopulateChunkEvent.Populate.EventType.ICE))return;
        int ox=chunkX<<4,oz=chunkZ<<4;
        for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++){
            int x=ox+dx,z=oz+dz,y=world.getPrecipitationHeight(x,z);
            if(world.isBlockFreezable(x,y-1,z))world.setBlock(x,y-1,z,Blocks.ice,0,2);
            if(world.func_147478_e(x,y,z,true))world.setBlock(x,y,z,Blocks.snow_layer,0,2);
        }
    }

    /** The seed contract is public so fixtures can catch accidental pass coupling. */
    public static long featureSeed(long worldSeed,int chunkX,int chunkZ,DecorationStep step,int featureIndex){
        return WorldgenRandom120.featureSeed(
                WorldgenRandom120.decorationSeed(worldSeed,chunkX<<4,chunkZ<<4),step.ordinal(),featureIndex);
    }

    private static Random featureRandom(long seed,int chunkX,int chunkZ,DecorationStep step,int featureIndex){
        return new WorldgenRandom120(featureSeed(seed,chunkX,chunkZ,step,featureIndex));
    }

}
