package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.BackportResourceLoader120;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Immutable-on-load view of the seven official 1.20.2 Ancient City pools. */
public final class AncientCityJigsawRegistry120 implements JigsawPlacementRuntime120.Registry {
    public static final String START_POOL="minecraft:ancient_city/city_center";
    public static final String START_JIGSAW="minecraft:city_anchor";
    public static final int SIZE=7,MAX_DISTANCE=116;
    private final Map<String,JigsawPlacementRuntime120.Pool> pools=new HashMap<String,JigsawPlacementRuntime120.Pool>();
    private final Map<String,JigsawPlacementRuntime120.Template> templates=new HashMap<String,JigsawPlacementRuntime120.Template>();
    private Field tagListField;

    @Override public synchronized JigsawPlacementRuntime120.Pool pool(String rawId){
        String id=normalize(rawId);
        if("minecraft:empty".equals(id))return new JigsawPlacementRuntime120.Pool(id,id,
                Collections.<JigsawPlacementRuntime120.Weighted>emptyList());
        JigsawPlacementRuntime120.Pool cached=pools.get(id);if(cached!=null)return cached;
        String relative=stripNamespace(id);
        if(!relative.startsWith("ancient_city/"))return null;
        String path="/assets/htgt6/worldgen/template_pool/"+relative+".json";
        InputStream input=BackportResourceLoader120.open(AncientCityJigsawRegistry120.class,path);if(input==null)return null;
        try{
            JsonObject root=new JsonParser().parse(new InputStreamReader(input,"UTF-8")).getAsJsonObject();
            List<JigsawPlacementRuntime120.Weighted> entries=new ArrayList<JigsawPlacementRuntime120.Weighted>();
            for(JsonElement value:root.getAsJsonArray("elements")){
                JsonObject weighted=value.getAsJsonObject();
                entries.add(new JigsawPlacementRuntime120.Weighted(parseElement(weighted.getAsJsonObject("element")),
                        weighted.get("weight").getAsInt()));
            }
            JigsawPlacementRuntime120.Pool result=new JigsawPlacementRuntime120.Pool(id,
                    normalize(root.get("fallback").getAsString()),entries);pools.put(id,result);return result;
        }catch(Exception error){throw new IllegalStateException("Cannot parse Ancient City pool "+path,error);}
        finally{try{input.close();}catch(IOException ignored){}}
    }

    private JigsawPlacementRuntime120.Element parseElement(JsonObject value){
        String type=value.get("element_type").getAsString();
        if("minecraft:empty_pool_element".equals(type))return JigsawPlacementRuntime120.Element.empty();
        if("minecraft:feature_pool_element".equals(type))return JigsawPlacementRuntime120.Element.feature(
                normalize(value.get("feature").getAsString()),projection(value));
        if("minecraft:list_pool_element".equals(type)){
            List<JigsawPlacementRuntime120.Element> children=new ArrayList<JigsawPlacementRuntime120.Element>();
            for(JsonElement child:value.getAsJsonArray("elements"))children.add(parseElement(child.getAsJsonObject()));
            return JigsawPlacementRuntime120.Element.list(projection(value),children);
        }
        if(!"minecraft:single_pool_element".equals(type)&&!"minecraft:legacy_single_pool_element".equals(type))
            throw new IllegalStateException("Unsupported Ancient City element "+type);
        return new JigsawPlacementRuntime120.Element(normalize(value.get("location").getAsString()),
                projection(value),processors(value.get("processors")),
                "minecraft:legacy_single_pool_element".equals(type));
    }

    private static JigsawPlacementRuntime120.Projection projection(JsonObject value){
        return value.has("projection")&&"terrain_matching".equals(value.get("projection").getAsString())
                ?JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING:JigsawPlacementRuntime120.Projection.RIGID;
    }
    private static List<String> processors(JsonElement value){
        if(value==null||value.isJsonNull())return Collections.emptyList();
        if(value.isJsonPrimitive())return Collections.singletonList(normalize(value.getAsString()));
        JsonArray inline=value.getAsJsonObject().getAsJsonArray("processors");
        if(inline==null||inline.size()==0)return Collections.emptyList();
        throw new IllegalStateException("Non-empty inline Ancient City processors are unsupported");
    }

