package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

/** Direct, ordered evaluator of Minecraft 1.20.2 SurfaceRuleData.overworld(). */
public final class FuturaOverworldSurfaceRules120 {
    private static final FuturaSurfaceRules120.State STONE=state(Blocks.stone),DIRT=state(Blocks.dirt);
    private static final FuturaSurfaceRules120.State COARSE_DIRT=state(BountifulContent.coarseDirt);
    private static final FuturaSurfaceRules120.State PODZOL=new FuturaSurfaceRules120.State(Blocks.dirt,2);
    private static final FuturaSurfaceRules120.State GRASS=state(Blocks.grass),GRAVEL=state(Blocks.gravel);
    private static final FuturaSurfaceRules120.State SAND=state(Blocks.sand),SANDSTONE=state(Blocks.sandstone);
    private static final FuturaSurfaceRules120.State RED_SAND=new FuturaSurfaceRules120.State(Blocks.sand,1);
    private static final FuturaSurfaceRules120.State RED_SANDSTONE=state(BountifulContent.redSandstone);
    private static final FuturaSurfaceRules120.State TERRACOTTA=state(Blocks.hardened_clay);
    private static final FuturaSurfaceRules120.State ORANGE_TERRACOTTA=clay(1),WHITE_TERRACOTTA=clay(0);
    private static final FuturaSurfaceRules120.State CALCITE=state(Backport120Content.calcite);
    private static final FuturaSurfaceRules120.State PACKED_ICE=state(Blocks.packed_ice),ICE=state(Blocks.ice);
    private static final FuturaSurfaceRules120.State SNOW=state(Blocks.snow),POWDER_SNOW=state(Backport120Content.powderSnow);
    private static final FuturaSurfaceRules120.State MUD=state(Backport120Content.mud);
    private static final FuturaSurfaceRules120.State WATER=state(Blocks.water),AIR=new FuturaSurfaceRules120.State(null,0);

    public static FuturaSurfaceRules120.State select(FuturaSurfaceSystem120 system,FuturaSurfaceContext120 context,
                                                      BiomeGenBase biome,int x,int z){
        int id=biome.biomeID;
        boolean onFloor=context.onFloor(),underFloor=context.underFloor();
        boolean deep=context.stoneDepth(FuturaSurfaceContext120.CaveSurface.FLOOR,0,true,6);
        boolean veryDeep=context.stoneDepth(FuturaSurfaceContext120.CaveSurface.FLOOR,0,true,30);
        boolean waterMinusOne=context.waterBlock(-1,0,false),waterZero=context.waterBlock(0,0,false);
        boolean waterStartMinusSix=context.waterBlock(-6,-1,true);

        if(onFloor){
            if(isWoodedBadlands(id)&&context.yBlockCheck(161,2,false)){
                if(surfaceBands(system,x,z))return COARSE_DIRT;
                return waterZero?GRASS:DIRT;
            }
            if(isSwamp(id)&&context.yBlockCheck(126,0,false)&&!context.yBlockCheck(127,0,false)
                    &&system.noiseBetween(FuturaSurfaceSystem120.NOISE_SWAMP,x,z,0.0D,Double.MAX_VALUE))return WATER;
            if(id==FuturaBiomeIds120.MANGROVE_SWAMP&&context.yBlockCheck(124,0,false)
                    &&!context.yBlockCheck(127,0,false)
                    &&system.noiseBetween(FuturaSurfaceSystem120.NOISE_SWAMP,x,z,0.0D,Double.MAX_VALUE))return WATER;
        }

        if(isBadlands(id)){
            if(onFloor){
                if(context.yBlockCheck(320,0,false))return ORANGE_TERRACOTTA;
                if(context.yBlockCheck(138,1,true)){
                    if(surfaceBands(system,x,z))return TERRACOTTA;
                    return system.clayBand(x,context.y(),z);
                }
                if(waterMinusOne)return context.onCeiling()?RED_SANDSTONE:RED_SAND;
                if(!context.hole())return ORANGE_TERRACOTTA;
                if(waterStartMinusSix)return WHITE_TERRACOTTA;
                return context.onCeiling()?STONE:GRAVEL;
            }
            if(context.yBlockCheck(127,-1,true)){
                if(context.yBlockCheck(127,0,false)&&!context.yBlockCheck(138,1,true))return ORANGE_TERRACOTTA;
                return system.clayBand(x,context.y(),z);
            }
            if(underFloor&&waterStartMinusSix)return WHITE_TERRACOTTA;
        }

        if(onFloor&&waterMinusOne){
            if(isFrozenOcean(biome)&&context.hole()){
                if(waterZero)return AIR;
                return context.cold()?WATER:ICE;
            }
            return topMaterial(system,context,biome,x,z);
        }
        if(waterStartMinusSix){
            if(onFloor&&isFrozenOcean(biome)&&context.hole())return WATER;
            if(underFloor)return underMaterial(system,context,biome,x,z);
            if(isWarmOcean(biome)&&deep)return SANDSTONE;
            if(id==BiomeGenBase.desert.biomeID&&veryDeep)return SANDSTONE;
        }
        if(onFloor){
            if(id==FuturaBiomeIds120.FROZEN_PEAKS||id==FuturaBiomeIds120.JAGGED_PEAKS)return STONE;
            if(isWarmOcean(biome))return context.onCeiling()?SANDSTONE:SAND;
            return GRAVEL;
        }
        return null;
    }

