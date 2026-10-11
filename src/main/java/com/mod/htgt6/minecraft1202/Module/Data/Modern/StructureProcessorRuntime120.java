package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;

import com.mod.htgt6.minecraft1202.Module.World.Modern.BackportResourceLoader120;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Executable subset exactly covering the rule processors referenced by plains village pools. */
public final class StructureProcessorRuntime120 {
    private static final Map<String,List<Rule>> CACHE=new HashMap<String,List<Rule>>();
    private static final Map<String,List<Processor>> PIPELINES=new HashMap<String,List<Processor>>();

    public static State apply(List<String> processorIds,State input,State existing,int x,int y,int z){
        State result=input;
        for(String id:processorIds){
            for(Processor processor:loadPipeline(id)){
                result=processor.apply(result,existing,new Random(positionalSeed(x,y,z)));
                if(result==null)return null;
            }
        }
        return result;
    }

    public static synchronized List<Rule> load(String id){
        loadPipeline(id);return CACHE.get(normalize(id));
    }

    private static synchronized List<Processor> loadPipeline(String id){
        id=normalize(id);List<Processor> cached=PIPELINES.get(id);if(cached!=null)return cached;
        String path="/assets/htgt6/worldgen/processor_list/"+stripNamespace(id)+".json";
        InputStream input=BackportResourceLoader120.open(StructureProcessorRuntime120.class,path);
        if(input==null)throw new JigsawPlacementRuntime120.MissingDataException("Missing processor list "+id);
        try{
            JsonObject root=new JsonParser().parse(new InputStreamReader(input,"UTF-8")).getAsJsonObject();
            List<Rule> rules=new ArrayList<Rule>();
            List<Processor> processors=new ArrayList<Processor>();
            for(JsonElement processorValue:root.getAsJsonArray("processors")){
                JsonObject processor=processorValue.getAsJsonObject();
                String type=processor.get("processor_type").getAsString();
                if("minecraft:rule".equals(type)){
                    List<Rule> group=new ArrayList<Rule>();
                    for(JsonElement ruleValue:processor.getAsJsonArray("rules")){
                        JsonObject rule=ruleValue.getAsJsonObject();Rule parsed=new Rule(predicate(rule.getAsJsonObject("input_predicate")),
                            predicate(rule.getAsJsonObject("location_predicate")),
                            state(rule.getAsJsonObject("output_state")));rules.add(parsed);group.add(parsed);
                    }
                    processors.add(new Rules(group));
                }else if("minecraft:block_rot".equals(type)){
                    String tag=processor.has("rottable_blocks")
                            ?processor.get("rottable_blocks").getAsString():null;
                    if(tag!=null&&!"#minecraft:ancient_city_replaceable".equals(tag))
                        throw new IllegalStateException("Unsupported block-rot tag "+tag);
                    processors.add(new BlockRot(processor.get("integrity").getAsFloat(),tag==null));
                }else if("minecraft:protected_blocks".equals(type)){
                    processors.add(Protected.INSTANCE);
                }else throw new IllegalStateException("Unsupported processor type "+type);
                }
            List<Rule> result=Collections.unmodifiableList(rules);CACHE.put(id,result);
            List<Processor> pipeline=Collections.unmodifiableList(processors);PIPELINES.put(id,pipeline);return pipeline;
        }catch(Exception error){throw new IllegalStateException("Cannot parse processor list "+id,error);}
        finally{try{input.close();}catch(IOException ignored){}}
    }

    private static Predicate predicate(JsonObject value){
        String type=value.get("predicate_type").getAsString();
        if("minecraft:always_true".equals(type))return new Always();
        if("minecraft:block_match".equals(type))return new BlockMatch(value.get("block").getAsString(),1.0F,false);
        if("minecraft:random_block_match".equals(type))return new BlockMatch(value.get("block").getAsString(),
                value.get("probability").getAsFloat(),true);
        if("minecraft:blockstate_match".equals(type))return new StateMatch(state(value.getAsJsonObject("block_state")));
        if("minecraft:tag_match".equals(type)){
            String tag=value.get("tag").getAsString();
            if(!"minecraft:doors".equals(tag))throw new IllegalStateException("Unsupported processor tag "+tag);
            return new DoorsTag();
        }
        throw new IllegalStateException("Unsupported rule predicate "+type);
    }

