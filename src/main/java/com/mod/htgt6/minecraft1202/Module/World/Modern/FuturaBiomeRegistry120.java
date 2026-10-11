package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.asm.HTGT6Transformer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

/** Registers only biomes which do not already have a vanilla 1.7.10 identity. */
public final class FuturaBiomeRegistry120 {
    private static final BiomeGenBase[] NEW_BIOMES = new BiomeGenBase[
            FuturaBiomeIds120.MAX_ID-FuturaBiomeIds120.MIN_ID+1];
    private static boolean registered;

    public static synchronized void register() {
        if (!HTGT6Config.ENABLE_EXTENDED_BIOME_ASM)
            throw new IllegalStateException("Futura requires EnableExtendedBiomeASM=true");
        if (registered) return;
        for(int id=FuturaBiomeIds120.MIN_ID;id<=FuturaBiomeIds120.MAX_ID;id++){
            BiomeGenBase occupied=HTGT6Transformer.getRegisteredBiomeExact(id);
            if(occupied!=null)throw new IllegalStateException("Futura biome ID "+id
                    +" is already occupied by "+occupied.biomeName);
        }
        add(FuturaBiomeIds120.BAMBOO_JUNGLE,"Bamboo Jungle",BiomeGenBase.jungle,.95F,.9F,false,false,4159204,
                BiomeDictionary.Type.JUNGLE,BiomeDictionary.Type.HOT,BiomeDictionary.Type.WET);
        add(FuturaBiomeIds120.MANGROVE_SWAMP,"Mangrove Swamp",BiomeGenBase.swampland,.8F,.9F,false,false,3832426,
                BiomeDictionary.Type.SWAMP,BiomeDictionary.Type.HOT,BiomeDictionary.Type.WET);
        add(FuturaBiomeIds120.MEADOW,"Meadow",BiomeGenBase.plains,.5F,.8F,false,false,937679,
                BiomeDictionary.Type.PLAINS,BiomeDictionary.Type.MOUNTAIN);
        add(FuturaBiomeIds120.CHERRY_GROVE,"Cherry Grove",BiomeGenBase.forest,.5F,.8F,false,false,6141935,
                BiomeDictionary.Type.FOREST,BiomeDictionary.Type.MOUNTAIN);
        add(FuturaBiomeIds120.GROVE,"Grove",BiomeGenBase.coldTaiga,-.2F,.8F,true,false,4159204,
                BiomeDictionary.Type.FOREST,BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.MOUNTAIN);
        add(FuturaBiomeIds120.SNOWY_SLOPES,"Snowy Slopes",BiomeGenBase.iceMountains,-.3F,.9F,true,false,4159204,
                BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.MOUNTAIN);
        add(FuturaBiomeIds120.JAGGED_PEAKS,"Jagged Peaks",BiomeGenBase.iceMountains,-.7F,.9F,true,false,4159204,
                BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.MOUNTAIN);
        add(FuturaBiomeIds120.FROZEN_PEAKS,"Frozen Peaks",BiomeGenBase.iceMountains,-.7F,.9F,true,false,4159204,
                BiomeDictionary.Type.SNOWY,BiomeDictionary.Type.MOUNTAIN);
        add(FuturaBiomeIds120.STONY_PEAKS,"Stony Peaks",BiomeGenBase.extremeHills,1F,.3F,false,false,4159204,
                BiomeDictionary.Type.MOUNTAIN);
        add(FuturaBiomeIds120.LUSH_CAVES,"Lush Caves",BiomeGenBase.forest,.5F,.5F,false,false,4159204,
                BiomeDictionary.Type.WET);
        add(FuturaBiomeIds120.DRIPSTONE_CAVES,"Dripstone Caves",BiomeGenBase.extremeHills,.8F,.4F,false,false,4159204,
                BiomeDictionary.Type.DRY);
        add(FuturaBiomeIds120.DEEP_DARK,"Deep Dark",BiomeGenBase.plains,.8F,.4F,false,false,4159204);
        registered=true;
        validateContract();
    }

    private static void add(int id,String name,BiomeGenBase source,float temperature,float rainfall,
                            boolean snow,boolean dry,int waterColor,BiomeDictionary.Type...types){
        BiomeGenBase biome=new BiomeGenFuturaClimate120(id,name,source,temperature,rainfall,snow,dry,waterColor);
        NEW_BIOMES[id-FuturaBiomeIds120.MIN_ID]=biome;
        if(types.length>0)BiomeDictionary.registerBiomeType(biome,types);
    }

    public static BiomeGenBase get(int id) {
        if (!registered) throw new IllegalStateException("Futura biome registry is not initialized");
        if(id<FuturaBiomeIds120.MIN_ID||id>FuturaBiomeIds120.MAX_ID)
            throw new IllegalArgumentException("Not a new Futura biome ID: "+id);
        return NEW_BIOMES[id-FuturaBiomeIds120.MIN_ID];
    }

