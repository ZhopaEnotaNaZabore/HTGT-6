package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mod.htgt6.minecraft1202.Module.World.Modern.BackportResourceLoader120;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Loader for the unmodified 1.20.2 village pools and structure-template jigsaws. */
public final class VillageJigsawRegistry120 implements JigsawPlacementRuntime120.Registry {
    public static final String PLAINS_START="minecraft:village/plains/town_centers";
    private static final String[] STYLES={"plains","desert","savanna","snowy","taiga"};
    /** Official 1.20.2 files retained by the data set but not referenced by a pool. */
    private static final String[] UNREFERENCED_TEMPLATES={
            "minecraft:village/decays/grass_11x13","minecraft:village/decays/grass_16x16",
            "minecraft:village/decays/grass_9x9","minecraft:village/snowy/streets/crossroad_01",
            "minecraft:village/snowy/zombie/streets/crossroad_01"
    };
    private final Map<String,JigsawPlacementRuntime120.Pool> pools=
            new HashMap<String,JigsawPlacementRuntime120.Pool>();
    private final Map<String,JigsawPlacementRuntime120.Template> templates=
            new HashMap<String,JigsawPlacementRuntime120.Template>();
    private final Set<String> featureTypes=new HashSet<String>();
    private final Set<String> entityTypes=new HashSet<String>();
    private int entityCount;
    private int legacyElementCount;
    private Field tagListField;

    @Override public synchronized JigsawPlacementRuntime120.Pool pool(String id){
        id=normalize(id);
        if("minecraft:empty".equals(id))return new JigsawPlacementRuntime120.Pool(id,id,
                Collections.<JigsawPlacementRuntime120.Weighted>emptyList());
        JigsawPlacementRuntime120.Pool cached=pools.get(id);
        if(cached!=null)return cached;
        String path="/assets/htgt6/worldgen/template_pool/"+stripNamespace(id)+".json";
        InputStream input=BackportResourceLoader120.open(VillageJigsawRegistry120.class,path);
        if(input==null)return null;
        try{
            JsonObject root=new JsonParser().parse(new InputStreamReader(input,"UTF-8")).getAsJsonObject();
            String fallback=normalize(root.get("fallback").getAsString());
            List<JigsawPlacementRuntime120.Weighted> entries=new ArrayList<JigsawPlacementRuntime120.Weighted>();
            for(JsonElement value:root.getAsJsonArray("elements")){
                JsonObject weighted=value.getAsJsonObject();
                entries.add(new JigsawPlacementRuntime120.Weighted(parseElement(weighted.getAsJsonObject("element")),
                        weighted.get("weight").getAsInt()));
            }
            JigsawPlacementRuntime120.Pool result=new JigsawPlacementRuntime120.Pool(id,fallback,entries);
            pools.put(id,result);return result;
        }catch(Exception error){throw new IllegalStateException("Cannot parse official template pool "+path,error);}
        finally{try{input.close();}catch(IOException ignored){}}
    }

    private JigsawPlacementRuntime120.Element parseElement(JsonObject value){
        String type=value.get("element_type").getAsString();
        if("minecraft:empty_pool_element".equals(type))return JigsawPlacementRuntime120.Element.empty();
        if("minecraft:feature_pool_element".equals(type)){
            String feature=normalize(value.get("feature").getAsString());
            featureTypes.add(feature);
            String projection=value.has("projection")?value.get("projection").getAsString():"rigid";
            return JigsawPlacementRuntime120.Element.feature(feature,
                    "terrain_matching".equals(projection)?JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING:
                            JigsawPlacementRuntime120.Projection.RIGID);
        }
        if(!"minecraft:legacy_single_pool_element".equals(type)
                &&!"minecraft:single_pool_element".equals(type))
            throw new IllegalStateException("Unsupported official pool element "+type);
        String location=normalize(value.get("location").getAsString());
        String projection=value.get("projection").getAsString();
        boolean legacy="minecraft:legacy_single_pool_element".equals(type);
        if(legacy)legacyElementCount++;
        return new JigsawPlacementRuntime120.Element(location,
                "terrain_matching".equals(projection)?JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING
                        :JigsawPlacementRuntime120.Projection.RIGID,processors(value.get("processors")),
                legacy);
    }