    private static FuturaSurfaceRules120.State topMaterial(FuturaSurfaceSystem120 s,FuturaSurfaceContext120 c,
                                                            BiomeGenBase biome,int x,int z){
        int id=biome.biomeID;
        if(id==FuturaBiomeIds120.FROZEN_PEAKS){
            if(c.steep())return PACKED_ICE;
            if(s.noiseBetween(FuturaSurfaceSystem120.NOISE_PACKED_ICE,x,z,0.0D,.2D))return PACKED_ICE;
            if(s.noiseBetween(FuturaSurfaceSystem120.NOISE_ICE,x,z,0.0D,.025D))return ICE;
            return c.waterBlock(0,0,false)?SNOW:null;
        }
        if(id==FuturaBiomeIds120.SNOWY_SLOPES){
            if(c.steep())return STONE;
            // SurfaceRuleData wraps both the powder-snow branch and the
            // ordinary snow fallback in WATER_BLOCK_CHECK(0, 0).  Checking
            // only the outer water(-1) condition leaks powder snow one block
            // below shallow water/ice edges.
            if(c.waterBlock(0,0,false)
                    &&s.noiseBetween(FuturaSurfaceSystem120.NOISE_POWDER_SNOW,x,z,.35D,.6D))return POWDER_SNOW;
            return c.waterBlock(0,0,false)?SNOW:null;
        }
        if(id==FuturaBiomeIds120.JAGGED_PEAKS)return c.steep()?STONE:(c.waterBlock(0,0,false)?SNOW:null);
        if(id==FuturaBiomeIds120.GROVE){
            if(c.waterBlock(0,0,false)
                    &&s.noiseBetween(FuturaSurfaceSystem120.NOISE_POWDER_SNOW,x,z,.35D,.6D))return POWDER_SNOW;
            return c.waterBlock(0,0,false)?SNOW:null;
        }
        FuturaSurfaceRules120.State special=specialSurface(s,c,biome,x,z);if(special!=null)return special;
        if(id==163)return s.surfaceNoiseAbove(x,z,1.75D)?STONE:(s.surfaceNoiseAbove(x,z,-.5D)?COARSE_DIRT:null);
        if(id==131){if(s.surfaceNoiseAbove(x,z,2.0D))return GRAVEL;if(s.surfaceNoiseAbove(x,z,1.0D))return STONE;
            if(s.surfaceNoiseAbove(x,z,-1.0D))return c.waterBlock(0,0,false)?GRASS:DIRT;return GRAVEL;}
        if(id==BiomeGenBase.megaTaiga.biomeID||id==160){if(s.surfaceNoiseAbove(x,z,1.75D))return COARSE_DIRT;
            if(s.surfaceNoiseAbove(x,z,-.95D))return PODZOL;}
        if(id==140&&c.waterBlock(0,0,false))return SNOW;
        if(id==FuturaBiomeIds120.MANGROVE_SWAMP)return MUD;
        if(id==BiomeGenBase.mushroomIsland.biomeID)return state(Blocks.mycelium);
        return c.waterBlock(0,0,false)?GRASS:DIRT;
    }