    /** Fails during startup if an extended registration or a vanilla identity was remapped. */
    public static void validateContract() {
        if (!registered) throw new IllegalStateException("Futura biome registry is not initialized");
        for (int id=FuturaBiomeIds120.MIN_ID;id<=FuturaBiomeIds120.MAX_ID;id++) {
            BiomeGenBase expected=NEW_BIOMES[id-FuturaBiomeIds120.MIN_ID];
            if (expected==null || expected.biomeID!=id || HTGT6Transformer.getRegisteredBiomeExact(id)!=expected)
                throw new IllegalStateException("Futura new-biome registration mismatch at ID "+id);
        }
        assertClimate(FuturaBiomeIds120.BAMBOO_JUNGLE,.95F,.9F,4159204,false);
        assertClimate(FuturaBiomeIds120.MANGROVE_SWAMP,.8F,.9F,3832426,false);
        assertClimate(FuturaBiomeIds120.MEADOW,.5F,.8F,937679,false);
        assertClimate(FuturaBiomeIds120.CHERRY_GROVE,.5F,.8F,6141935,false);
        assertClimate(FuturaBiomeIds120.GROVE,-.2F,.8F,4159204,true);
        assertClimate(FuturaBiomeIds120.SNOWY_SLOPES,-.3F,.9F,4159204,true);
        assertClimate(FuturaBiomeIds120.JAGGED_PEAKS,-.7F,.9F,4159204,true);
        assertClimate(FuturaBiomeIds120.FROZEN_PEAKS,-.7F,.9F,4159204,true);
        assertClimate(FuturaBiomeIds120.STONY_PEAKS,1F,.3F,4159204,false);
        assertClimate(FuturaBiomeIds120.LUSH_CAVES,.5F,.5F,4159204,false);
        assertClimate(FuturaBiomeIds120.DRIPSTONE_CAVES,.8F,.4F,4159204,false);
        assertClimate(FuturaBiomeIds120.DEEP_DARK,.8F,.4F,4159204,false);
        assertVanilla(BiomeGenBase.ocean,0,"Ocean");
        assertVanilla(BiomeGenBase.plains,1,"Plains");
        assertVanilla(BiomeGenBase.desert,2,"Desert");
        assertVanilla(BiomeGenBase.extremeHills,3,"Extreme Hills");
        assertVanilla(BiomeGenBase.forest,4,"Forest");
        assertVanilla(BiomeGenBase.taiga,5,"Taiga");
        assertVanilla(BiomeGenBase.swampland,6,"Swampland");
        assertVanilla(BiomeGenBase.river,7,"River");
        assertVanilla(BiomeGenBase.frozenOcean,10,"Frozen Ocean");
        assertVanilla(BiomeGenBase.frozenRiver,11,"Frozen River");
        assertVanilla(BiomeGenBase.icePlains,12,"Ice Plains");
        assertVanilla(BiomeGenBase.iceMountains,13,"Ice Mountains");
        assertVanilla(BiomeGenBase.mushroomIsland,14,"Mushroom Island");
        assertVanilla(BiomeGenBase.beach,16,"Beach");
        assertVanilla(BiomeGenBase.jungle,21,"Jungle");
        assertVanilla(BiomeGenBase.deepOcean,24,"Deep Ocean");
        assertVanilla(BiomeGenBase.stoneBeach,25,"Stone Beach");
        assertVanilla(BiomeGenBase.coldBeach,26,"Cold Beach");
        assertVanilla(BiomeGenBase.birchForest,27,"Birch Forest");
        assertVanilla(BiomeGenBase.roofedForest,29,"Roofed Forest");
        assertVanilla(BiomeGenBase.coldTaiga,30,"Cold Taiga");
        assertVanilla(BiomeGenBase.megaTaiga,32,"Mega Taiga");
        assertVanilla(BiomeGenBase.extremeHillsPlus,34,"Extreme Hills+");
        assertVanilla(BiomeGenBase.savanna,35,"Savanna");
        assertVanilla(BiomeGenBase.mesa,37,"Mesa");
    }

    private static void assertVanilla(BiomeGenBase biome,int id,String name) {
        if (biome==null || biome.biomeID!=id || BiomeGenBase.getBiome(id)!=biome)
            throw new IllegalStateException("Futura must not remap vanilla biome "+name+" (ID "+id+")");
    }

    private static void assertClimate(int id,float temperature,float downfall,int waterColor,boolean snow){
        BiomeGenBase biome=get(id);
        if(Float.floatToIntBits(biome.temperature)!=Float.floatToIntBits(temperature)
                ||Float.floatToIntBits(biome.rainfall)!=Float.floatToIntBits(downfall)
                ||biome.waterColorMultiplier!=waterColor||biome.getEnableSnow()!=snow)
            throw new IllegalStateException("Futura biome JSON climate mismatch at ID "+id);
    }

    private FuturaBiomeRegistry120() { }
}