    private List<String> processors(JsonElement value){
        if(value==null||value.isJsonNull())return Collections.emptyList();
        if(value.isJsonPrimitive()){
            String id=normalize(value.getAsString());
            validateProcessor(id);
            return Collections.singletonList(id);
        }
        JsonArray inline=value.getAsJsonObject().getAsJsonArray("processors");
        if(inline==null||inline.size()==0)return Collections.emptyList();
        throw new IllegalStateException("Inline non-empty processor lists are not supported by village loader");
    }

    private void validateProcessor(String id){
        String path="/assets/htgt6/worldgen/processor_list/"+stripNamespace(id)+".json";
        InputStream input=BackportResourceLoader120.open(VillageJigsawRegistry120.class,path);
        if(input==null)throw new JigsawPlacementRuntime120.MissingDataException("Missing processor list "+id);
        try{new JsonParser().parse(new InputStreamReader(input));}
        finally{try{input.close();}catch(IOException ignored){}}
    }

    @Override public synchronized JigsawPlacementRuntime120.Template template(String id){
        id=normalize(id);
        JigsawPlacementRuntime120.Template cached=templates.get(id);
        if(cached!=null)return cached;
        String relative=stripNamespace(id);
        if(!relative.startsWith("village/"))return null;
        String path="/assets/htgt6/structures/village_1_20/"+relative.substring("village/".length())+".nbt";
        InputStream input=BackportResourceLoader120.open(VillageJigsawRegistry120.class,path);
        if(input==null)return null;
        try{
            NBTTagCompound root=CompressedStreamTools.readCompressed(input);
            int[] size=intList(root.getTagList("size",3));
            NBTTagList palette=root.getTagList("palette",10);
            Map<Integer,Orientation> jigsawStates=new HashMap<Integer,Orientation>();
            for(int i=0;i<palette.tagCount();i++){
                NBTTagCompound state=palette.getCompoundTagAt(i);
                if(!"minecraft:jigsaw".equals(state.getString("Name")))continue;
                String orientation=state.getCompoundTag("Properties").getString("orientation");
                jigsawStates.put(i,orientation(orientation));
            }
            List<JigsawPlacementRuntime120.Connector> connectors=
                    new ArrayList<JigsawPlacementRuntime120.Connector>();
            NBTTagList blocks=root.getTagList("blocks",10);
            for(int i=0;i<blocks.tagCount();i++){
                NBTTagCompound block=blocks.getCompoundTagAt(i);
                Orientation orientation=jigsawStates.get(block.getInteger("state"));
                if(orientation==null||!block.hasKey("nbt",10))continue;
                int[] pos=intList(block.getTagList("pos",3));
                NBTTagCompound data=block.getCompoundTag("nbt");
                String joint=data.getString("joint");
                connectors.add(new JigsawPlacementRuntime120.Connector(pos[0],pos[1],pos[2],
                        orientation.front,orientation.top,"rollable".equals(joint)
                        ?JigsawPlacementRuntime120.Joint.ROLLABLE:JigsawPlacementRuntime120.Joint.ALIGNED,
                        normalize(data.getString("name")),normalize(data.getString("target")),
                        normalize(data.getString("pool"))));
            }
            NBTTagList entities=root.getTagList("entities",10);
            for(int i=0;i<entities.tagCount();i++){
                NBTTagCompound value=entities.getCompoundTagAt(i);
                if(value.hasKey("nbt",10)){entityTypes.add(normalize(value.getCompoundTag("nbt").getString("id")));entityCount++;}
            }
            JigsawPlacementRuntime120.Template result=new JigsawPlacementRuntime120.Template(id,
                    size[0],size[1],size[2],1,connectors);
            templates.put(id,result);return result;
        }catch(IOException error){throw new IllegalStateException("Cannot read official village template "+path,error);}
        finally{try{input.close();}catch(IOException ignored){}}
    }