    private static FuturaSurfaceRules120.State underMaterial(FuturaSurfaceSystem120 s,FuturaSurfaceContext120 c,
                                                              BiomeGenBase biome,int x,int z){
        int id=biome.biomeID;
        if(id==FuturaBiomeIds120.FROZEN_PEAKS){if(c.steep())return PACKED_ICE;
            if(s.noiseBetween(FuturaSurfaceSystem120.NOISE_PACKED_ICE,x,z,-.5D,.2D))return PACKED_ICE;
            if(s.noiseBetween(FuturaSurfaceSystem120.NOISE_ICE,x,z,-.0625D,.025D))return ICE;
            return c.waterBlock(0,0,false)?SNOW:null;}
        if(id==FuturaBiomeIds120.SNOWY_SLOPES){if(c.steep())return STONE;
            if(c.waterBlock(0,0,false)
                    &&s.noiseBetween(FuturaSurfaceSystem120.NOISE_POWDER_SNOW,x,z,.45D,.58D))return POWDER_SNOW;
            return c.waterBlock(0,0,false)?SNOW:null;}
        if(id==FuturaBiomeIds120.JAGGED_PEAKS)return STONE;
        if(id==FuturaBiomeIds120.GROVE){if(c.waterBlock(0,0,false)
                &&s.noiseBetween(FuturaSurfaceSystem120.NOISE_POWDER_SNOW,x,z,.45D,.58D))return POWDER_SNOW;return DIRT;}
        FuturaSurfaceRules120.State special=specialSurface(s,c,biome,x,z);if(special!=null)return special;
        if(id==163&&s.surfaceNoiseAbove(x,z,1.75D))return STONE;
        if(id==131){if(s.surfaceNoiseAbove(x,z,2.0D))return GRAVEL;if(s.surfaceNoiseAbove(x,z,1.0D))return STONE;
            if(s.surfaceNoiseAbove(x,z,-1.0D))return DIRT;return GRAVEL;}
        if(id==FuturaBiomeIds120.MANGROVE_SWAMP)return MUD;
        return DIRT;
    }

    private static FuturaSurfaceRules120.State specialSurface(FuturaSurfaceSystem120 s,FuturaSurfaceContext120 c,
                                                               BiomeGenBase biome,int x,int z){
        int id=biome.biomeID;
        if(id==FuturaBiomeIds120.STONY_PEAKS)return s.noiseBetween(FuturaSurfaceSystem120.NOISE_CALCITE,x,z,-.0125D,.0125D)?CALCITE:STONE;
        if(biome==BiomeGenBase.stoneBeach)return s.noiseBetween(FuturaSurfaceSystem120.NOISE_GRAVEL,x,z,-.05D,.05D)
                ?(c.onCeiling()?STONE:GRAVEL):STONE;
        if(biome==BiomeGenBase.extremeHills&&s.surfaceNoiseAbove(x,z,1.0D))return STONE;
        if(isWarmOcean(biome)||id==BiomeGenBase.desert.biomeID)return c.onCeiling()?SANDSTONE:SAND;
        if(id==FuturaBiomeIds120.DRIPSTONE_CAVES)return STONE;
        return null;
    }

    private static boolean isBadlands(int id){return id==BiomeGenBase.mesa.biomeID||id==38||id==165;}
    private static boolean isWoodedBadlands(int id){return id==38;}
    private static boolean isSwamp(int id){return id==BiomeGenBase.swampland.biomeID;}
    private static boolean isFrozenOcean(BiomeGenBase biome){return biome==BiomeGenBase.frozenOcean||biome==OceanBiomeRegistry.deepFrozenOcean;}
    private static boolean isWarmOcean(BiomeGenBase biome){return biome==OceanBiomeRegistry.warmOcean||biome==OceanBiomeRegistry.lukewarmOcean||biome==OceanBiomeRegistry.deepLukewarmOcean;}
    private static boolean in(double value,double min,double max){return value>=min&&value<=max;}
    private static boolean surfaceBands(FuturaSurfaceSystem120 system,int x,int z){
        return system.surfaceNoiseBetween(x,z,-.909D,-.5454D)
                ||system.surfaceNoiseBetween(x,z,-.1818D,.1818D)
                ||system.surfaceNoiseBetween(x,z,.5454D,.909D);
    }
    private static FuturaSurfaceRules120.State state(Block block){return new FuturaSurfaceRules120.State(block,0);}
    private static FuturaSurfaceRules120.State clay(int metadata){return new FuturaSurfaceRules120.State(Blocks.stained_hardened_clay,metadata);}
    private FuturaOverworldSurfaceRules120(){}
}
