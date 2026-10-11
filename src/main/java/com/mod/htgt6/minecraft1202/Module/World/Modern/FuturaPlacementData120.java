package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import java.io.*;
import java.util.*;

/** Pinned vanilla JSON plus FeatureSorter ordering; no Minecraft runtime dependency. */
public final class FuturaPlacementData120 {
    public static final FuturaPlacementData120 INSTANCE=new FuturaPlacementData120();
    private final JsonObject data;
    private final List<List<String>> steps=new ArrayList<List<String>>();
    private final Map<String,Integer> indices=new HashMap<String,Integer>();

    private FuturaPlacementData120(){
        InputStream stream=getClass().getResourceAsStream("/assets/htgt6/worldgen/futura_placements_1202.json");
        if(stream==null)throw new IllegalStateException("Missing vanilla 1.20.2 placement data");
        try(Reader reader=new InputStreamReader(stream,"UTF-8")){
            data=new JsonParser().parse(reader).getAsJsonObject();
        }catch(IOException e){throw new IllegalStateException(e);}
        LinkedHashSet<String> biomes=new LinkedHashSet<String>();
        for(OverworldBiomeParameterMap120.Entry e:new OverworldBiomeParameterMap120().entries())
            biomes.add(e.biome.name().toLowerCase(Locale.ROOT));
        final Map<String,Integer> ids=new HashMap<String,Integer>();
        Comparator<String> order=new Comparator<String>(){
            public int compare(String a,String b){
                int step=Integer.compare(step(a),step(b));
                return step!=0?step:Integer.compare(ids.get(name(a)),ids.get(name(b)));
            }
        };
        Map<String,SortedSet<String>> graph=new TreeMap<String,SortedSet<String>>(order);
        for(String biome:biomes){
            JsonArray stages=data.getAsJsonObject("biomes").getAsJsonArray(biome);
            String previous=null;
            for(int s=0;s<stages.size();s++)for(JsonElement feature:stages.get(s).getAsJsonArray()){
                String key=feature.getAsString();
                if(!ids.containsKey(key))ids.put(key,ids.size());
                String node=s+"/"+key;
                if(!graph.containsKey(node))graph.put(node,new TreeSet<String>(order));
                if(previous!=null)graph.get(previous).add(node);
                previous=node;
            }
        }
        List<String> sorted=new ArrayList<String>();
        Set<String> visited=new HashSet<String>(),active=new HashSet<String>();
        for(String node:graph.keySet())visit(node,graph,visited,active,sorted);
        Collections.reverse(sorted);
        for(int s=0;s<11;s++)steps.add(new ArrayList<String>());
        for(String node:sorted){
            List<String> list=steps.get(step(node));
            indices.put(node,list.size());list.add(name(node));
        }
    }
    private static int step(String node){return Integer.parseInt(node.substring(0,node.indexOf('/')));}
    private static String name(String node){return node.substring(node.indexOf('/')+1);}
    private static void visit(String node,Map<String,SortedSet<String>> graph,Set<String> done,
                              Set<String> active,List<String> out){
        if(done.contains(node))return;
        if(!active.add(node))throw new IllegalStateException("Vanilla feature cycle: "+node);
        for(String next:graph.get(node))visit(next,graph,done,active,out);
        active.remove(node);done.add(node);out.add(node);
    }
    public List<String> features(int step){return Collections.unmodifiableList(steps.get(step));}
    public int index(int step,String name){
        Integer index=indices.get(step+"/"+key(name));
        if(index==null)throw new IllegalArgumentException("Unregistered placed feature: "+name+" at "+step);
        return index;
    }
    public Random random(long seed,int chunkX,int chunkZ,int step,String name){
        return new WorldgenRandom120(WorldgenRandom120.featureSeed(
                WorldgenRandom120.decorationSeed(seed,chunkX<<4,chunkZ<<4),step,index(step,name)));
    }
    public JsonObject placed(String name){return data.getAsJsonObject("placed").getAsJsonObject(key(name));}
    public JsonObject configured(String name){return data.getAsJsonObject("configured").getAsJsonObject(key(name));}
    public boolean has(OverworldBiomeParameterMap120.Biome biome,int step,String feature){
        JsonArray list=data.getAsJsonObject("biomes").getAsJsonArray(biome.name().toLowerCase(Locale.ROOT))
                .get(step).getAsJsonArray();
        for(JsonElement entry:list)if(entry.getAsString().equals(key(feature)))return true;
        return false;
    }
    public static String key(String name){return name.indexOf(':')>=0?name:"minecraft:"+name;}

    /** Resolve anchors in modern coordinates. Never clip before sampling. */
    public static int anchor(JsonObject value,int height){
        if(value.has("absolute"))return value.get("absolute").getAsInt();
        if(value.has("above_bottom"))return -64+value.get("above_bottom").getAsInt();
        return height-65-value.get("below_top").getAsInt();
    }
    public static int sampleHeight(Random random,JsonObject height,int buildHeight){
        int min=anchor(height.getAsJsonObject("min_inclusive"),buildHeight);
        int max=anchor(height.getAsJsonObject("max_inclusive"),buildHeight);
        String type=height.get("type").getAsString();
        int range=max-min;
        if(range<0)return min+64;
        int result;
        if(type.equals("minecraft:trapezoid")){
            int plateau=height.has("plateau")?height.get("plateau").getAsInt():0;
            int lower=(range-plateau)/2,upper=range-lower;
            result=plateau>=range?min+random.nextInt(range+1)
                    :min+random.nextInt(upper+1)+random.nextInt(lower+1);
        }else if(type.equals("minecraft:very_biased_to_bottom")){
            int inner=height.has("inner")?height.get("inner").getAsInt():1;
            int a=random.nextInt(range-inner+1)+min+inner;
            int b=random.nextInt(a-min)+min;
            result=random.nextInt(b-min+inner)+min;
        }else if(type.equals("minecraft:uniform"))result=min+random.nextInt(range+1);
        else throw new IllegalArgumentException("Unsupported height provider: "+type);
        return result+64;
    }
    public static int count(Random random,JsonElement count){
        if(count.isJsonPrimitive())return count.getAsInt();
        JsonObject value=count.getAsJsonObject();
        String type=value.get("type").getAsString();
        if(type.equals("minecraft:weighted_list")){
            int total=0;for(JsonElement e:value.getAsJsonArray("distribution"))total+=e.getAsJsonObject().get("weight").getAsInt();
            int selected=random.nextInt(total);
            for(JsonElement e:value.getAsJsonArray("distribution")){
                JsonObject entry=e.getAsJsonObject();selected-=entry.get("weight").getAsInt();
                if(selected<0)return count(random,entry.get("data"));
            }
            throw new IllegalStateException("Empty weighted count");
        }
        if(value.has("value"))value=value.getAsJsonObject("value");
        int min=value.get("min_inclusive").getAsInt(),max=value.get("max_inclusive").getAsInt();
        if(type.equals("minecraft:clamped"))return Math.max(min,Math.min(max,count(random,value.get("source"))));
        if(type.equals("minecraft:biased_to_bottom"))return min+random.nextInt(random.nextInt(max-min+1)+1);
        return min+random.nextInt(max-min+1);
    }
}
