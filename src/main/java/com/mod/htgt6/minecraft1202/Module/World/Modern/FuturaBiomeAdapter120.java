package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeRegistry120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import net.minecraft.world.biome.BiomeGenBase;

/** Maps 1.20.2 biome keys onto vanilla 1.7 identities or registered extended IDs. */
public final class FuturaBiomeAdapter120 {
    public static BiomeGenBase resolve(OverworldBiomeParameterMap120.Biome biome){
        switch(biome){
            case PLAINS:return BiomeGenBase.plains;
            case SUNFLOWER_PLAINS:return id(129,BiomeGenBase.plains);
            case SNOWY_PLAINS:return BiomeGenBase.icePlains;
            case ICE_SPIKES:return id(140,BiomeGenBase.icePlains);
            case DESERT:return BiomeGenBase.desert;
            case SWAMP:return BiomeGenBase.swampland;
            case MANGROVE_SWAMP:return futura(FuturaBiomeIds120.MANGROVE_SWAMP);
            case FOREST:return BiomeGenBase.forest;
            case FLOWER_FOREST:return id(132,BiomeGenBase.forest);
            case BIRCH_FOREST:return BiomeGenBase.birchForest;
            case DARK_FOREST:return BiomeGenBase.roofedForest;
            case OLD_GROWTH_BIRCH_FOREST:return id(155,BiomeGenBase.birchForest);
            case OLD_GROWTH_PINE_TAIGA:return BiomeGenBase.megaTaiga;
            case OLD_GROWTH_SPRUCE_TAIGA:return id(160,BiomeGenBase.megaTaiga);
            case TAIGA:return BiomeGenBase.taiga;
            case SNOWY_TAIGA:return BiomeGenBase.coldTaiga;
            case SAVANNA:return BiomeGenBase.savanna;
            case SAVANNA_PLATEAU:return BiomeGenBase.savannaPlateau;
            case WINDSWEPT_HILLS:return BiomeGenBase.extremeHills;
            case WINDSWEPT_GRAVELLY_HILLS:return id(131,BiomeGenBase.extremeHills);
            case WINDSWEPT_FOREST:return BiomeGenBase.extremeHillsPlus;
            case WINDSWEPT_SAVANNA:return id(163,BiomeGenBase.savanna);
            case JUNGLE:return BiomeGenBase.jungle;
            case SPARSE_JUNGLE:return BiomeGenBase.jungleEdge;
            case BAMBOO_JUNGLE:return futura(FuturaBiomeIds120.BAMBOO_JUNGLE);
            case BADLANDS:return BiomeGenBase.mesa;
            case ERODED_BADLANDS:return id(165,BiomeGenBase.mesa);
            case WOODED_BADLANDS:return id(38,BiomeGenBase.mesa);
            case MEADOW:return futura(FuturaBiomeIds120.MEADOW);
            case CHERRY_GROVE:return futura(FuturaBiomeIds120.CHERRY_GROVE);
            case GROVE:return futura(FuturaBiomeIds120.GROVE);
            case SNOWY_SLOPES:return futura(FuturaBiomeIds120.SNOWY_SLOPES);
            case FROZEN_PEAKS:return futura(FuturaBiomeIds120.FROZEN_PEAKS);
            case JAGGED_PEAKS:return futura(FuturaBiomeIds120.JAGGED_PEAKS);
            case STONY_PEAKS:return futura(FuturaBiomeIds120.STONY_PEAKS);
            case RIVER:return BiomeGenBase.river;
            case FROZEN_RIVER:return BiomeGenBase.frozenRiver;
            case BEACH:return BiomeGenBase.beach;
            case SNOWY_BEACH:return BiomeGenBase.coldBeach;
            case STONY_SHORE:return BiomeGenBase.stoneBeach;
            case WARM_OCEAN:return ocean(OceanBiomeRegistry.warmOcean,BiomeGenBase.ocean);
            case LUKEWARM_OCEAN:return ocean(OceanBiomeRegistry.lukewarmOcean,BiomeGenBase.ocean);
            case DEEP_LUKEWARM_OCEAN:return ocean(OceanBiomeRegistry.deepLukewarmOcean,BiomeGenBase.deepOcean);
            case OCEAN:return BiomeGenBase.ocean;
            case DEEP_OCEAN:return BiomeGenBase.deepOcean;
            case COLD_OCEAN:return ocean(OceanBiomeRegistry.coldOcean,BiomeGenBase.ocean);
            case DEEP_COLD_OCEAN:return ocean(OceanBiomeRegistry.deepColdOcean,BiomeGenBase.deepOcean);
            case FROZEN_OCEAN:return BiomeGenBase.frozenOcean;
            case DEEP_FROZEN_OCEAN:return ocean(OceanBiomeRegistry.deepFrozenOcean,BiomeGenBase.frozenOcean);
            case MUSHROOM_FIELDS:return BiomeGenBase.mushroomIsland;
            case DRIPSTONE_CAVES:return futura(FuturaBiomeIds120.DRIPSTONE_CAVES);
            case LUSH_CAVES:return futura(FuturaBiomeIds120.LUSH_CAVES);
            case DEEP_DARK:return futura(FuturaBiomeIds120.DEEP_DARK);
            default:return BiomeGenBase.plains;
        }
    }
    private static BiomeGenBase futura(int id){return FuturaBiomeRegistry120.get(id);}
    private static BiomeGenBase id(int id,BiomeGenBase fallback){BiomeGenBase biome=BiomeGenBase.getBiome(id);return biome==null?fallback:biome;}
    private static BiomeGenBase ocean(BiomeGenBase biome,BiomeGenBase fallback){return biome==null?fallback:biome;}
    private FuturaBiomeAdapter120(){}
}