    @Override public synchronized JigsawPlacementRuntime120.Template template(String rawId){
        String id=normalize(rawId);JigsawPlacementRuntime120.Template cached=templates.get(id);if(cached!=null)return cached;
        String relative=stripNamespace(id);if(!relative.startsWith("ancient_city/"))return null;
        String path="/assets/htgt6/structures/ancient_city/"+relative.substring("ancient_city/".length())+".nbt";
        InputStream input=BackportResourceLoader120.open(AncientCityJigsawRegistry120.class,path);if(input==null)return null;
        try{
            NBTTagCompound root=CompressedStreamTools.readCompressed(input);int[] size=intList(root.getTagList("size",3));
            NBTTagList palette=root.getTagList("palette",10);Map<Integer,Orientation> states=new HashMap<Integer,Orientation>();
            for(int i=0;i<palette.tagCount();i++){
                NBTTagCompound state=palette.getCompoundTagAt(i);if(!"minecraft:jigsaw".equals(state.getString("Name")))continue;
                states.put(i,orientation(state.getCompoundTag("Properties").getString("orientation")));
            }
            List<JigsawPlacementRuntime120.Connector> connectors=new ArrayList<JigsawPlacementRuntime120.Connector>();
            NBTTagList blocks=root.getTagList("blocks",10);
            for(int i=0;i<blocks.tagCount();i++){
                NBTTagCompound block=blocks.getCompoundTagAt(i);Orientation orientation=states.get(block.getInteger("state"));
                if(orientation==null||!block.hasKey("nbt",10))continue;int[] pos=intList(block.getTagList("pos",3));
                NBTTagCompound data=block.getCompoundTag("nbt");connectors.add(new JigsawPlacementRuntime120.Connector(
                        pos[0],pos[1],pos[2],orientation.front,orientation.top,
                        "rollable".equals(data.getString("joint"))?JigsawPlacementRuntime120.Joint.ROLLABLE
                                :JigsawPlacementRuntime120.Joint.ALIGNED,
                        normalize(data.getString("name")),normalize(data.getString("target")),normalize(data.getString("pool"))));
            }
            JigsawPlacementRuntime120.Template result=new JigsawPlacementRuntime120.Template(id,size[0],size[1],size[2],0,connectors);
            templates.put(id,result);return result;
        }catch(IOException error){throw new IllegalStateException("Cannot read Ancient City template "+path,error);}
        finally{try{input.close();}catch(IOException ignored){}}
    }

    public List<JigsawPlacementRuntime120.Pool> allPools(){
        String[] ids={"city/entrance","city_center","city_center/walls","sculk","structures","walls","walls/no_corners"};
        List<JigsawPlacementRuntime120.Pool> result=new ArrayList<JigsawPlacementRuntime120.Pool>();
        for(String id:ids){JigsawPlacementRuntime120.Pool pool=pool("minecraft:ancient_city/"+id);
            if(pool==null)throw new JigsawPlacementRuntime120.MissingDataException("Missing Ancient City pool "+id);result.add(pool);}
        return Collections.unmodifiableList(result);
    }
    private Orientation orientation(String value){String[] parts=value.split("_");
        return new Orientation(direction(parts[0]),direction(parts[1]));}
    private static JigsawPlacementRuntime120.Direction direction(String value){return JigsawPlacementRuntime120.Direction.valueOf(
            value.toUpperCase(java.util.Locale.ROOT));}
    private int[] intList(NBTTagList list){List<NBTBase> values=rawList(list);int[] result=new int[values.size()];
        for(int i=0;i<result.length;i++)result[i]=((NBTBase.NBTPrimitive)values.get(i)).func_150287_d();return result;}
    @SuppressWarnings("unchecked") private List<NBTBase> rawList(NBTTagList list){try{
        if(tagListField==null)for(Field field:NBTTagList.class.getDeclaredFields())if(List.class.isAssignableFrom(field.getType())){
            field.setAccessible(true);tagListField=field;break;}return (List<NBTBase>)tagListField.get(list);
    }catch(Exception error){throw new IllegalStateException("Cannot access NBT list",error);}}
    private static String normalize(String id){return id.indexOf(':')<0?"minecraft:"+id:id;}
    private static String stripNamespace(String id){int split=id.indexOf(':');return split<0?id:id.substring(split+1);}
    private static final class Orientation {final JigsawPlacementRuntime120.Direction front,top;
        Orientation(JigsawPlacementRuntime120.Direction front,JigsawPlacementRuntime120.Direction top){this.front=front;this.top=top;}}
}
