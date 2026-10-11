package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaClimateSampler120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.OverworldBiomeParameterMap120;

import java.util.Random;

/** Fixed-seed climate and OverworldBiomeBuilder regression. */
public final class FuturaClimateFixtures120 {
    private static final int EXPECTED_ENTRIES=7543;
    private static final long EXPECTED_FINGERPRINT=0xAB828CE09073F45EL;
    private FuturaClimateFixtures120() { }
    public static void main(String[] arguments){
        FuturaClimateSampler120 sampler=new FuturaClimateSampler120(0x1234ABCD5678EF90L);
        OverworldBiomeParameterMap120 map=new OverworldBiomeParameterMap120();
        int[][] points={{0,0},{17,-33},{-1024,2048},{30000000,-30000000}};
        long hash=0xcbf29ce484222325L;
        for(int[] point:points){
            FuturaClimateSampler120.Point climate=sampler.sampleSurface(point[0],point[1]);
            hash=mix(hash,Float.floatToIntBits(climate.temperature));
            hash=mix(hash,Float.floatToIntBits(climate.humidity));
            hash=mix(hash,Float.floatToIntBits(climate.continentalness));
            hash=mix(hash,Float.floatToIntBits(climate.erosion));
            hash=mix(hash,Float.floatToIntBits(climate.depth));
            hash=mix(hash,Float.floatToIntBits(climate.weirdness));
            hash=mix(hash,map.select(climate).ordinal());
        }
        if(map.entries().size()!=EXPECTED_ENTRIES)throw new IllegalStateException(
                "OverworldBiomeBuilder entry count: expected "+EXPECTED_ENTRIES+", got "+map.entries().size());
        if(hash!=EXPECTED_FINGERPRINT)throw new IllegalStateException("Climate fixture changed: expected 0x"
                +Long.toHexString(EXPECTED_FINGERPRINT)+", got 0x"+Long.toHexString(hash));
        FuturaClimateSampler120.Point surface=sampler.sample(17,127,-33,0.0F);
        FuturaClimateSampler120.Point shiftedY=sampler.sample(17,63,-33,0.0F);
        if(Float.floatToIntBits(surface.temperature)!=Float.floatToIntBits(shiftedY.temperature)
                ||Float.floatToIntBits(surface.humidity)!=Float.floatToIntBits(shiftedY.humidity))
            throw new IllegalStateException("Climate noises must keep yScale=0 after Futura Y translation");
        assertBiome(map,point(0,0,-1.1F,0,0,0),OverworldBiomeParameterMap120.Biome.MUSHROOM_FIELDS);
        assertBiome(map,point(.8F,0,-.3F,0,0,0),OverworldBiomeParameterMap120.Biome.WARM_OCEAN);
        assertBiome(map,point(0,0,-.8F,0,0,0),OverworldBiomeParameterMap120.Biome.DEEP_OCEAN);
        assertBiome(map,point(0,0,.9F,0,.5F,0),OverworldBiomeParameterMap120.Biome.DRIPSTONE_CAVES);
        assertBiome(map,point(0,.8F,0,0,.5F,0),OverworldBiomeParameterMap120.Biome.LUSH_CAVES);
        assertBiome(map,point(0,0,0,-.9F,1.1F,0),OverworldBiomeParameterMap120.Biome.DEEP_DARK);
        assertIndexedLookupEquivalent(map);
        validateWorldFamilies();
        OverworldBiomeParameterMap120.LookupStatistics statistics=map.lookupStatistics();
        System.out.println("Climate fixture OK; entries="+map.entries().size()+", fingerprint=0x"+Long.toHexString(hash)
                +", indexed average tested="+statistics.averageTestedEntries()+", nodes="+statistics.visitedNodes);
    }
    private static FuturaClimateSampler120.Point point(float t,float h,float c,float e,float d,float w){
        return new FuturaClimateSampler120.Point(t,h,c,e,d,w);}
    private static void assertBiome(OverworldBiomeParameterMap120 map,FuturaClimateSampler120.Point point,
                                    OverworldBiomeParameterMap120.Biome expected){
        OverworldBiomeParameterMap120.Biome actual=map.select(point);
        if(actual!=expected)throw new IllegalStateException("Expected "+expected+", got "+actual);
    }
    private static void assertIndexedLookupEquivalent(OverworldBiomeParameterMap120 map){
        map.resetLookupStatistics();
        int checked=0;
        // Every parameter entry contributes a representative point, including
        // duplicated/tied boxes where original insertion order must win.
        for(OverworldBiomeParameterMap120.Entry entry:map.entries()){
            assertEquivalent(map,entryPoint(entry));checked++;
            assertEquivalent(map,entryCorner(entry,false));checked++;
            assertEquivalent(map,entryCorner(entry,true));checked++;
        }
        Random random=new Random(0x1202B10BE5EEDL);
        for(int i=0;i<8192;i++){
            assertEquivalent(map,point(randomValue(random,1.25F),randomValue(random,1.25F),
                    randomValue(random,1.25F),randomValue(random,1.25F),
                    randomValue(random,1.5F),randomValue(random,1.25F)));
            checked++;
        }
        OverworldBiomeParameterMap120.LookupStatistics statistics=map.lookupStatistics();
        if(statistics.lookups!=checked)throw new IllegalStateException("Lookup counter mismatch");
        if(statistics.averageTestedEntries()>=statistics.totalEntries*0.5D)
            throw new IllegalStateException("Climate index pruned too little: average tested="
                    +statistics.averageTestedEntries()+" of "+statistics.totalEntries);
    }
    private static void assertEquivalent(OverworldBiomeParameterMap120 map,FuturaClimateSampler120.Point point){
        OverworldBiomeParameterMap120.Biome indexed=map.select(point),linear=map.selectLinear(point);
        if(indexed!=linear)throw new IllegalStateException("Indexed climate lookup differs from linear scan: indexed="
                +indexed+", linear="+linear+", point="+point.temperature+","+point.humidity+","
                +point.continentalness+","+point.erosion+","+point.depth+","+point.weirdness);
    }
    private static FuturaClimateSampler120.Point entryPoint(OverworldBiomeParameterMap120.Entry entry){
        return point(center(entry.temperature),center(entry.humidity),center(entry.continentalness),
                center(entry.erosion),center(entry.depth),center(entry.weirdness));
    }
    private static FuturaClimateSampler120.Point entryCorner(OverworldBiomeParameterMap120.Entry entry,
                                                              boolean maximum){
        return point(endpoint(entry.temperature,maximum),endpoint(entry.humidity,maximum),
                endpoint(entry.continentalness,maximum),endpoint(entry.erosion,maximum),
                endpoint(entry.depth,maximum),endpoint(entry.weirdness,maximum));
    }
    private static float center(OverworldBiomeParameterMap120.Range range){
        return (range.min+(range.max-range.min)/2L)/10000.0F;
    }
    private static float endpoint(OverworldBiomeParameterMap120.Range range,boolean maximum){
        return (maximum?range.max:range.min)/10000.0F;
    }
    private static float randomValue(Random random,float magnitude){return (random.nextFloat()*2.0F-1.0F)*magnitude;}
    private static void validateWorldFamilies(){
        FuturaClimateSampler120 sampler=new FuturaClimateSampler120(0x465554555241120L);
        OverworldBiomeParameterMap120 map=new OverworldBiomeParameterMap120();
        boolean ocean=false,land=false,extended=false;
        for(int z=-8192;z<=8192;z+=1021)for(int x=-8192;x<=8192;x+=997){
            OverworldBiomeParameterMap120.Biome biome=map.select(sampler.sampleSurface(x,z));
            boolean isOcean=biome.name().contains("OCEAN");
            ocean|=isOcean;land|=!isOcean;
            switch(biome){case BAMBOO_JUNGLE:case MANGROVE_SWAMP:case MEADOW:case CHERRY_GROVE:
                case GROVE:case SNOWY_SLOPES:case FROZEN_PEAKS:case JAGGED_PEAKS:case STONY_PEAKS:
                    extended=true;break;default:break;}
        }
        if(!ocean||!land||!extended)throw new IllegalStateException("Fixed-seed climate lacks required families: ocean="
                +ocean+", land="+land+", extended="+extended);
    }
    private static long mix(long hash,long value){return (hash^value)*0x100000001b3L;}
}