    private static State state(JsonObject value){
        Map<String,String> properties=new HashMap<String,String>();
        if(value.has("Properties"))for(Map.Entry<String,JsonElement> entry:value.getAsJsonObject("Properties").entrySet())
            properties.put(entry.getKey(),entry.getValue().getAsString());
        return new State(normalize(value.get("Name").getAsString()),properties);
    }

    /** StructurePlaceSettings positional random source used by rule processors. */
    public static long positionalSeed(int x,int y,int z){
        long value=(long)(x*3129871)^((long)z*116129781L)^(long)y;
        value=value*value*42317861L+value*11L;
        return value>>16;
    }

    public static final class State {
        public final String name;
        public final Map<String,String> properties;
        public State(String name,Map<String,String> properties){this.name=normalize(name);
            this.properties=Collections.unmodifiableMap(new HashMap<String,String>(properties));}
        public State(String name){this(name,Collections.<String,String>emptyMap());}
    }
    public static final class Rule {final Predicate input,location;final State output;
        Rule(Predicate input,Predicate location,State output){this.input=input;this.location=location;this.output=output;}}
    private interface Predicate {boolean matches(State state,Random random);}
    private interface Processor {State apply(State input,State existing,Random random);}
    private static final class Rules implements Processor {final List<Rule> rules;Rules(List<Rule> rules){this.rules=rules;}
        public State apply(State input,State existing,Random random){for(Rule rule:rules)
            if(rule.input.matches(input,random)&&rule.location.matches(existing,random))return rule.output;return input;}}
    private static final class BlockRot implements Processor {final float integrity;final boolean allBlocks;
        BlockRot(float integrity,boolean allBlocks){this.integrity=integrity;this.allBlocks=allBlocks;}
        public State apply(State input,State existing,Random random){return input!=null&&(allBlocks||ancientReplaceable(input.name))
                &&random.nextFloat()>integrity?null:input;}}
    private enum Protected implements Processor {INSTANCE;public State apply(State input,State existing,Random random){
        if(existing==null)return input;String path=stripNamespace(existing.name);
        return "bedrock".equals(path)||"spawner".equals(path)||"chest".equals(path)
                ||"end_portal_frame".equals(path)||"reinforced_deepslate".equals(path)?null:input;}}
    private static final class Always implements Predicate {public boolean matches(State state,Random random){return true;}}
    private static final class BlockMatch implements Predicate {final String block;final float probability;final boolean random;
        BlockMatch(String block,float probability,boolean random){this.block=normalize(block);this.probability=probability;this.random=random;}
        public boolean matches(State state,Random source){return state!=null&&block.equals(state.name)&&(!random||source.nextFloat()<probability);}}
    private static final class StateMatch implements Predicate {final State expected;StateMatch(State expected){this.expected=expected;}
        public boolean matches(State state,Random random){return state!=null&&expected.name.equals(state.name)&&expected.properties.equals(state.properties);}}
    private static final class DoorsTag implements Predicate {public boolean matches(State state,Random random){
        String path=state==null?"":stripNamespace(state.name);return path.endsWith("_door")||"iron_door".equals(path);}}
    private static String normalize(String id){return id.indexOf(':')<0?"minecraft:"+id:id;}
    private static boolean ancientReplaceable(String name){String path=stripNamespace(name);
        return "deepslate".equals(path)||"deepslate_bricks".equals(path)||"deepslate_tiles".equals(path)
                ||"deepslate_brick_slab".equals(path)||"deepslate_tile_slab".equals(path)
                ||"deepslate_brick_stairs".equals(path)||"deepslate_tile_wall".equals(path)
                ||"deepslate_brick_wall".equals(path)||"cobbled_deepslate".equals(path)
                ||"cracked_deepslate_bricks".equals(path)||"cracked_deepslate_tiles".equals(path)
                ||"gray_wool".equals(path);}
    private static String stripNamespace(String id){int split=id.indexOf(':');return split<0?id:id.substring(split+1);}
    private StructureProcessorRuntime120(){ }
}
