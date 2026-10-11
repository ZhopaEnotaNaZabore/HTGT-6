package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.world.ChunkCoordIntPair;

/** 1.20.2 placement math, separate from piece construction and saved starts. */
public final class FuturaStructurePlacement120 {
    private static final JsonObject DATA=load();
    private static final Set<OverworldBiomeParameterMap120.Biome> PREFERRED=preferredBiomes();
    public interface BiomeSearch { ChunkCoordIntPair find(int blockX,int blockZ,Random random); }
    private FuturaStructurePlacement120(){}
    public static boolean preferred(OverworldBiomeParameterMap120.Biome biome){
        return PREFERRED.contains(biome);
    }
    private static Set<OverworldBiomeParameterMap120.Biome> preferredBiomes(){
        Set<OverworldBiomeParameterMap120.Biome> result=EnumSet.noneOf(OverworldBiomeParameterMap120.Biome.class);
        for(JsonElement e:DATA.getAsJsonArray("stronghold_biased_to"))result.add(OverworldBiomeParameterMap120.Biome.valueOf(e.getAsString().substring(10).toUpperCase(Locale.ROOT)));
        return result;
    }
    public static List<ChunkCoordIntPair> strongholds(long seed,BiomeSearch search){
        JsonObject p=DATA.getAsJsonObject("strongholds").getAsJsonObject("placement");
        int count=p.get("count").getAsInt(),distance=p.get("distance").getAsInt(),spread=p.get("spread").getAsInt();
        Random random=new Random(seed);double angle=random.nextDouble()*Math.PI*2;
        int inRing=0,ring=0;List<ChunkCoordIntPair> result=new ArrayList<ChunkCoordIntPair>(count);
        for(int i=0;i<count;i++){
            double radius=4*distance+distance*ring*6+(random.nextDouble()-.5)*(distance*2.5);
            int x=(int)Math.round(Math.cos(angle)*radius),z=(int)Math.round(Math.sin(angle)*radius);
            Random fork=new Random(random.nextLong());
            ChunkCoordIntPair preferred=search==null?null:search.find((x<<4)+8,(z<<4)+8,fork);
            result.add(preferred==null?new ChunkCoordIntPair(x,z):preferred);
            angle+=Math.PI*2/spread;
            if(++inRing==spread){inRing=0;spread+=2*spread/(++ring+1);spread=Math.min(spread,count-i);angle+=random.nextDouble()*Math.PI*2;}
        }
        return result;
    }
    public static boolean mineshaft(long seed,int x,int z){
        float frequency=DATA.getAsJsonObject("mineshafts").getAsJsonObject("placement").get("frequency").getAsFloat();
        return WorldgenRandom120.carver(seed,0,x,z).nextDouble()<(double)frequency;
    }
    public static boolean scattered(long seed,int x,int z,String set){
        JsonObject p=DATA.getAsJsonObject(set).getAsJsonObject("placement");
        int spacing=p.get("spacing").getAsInt(),range=spacing-p.get("separation").getAsInt();
        int rx=Math.floorDiv(x,spacing),rz=Math.floorDiv(z,spacing);
        Random random=new Random(seed+(long)rx*341873128712L+(long)rz*132897987541L+p.get("salt").getAsInt());
        return x==rx*spacing+random.nextInt(range)&&z==rz*spacing+random.nextInt(range);
    }
    /** StructurePiecesBuilder.moveBelowSeaLevel after translating minY=-64 to zero. */
    public static int undergroundTop(Random random,int span){
        int top=span+1,limit=127-10;
        if(top<limit)top+=random.nextInt(limit-top);
        return top;
    }
    private static JsonObject load(){
        try(InputStream in=FuturaStructurePlacement120.class.getResourceAsStream("/assets/htgt6/worldgen/futura_structure_placements_1202.json")){
            if(in==null)throw new FileNotFoundException("Futura structure placements");
            return new JsonParser().parse(new InputStreamReader(in,"UTF-8")).getAsJsonObject();
        }catch(IOException e){throw new ExceptionInInitializerError(e);}
    }
}
