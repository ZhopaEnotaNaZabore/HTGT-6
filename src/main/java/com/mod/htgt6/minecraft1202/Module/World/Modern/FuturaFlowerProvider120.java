package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/** State provider codecs used by the seven pinned Overworld flower configurations. */
public final class FuturaFlowerProvider120 {
    private static final ConcurrentHashMap<String,NormalNoise120> NOISE=new ConcurrentHashMap<String,NormalNoise120>();
    private FuturaFlowerProvider120() {}
    public static JsonObject state(Random r,JsonObject p,int x,int physicalY,int z) {
        String type=p.get("type").getAsString();int y=physicalY-64;
        if(type.equals("minecraft:simple_state_provider"))return p.getAsJsonObject("state");
        if(type.equals("minecraft:weighted_state_provider")) {
            JsonArray entries=p.getAsJsonArray("entries");int total=0;
            for(JsonElement e:entries)total+=e.getAsJsonObject().get("weight").getAsInt();
            int choice=r.nextInt(total);
            for(JsonElement e:entries){JsonObject v=e.getAsJsonObject();choice-=v.get("weight").getAsInt();if(choice<0)return v.getAsJsonObject("data");}
            throw new IllegalArgumentException("Empty weighted provider");
        }
        double scale=(double)p.get("scale").getAsFloat();
        double noise=noise(p,"noise").sample(x*scale,y*scale,z*scale);
        if(type.equals("minecraft:noise_provider"))return choose(p.getAsJsonArray("states"),noise);
        if(type.equals("minecraft:noise_threshold_provider")) {
            if(noise<(double)p.get("threshold").getAsFloat())return random(r,p.getAsJsonArray("low_states"));
            return r.nextFloat()<p.get("high_chance").getAsFloat()?random(r,p.getAsJsonArray("high_states")):p.getAsJsonObject("default_state");
        }
        if(type.equals("minecraft:dual_noise_provider")) {
            float slowScale=p.get("slow_scale").getAsFloat();NormalNoise120 slow=noise(p,"slow_noise");
            double value=slow.sample((double)(x*slowScale),(double)(y*slowScale),(double)(z*slowScale));
            JsonArray variety=p.getAsJsonArray("variety");int min=variety.get(0).getAsInt(),max=variety.get(1).getAsInt();
            int size=(int)(min+Math.max(0,Math.min(1,(value+1)/2))*(max+1-min));
            JsonArray palette=new JsonArray();
            for(int i=0;i<size;i++)palette.add(choose(p.getAsJsonArray("states"),slow.sample(
                    (double)((x+i*54545)*slowScale),(double)(y*slowScale),(double)((z+i*34234)*slowScale))));
            return choose(palette,noise);
        }
        throw new IllegalArgumentException("Unsupported flower provider: "+type);
    }
    private static JsonObject random(Random r,JsonArray values){return values.get(r.nextInt(values.size())).getAsJsonObject();}
    private static JsonObject choose(JsonArray values,double noise){return values.get((int)(Math.max(0,Math.min(.9999,(1+noise)/2))*values.size())).getAsJsonObject();}
    private static NormalNoise120 noise(JsonObject provider,String field) {
        String key=provider.get("seed").toString()+":"+provider.get(field).toString();
        NormalNoise120 cached=NOISE.get(key);if(cached!=null)return cached;
        JsonObject params=provider.getAsJsonObject(field);JsonArray values=params.getAsJsonArray("amplitudes");
        double[] amplitudes=new double[values.size()];for(int i=0;i<amplitudes.length;i++)amplitudes[i]=values.get(i).getAsDouble();
        NormalNoise120 created=new NormalNoise120(new Random(provider.get("seed").getAsLong()),params.get("firstOctave").getAsInt(),amplitudes);
        NormalNoise120 previous=NOISE.putIfAbsent(key,created);return previous==null?created:previous;
    }
}