    public void validatePlains(){validateStyle("plains");}
    public void validateAll(){
        for(String style:STYLES)validateStyle(style);
        for(String id:UNREFERENCED_TEMPLATES)if(template(id)==null)
            throw new JigsawPlacementRuntime120.MissingDataException("Missing retained village template "+id);
    }
    public void validateStyle(String style){
        Set<String> visitedPools=new HashSet<String>();
        Set<String> visitedTemplates=new HashSet<String>();
        ArrayList<String> pending=new ArrayList<String>();pending.add(startPool(style));
        for(int index=0;index<pending.size();index++){
            String poolId=normalize(pending.get(index));
            if("minecraft:empty".equals(poolId)||!visitedPools.add(poolId))continue;
            JigsawPlacementRuntime120.Pool pool=pool(poolId);
            if(pool==null)throw new JigsawPlacementRuntime120.MissingDataException("Missing template pool "+poolId);
            pending.add(pool.fallback);
            for(JigsawPlacementRuntime120.Weighted weighted:pool.entries){
                if(weighted.element.empty)continue;
                if(weighted.element.kind==JigsawPlacementRuntime120.ElementKind.FEATURE)continue;
                String templateId=weighted.element.template;
                if(!visitedTemplates.add(templateId))continue;
                JigsawPlacementRuntime120.Template template=template(templateId);
                if(template==null)throw new JigsawPlacementRuntime120.MissingDataException(
                        "Missing village template "+templateId);
                for(JigsawPlacementRuntime120.Connector connector:template.connectors)pending.add(connector.pool);
            }
        }
    }

    public static String startPool(String style){return "minecraft:village/"+style+"/town_centers";}

    /** Compatibility alias retained for fixtures; every official feature now has a runtime adapter. */
    public Set<String> unsupportedFeatures(){return Collections.emptySet();}
    public Set<String> featureTypes(){return Collections.unmodifiableSet(new HashSet<String>(featureTypes));}
    public int loadedPoolCount(){return pools.size();}
    public int loadedTemplateCount(){return templates.size();}
    public Set<String> loadedTemplateIds(){return Collections.unmodifiableSet(new HashSet<String>(templates.keySet()));}
    public Set<String> entityTypes(){return Collections.unmodifiableSet(new HashSet<String>(entityTypes));}
    public int entityCount(){return entityCount;}
    public int legacyElementCount(){return legacyElementCount;}

    private Orientation orientation(String value){
        String[] parts=value.split("_");
        if(parts.length!=2)throw new IllegalStateException("Invalid jigsaw orientation "+value);
        return new Orientation(direction(parts[0]),direction(parts[1]));
    }
    private JigsawPlacementRuntime120.Direction direction(String value){
        return JigsawPlacementRuntime120.Direction.valueOf(value.toUpperCase(java.util.Locale.ROOT));
    }
    private int[] intList(NBTTagList list){List<NBTBase> values=rawList(list);int[] result=new int[values.size()];
        for(int i=0;i<result.length;i++)result[i]=((NBTBase.NBTPrimitive)values.get(i)).func_150287_d();return result;}
    @SuppressWarnings("unchecked") private List<NBTBase> rawList(NBTTagList list){try{
        if(tagListField==null)for(Field field:NBTTagList.class.getDeclaredFields())if(List.class.isAssignableFrom(field.getType())){
            field.setAccessible(true);tagListField=field;break;}
        return (List<NBTBase>)tagListField.get(list);
    }catch(Exception error){throw new IllegalStateException("Cannot access NBT list",error);}}
    private static String normalize(String id){return id.indexOf(':')<0?"minecraft:"+id:id;}
    private static String stripNamespace(String id){int split=id.indexOf(':');return split<0?id:id.substring(split+1);}
    private static final class Orientation {final JigsawPlacementRuntime120.Direction front,top;
        Orientation(JigsawPlacementRuntime120.Direction front,JigsawPlacementRuntime120.Direction top){this.front=front;this.top=top;}}
}
