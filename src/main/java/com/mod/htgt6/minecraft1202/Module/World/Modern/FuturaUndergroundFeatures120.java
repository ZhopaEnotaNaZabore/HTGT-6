package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.Random;

/** Shared 1.20 underground feature passes which are absent from 1.7 biome decorators. */
public final class FuturaUndergroundFeatures120 {
    public static final int GEODE_RARITY=24;
    private final FuturaMountainDensityRouter120 terrain;
    private final MutableDensityContext120 biomeSample=new MutableDensityContext120();

    public FuturaUndergroundFeatures120(FuturaMountainDensityRouter120 terrain){this.terrain=terrain;}

    public void populateGeodes(World world,Random random,int chunkX,int chunkZ){
        if(random.nextFloat()>=1.0F/GEODE_RARITY)return;
        int x=(chunkX<<4)+random.nextInt(16),z=(chunkZ<<4)+random.nextInt(16);
        // above_bottom 6 .. absolute 30 => translated physical 6 .. 94
        int y=6+random.nextInt(FuturaMountainDensityRouter120.modernToFuturaY(30)-5);
        if(geodes==null)geodes=new FuturaGeodeFeature120(world.getSeed());
        geodes.generate(world,random,FuturaPlacementData120.INSTANCE.configured("amethyst_geode").getAsJsonObject("config"),x,y,z);
    }

    private final java.util.Map<String,WorldGenBackport120Ore> oreGenerators=
            new java.util.HashMap<String,WorldGenBackport120Ore>();

    public void populateOres(World world,Random eventRandom,int chunkX,int chunkZ){
        int x=chunkX<<4,z=chunkZ<<4;
        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(FuturaGenerationRegion120.unwrap(world),eventRandom,x,z));
        for(String key:FuturaPlacementData120.INSTANCE.features(6))if(key.startsWith("minecraft:ore_"))populateOre(world,chunkX,chunkZ,6,key);
        MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(FuturaGenerationRegion120.unwrap(world),eventRandom,x,z));
    }

    public void populateOre(World world,int chunkX,int chunkZ,int step,String key){
        int x=chunkX<<4,z=chunkZ<<4;
        FuturaPlacementData120 data=FuturaPlacementData120.INSTANCE;
            com.google.gson.JsonObject placed=data.placed(key);
            String configured=placed.get("feature").getAsString();
            WorldGenBackport120Ore generator=oreGenerators.get(configured);
            if(generator==null){generator=new WorldGenBackport120Ore(data.configured(configured).getAsJsonObject("config"));
                oreGenerators.put(configured,generator);}
            Random random=data.random(world.getSeed(),chunkX,chunkZ,step,key);
            if(!TerrainGen.generateOre(FuturaGenerationRegion120.unwrap(world),random,generator,x,z,oreEvent(key)))return;
            int attempts=1;com.google.gson.JsonObject height=null;
            for(com.google.gson.JsonElement modifier:placed.getAsJsonArray("placement")){
                com.google.gson.JsonObject m=modifier.getAsJsonObject();String type=m.get("type").getAsString();
                if(type.equals("minecraft:count"))attempts=FuturaPlacementData120.count(random,m.get("count"));
                else if(type.equals("minecraft:rarity_filter")&&random.nextFloat()>=1.0F/m.get("chance").getAsInt())attempts=0;
                else if(type.equals("minecraft:height_range"))height=m.getAsJsonObject("height");
            }
            if(height==null)throw new IllegalStateException("Missing ore height: "+key);
            for(int i=0;i<attempts;i++){
                int px=x+random.nextInt(16),pz=z+random.nextInt(16);
                int py=FuturaPlacementData120.sampleHeight(random,height,world.getHeight());
                // Keep the sampled origin; OreFeature bounds-checks each candidate block.
                if(!allows(world,px,py,pz,step,key))continue;
                generator.generate(world,random,px,py,pz);
            }
    }

    public boolean allows(World world,int x,int y,int z,int step,String feature){
        if(!(world.getWorldChunkManager() instanceof FuturaWorldChunkManager120))return false;
        OverworldBiomeParameterMap120.Biome key=((FuturaWorldChunkManager120)world.getWorldChunkManager())
                .getBiomeKeyAt(x,y,z,terrain,biomeSample);
        return FuturaPlacementData120.INSTANCE.has(key,step,feature);
    }

    private static OreGenEvent.GenerateMinable.EventType oreEvent(String key){
        String name=key.substring("minecraft:ore_".length()).split("_")[0].toUpperCase(java.util.Locale.ROOT);
        try{return OreGenEvent.GenerateMinable.EventType.valueOf(name);}
        catch(IllegalArgumentException ignored){return OreGenEvent.GenerateMinable.EventType.CUSTOM;}
    }

    private FuturaGeodeFeature120 geodes;
}
