package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.nbt.*;

/** Namespace-scoped loader for unmodified 1.20.2 jigsaw pools/templates. */
public final class OfficialJigsawRegistry120 implements JigsawPlacementRuntime120.Registry {
    private final String root;private final int groundDelta;
    private final Map<String,JigsawPlacementRuntime120.Pool> pools=new HashMap<String,JigsawPlacementRuntime120.Pool>();
    private final Map<String,JigsawPlacementRuntime120.Template> templates=new HashMap<String,JigsawPlacementRuntime120.Template>();
    private Field listField;
    public OfficialJigsawRegistry120(String root,int groundDelta){this.root=root.endsWith("/")?root:root+"/";this.groundDelta=groundDelta;}
    @Override public synchronized JigsawPlacementRuntime120.Pool pool(String raw){String id=norm(raw);
        if("minecraft:empty".equals(id))return new JigsawPlacementRuntime120.Pool(id,id,Collections.<JigsawPlacementRuntime120.Weighted>emptyList());
        JigsawPlacementRuntime120.Pool old=pools.get(id);if(old!=null)return old;String rel=strip(id);if(!rel.startsWith(root))return null;
        String path="/assets/htgt6/worldgen/template_pool/"+rel+".json";InputStream in=BackportResourceLoader120.open(getClass(),path);if(in==null)return null;
        try{JsonObject json=new JsonParser().parse(new InputStreamReader(in,"UTF-8")).getAsJsonObject();List<JigsawPlacementRuntime120.Weighted> entries=new ArrayList<JigsawPlacementRuntime120.Weighted>();
            for(JsonElement e:json.getAsJsonArray("elements")){JsonObject w=e.getAsJsonObject();entries.add(new JigsawPlacementRuntime120.Weighted(element(w.getAsJsonObject("element")),w.get("weight").getAsInt()));}
            JigsawPlacementRuntime120.Pool result=new JigsawPlacementRuntime120.Pool(id,norm(json.get("fallback").getAsString()),entries);pools.put(id,result);return result;
        }catch(Exception e){throw new IllegalStateException("Cannot parse official pool "+path,e);}finally{close(in);}}
    private JigsawPlacementRuntime120.Element element(JsonObject json){String type=json.get("element_type").getAsString();
        if("minecraft:empty_pool_element".equals(type))return JigsawPlacementRuntime120.Element.empty();
        if("minecraft:feature_pool_element".equals(type))return JigsawPlacementRuntime120.Element.feature(norm(json.get("feature").getAsString()),projection(json));
        if("minecraft:list_pool_element".equals(type)){List<JigsawPlacementRuntime120.Element> children=new ArrayList<JigsawPlacementRuntime120.Element>();for(JsonElement e:json.getAsJsonArray("elements"))children.add(element(e.getAsJsonObject()));return JigsawPlacementRuntime120.Element.list(projection(json),children);}
        if(!"minecraft:single_pool_element".equals(type)&&!"minecraft:legacy_single_pool_element".equals(type))throw new IllegalStateException("Unsupported pool element "+type);
        return new JigsawPlacementRuntime120.Element(norm(json.get("location").getAsString()),projection(json),processors(json.get("processors")),"minecraft:legacy_single_pool_element".equals(type));}
    private static JigsawPlacementRuntime120.Projection projection(JsonObject json){return json.has("projection")&&"terrain_matching".equals(json.get("projection").getAsString())?JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING:JigsawPlacementRuntime120.Projection.RIGID;}
    private static List<String> processors(JsonElement value){if(value==null||value.isJsonNull())return Collections.emptyList();if(value.isJsonPrimitive())return Collections.singletonList(norm(value.getAsString()));JsonArray array=value.getAsJsonObject().getAsJsonArray("processors");if(array==null||array.size()==0)return Collections.emptyList();throw new IllegalStateException("Unsupported inline processors");}
    @Override public synchronized JigsawPlacementRuntime120.Template template(String raw){String id=norm(raw);JigsawPlacementRuntime120.Template old=templates.get(id);if(old!=null)return old;String rel=strip(id);if(!rel.startsWith(root))return null;
        String path="/assets/htgt6/structures/"+rel+".nbt";InputStream in=BackportResourceLoader120.open(getClass(),path);if(in==null)return null;
        try{NBTTagCompound nbt=CompressedStreamTools.readCompressed(in);int[] size=ints(nbt.getTagList("size",3));NBTTagList palette=nbt.getTagList("palette",10);Map<Integer,Orientation> states=new HashMap<Integer,Orientation>();
            for(int i=0;i<palette.tagCount();i++){NBTTagCompound s=palette.getCompoundTagAt(i);if("minecraft:jigsaw".equals(s.getString("Name")))states.put(i,orientation(s.getCompoundTag("Properties").getString("orientation")));}
            List<JigsawPlacementRuntime120.Connector> connectors=new ArrayList<JigsawPlacementRuntime120.Connector>();NBTTagList blocks=nbt.getTagList("blocks",10);
            for(int i=0;i<blocks.tagCount();i++){NBTTagCompound b=blocks.getCompoundTagAt(i);Orientation o=states.get(b.getInteger("state"));if(o==null||!b.hasKey("nbt",10))continue;int[] p=ints(b.getTagList("pos",3));NBTTagCompound d=b.getCompoundTag("nbt");connectors.add(new JigsawPlacementRuntime120.Connector(p[0],p[1],p[2],o.front,o.top,"rollable".equals(d.getString("joint"))?JigsawPlacementRuntime120.Joint.ROLLABLE:JigsawPlacementRuntime120.Joint.ALIGNED,norm(d.getString("name")),norm(d.getString("target")),norm(d.getString("pool"))));}
            JigsawPlacementRuntime120.Template result=new JigsawPlacementRuntime120.Template(id,size[0],size[1],size[2],groundDelta,connectors);templates.put(id,result);return result;
        }catch(IOException e){throw new IllegalStateException("Cannot read official template "+path,e);}finally{close(in);}}
    public void validateGraph(String startPool){Set<String> seenPools=new HashSet<String>(),seenTemplates=new HashSet<String>();List<String> queue=new ArrayList<String>();queue.add(startPool);
        for(int i=0;i<queue.size();i++){String id=norm(queue.get(i));if("minecraft:empty".equals(id)||!seenPools.add(id))continue;JigsawPlacementRuntime120.Pool p=pool(id);if(p==null)throw new JigsawPlacementRuntime120.MissingDataException("Missing pool "+id);queue.add(p.fallback);for(JigsawPlacementRuntime120.Weighted w:p.entries)walk(w.element,seenTemplates,queue);}}
    private void walk(JigsawPlacementRuntime120.Element e,Set<String> templatesSeen,List<String> queue){if(e.kind==JigsawPlacementRuntime120.ElementKind.LIST){for(JigsawPlacementRuntime120.Element c:e.children)walk(c,templatesSeen,queue);return;}if(e.kind!=JigsawPlacementRuntime120.ElementKind.SINGLE||!templatesSeen.add(e.template))return;JigsawPlacementRuntime120.Template t=template(e.template);if(t==null)throw new JigsawPlacementRuntime120.MissingDataException("Missing template "+e.template);for(JigsawPlacementRuntime120.Connector c:t.connectors)queue.add(c.pool);}
    public int loadedPoolCount(){return pools.size();}public int loadedTemplateCount(){return templates.size();}
    public Set<String> loadedTemplateIds(){return Collections.unmodifiableSet(new HashSet<String>(templates.keySet()));}
    public static String templateResource(String id){return "/assets/htgt6/structures/"+strip(id)+".nbt";}
    private Orientation orientation(String s){String[] p=s.split("_");return new Orientation(direction(p[0]),direction(p[1]));}
    private static JigsawPlacementRuntime120.Direction direction(String s){return JigsawPlacementRuntime120.Direction.valueOf(s.toUpperCase(Locale.ROOT));}
    private int[] ints(NBTTagList tag){List<NBTBase> values=raw(tag);int[] out=new int[values.size()];for(int i=0;i<out.length;i++)out[i]=((NBTBase.NBTPrimitive)values.get(i)).func_150287_d();return out;}
    @SuppressWarnings("unchecked") private List<NBTBase> raw(NBTTagList tag){try{if(listField==null)for(Field f:NBTTagList.class.getDeclaredFields())if(List.class.isAssignableFrom(f.getType())){f.setAccessible(true);listField=f;break;}return (List<NBTBase>)listField.get(tag);}catch(Exception e){throw new IllegalStateException(e);}}
    private static String norm(String id){return id.indexOf(':')<0?"minecraft:"+id:id;}private static String strip(String id){int i=id.indexOf(':');return i<0?id:id.substring(i+1);}private static void close(InputStream in){try{in.close();}catch(IOException ignored){}}
    private static final class Orientation{final JigsawPlacementRuntime120.Direction front,top;Orientation(JigsawPlacementRuntime120.Direction f,JigsawPlacementRuntime120.Direction t){front=f;top=t;}}
}
