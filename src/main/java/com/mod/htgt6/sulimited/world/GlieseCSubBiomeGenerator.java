package com.mod.htgt6.sulimited.world;
import static com.mod.htgt6.sulimited.world.GlieseTerrain.*;
/** Region labels never control the continuous bathymetry. */
public final class GlieseCSubBiomeGenerator {
    public enum Type { DEEP_SEA,SEA,REEF,BEACHES,LOWLANDS,ISLANDS,LOW_SEA,UNDERWATER_VOLCANO,DEEP_REEF,DEEP_CRATER,MOSSY_HILLS }
    public static final class Sample {
        public final int height; public final Type type; public final boolean vent;
        Sample(double h,Type t,boolean v) { height=(int)Math.round(h); type=t; vent=v; }
    }
    public Sample sample(long seed,int x,int z,int seaLevel) {
        // Continental shelf rises gently to low land; smaller radial fields form islands.
        double c=noise(seed^0x4F1BBCDCBFA54021L,x,z,1050),detail=noise(seed+23,x,z,100);
        double h=seaLevel-112+smooth(-0.42,0.43,c)*123+detail*5;
        double island=radial(seed+41,x,z,320,3,85)*(1-smooth(0.22,0.42,c));
        h=lerp(h,seaLevel+5+detail*3,smooth(0,0.86,island));
        double hills=smooth(0.34,0.65,c)*smooth(-0.4,0.65,noise(seed+59,x,z,190)); h+=hills*12;
        // Impact basins include a subdued rim. Volcanic summits stay submerged.
        double crater=radial(seed+71,x,z,448,3,110)*(1-smooth(0,0.32,c)); h+=smooth(0,0.18,crater)*(1-smooth(0.18,0.4,crater))*7-smooth(0.15,0.8,crater)*28;
        double volcano=radial(seed+97,x,z,384,7,100)*(1-smooth(0.1,0.4,c));
        h+=Math.min(smooth(0,0.82,volcano)*59-smooth(0.82,0.97,volcano)*18,Math.max(0,seaLevel-4-h));
        h=Math.max(14,Math.min(seaLevel+28,h)); double reef=noise(seed+113,x,z,115); Type type;
        if(h>=seaLevel-2 && h<=seaLevel+2) type=Type.BEACHES;
        else if(h>seaLevel) type=hills>0.25?Type.MOSSY_HILLS:island>0.6?Type.ISLANDS:Type.LOWLANDS;
        else if(volcano>0.2) type=Type.UNDERWATER_VOLCANO;
        else if(crater>0.5) type=Type.DEEP_CRATER;
        else if(h<seaLevel-72) type=reef>0.05?Type.DEEP_REEF:Type.DEEP_SEA;
        else if(h>seaLevel-35 && reef>0.05) type=Type.REEF;
        else if(h>seaLevel-18) type=Type.LOW_SEA; else type=Type.SEA;
        return new Sample(h,type,volcano>0.88);
    }
    public Type getBiome(long s,int x,int z) { return sample(s,x,z,160).type; }
    public int getOceanFloorY(long s,int x,int z,Type ignored,int sea) { return sample(s,x,z,sea).height; }
    public int getVolcanoSurfaceY(long s,int x,int z,int sea) { Sample a=sample(s,x,z,sea); return a.type==Type.UNDERWATER_VOLCANO?a.height:-1; }
}
