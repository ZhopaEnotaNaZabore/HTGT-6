import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;

/** Exercises the actual loader with filesystem resources, without an OpenGL context. */
public final class JsonItemModels114Fixture {
    static int checks;
    static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
    static Object field(Object target,String name)throws Exception {Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}
    static JsonObject json(String text){return new JsonParser().parse(text).getAsJsonObject();}
    public static void main(String[] args)throws Exception {
        final Map<String,String> overrides=new HashMap<String,String>();
        IResourceManager resources=(IResourceManager)Proxy.newProxyInstance(JsonItemModels114Fixture.class.getClassLoader(),new Class[]{IResourceManager.class},(p,m,a)->{
            ResourceLocation id=(ResourceLocation)a[0];String key=id.toString();
            byte[] bytes=overrides.containsKey(key)?overrides.get(key).getBytes("UTF-8"):Files.readAllBytes(Paths.get("src/main/resources/assets",id.getResourceDomain(),id.getResourcePath()));
            return Proxy.newProxyInstance(JsonItemModels114Fixture.class.getClassLoader(),new Class[]{IResource.class},(q,n,b)->n.getName().equals("getInputStream")?new ByteArrayInputStream(bytes):null);
        });
        Constructor<?> ctor=JsonBlockModelLoader.class.getDeclaredConstructor(int.class);ctor.setAccessible(true);Object loader=ctor.newInstance(1);
        Method load=JsonBlockModelLoader.class.getDeclaredMethod("loadModel",IResourceManager.class,ResourceLocation.class,List.class);load.setAccessible(true);
        Method resolve=JsonBlockModelLoader.class.getDeclaredMethod("resolveTexture",Map.class,String.class);resolve.setAccessible(true);
        JsonObject manifest=json(new String(Files.readAllBytes(Paths.get("docs/backport-audit-2026-10-01/items114-models.json")),"UTF-8"));
        for(JsonElement name:manifest.getAsJsonArray("items")) {
            Object model=load.invoke(loader,resources,new ResourceLocation("htgt6","village_items114/item/"+name.getAsString()),new ArrayList());
            Map<String,String> textures=(Map)field(model,"textures");
            check((Boolean)field(model,"generated")||!((List)field(model,"elements")).isEmpty(),"item has drawable geometry: "+name);
            for(String texture:textures.values()) {
                String resolved=(String)resolve.invoke(null,textures,texture);String path=resolved.substring(resolved.indexOf(':')+1);
                check(Files.isRegularFile(Paths.get("src/main/resources/assets/htgt6/textures/blocks/village_items114",path+".png")),"texture exists: "+resolved);
            }
        }
        Object crossbow=load.invoke(loader,resources,new ResourceLocation("htgt6","village_items114/item/crossbow"),new ArrayList());
        List<JsonObject> rules=(List)field(crossbow,"overrides");Map<String,Float> predicates=new HashMap<String,Float>();
        predicates.put("pulling",1F);predicates.put("pull",.58F);predicates.put("charged",0F);predicates.put("firework",0F);
        check(JsonBlockModelLoader.selectOverride(rules,predicates).endsWith("pulling_1"),"pull boundary .58 selects stage 1");
        predicates.put("pull",1F);check(JsonBlockModelLoader.selectOverride(rules,predicates).endsWith("pulling_2"),"last matching pull rule wins");
        predicates.put("charged",1F);predicates.put("firework",1F);check(JsonBlockModelLoader.selectOverride(rules,predicates).endsWith("firework"),"charged rocket overrides pulling");
        Map<String,JsonObject> display=(Map)field(crossbow,"display");
        check(display.get("ground").getAsJsonArray("scale").get(0).getAsDouble()==.5,"generated parent ground display inherited");
        Object lantern=load.invoke(loader,resources,new ResourceLocation("htgt6","village_items114/item/lantern"),new ArrayList());
        check((Boolean)field(lantern,"generated"),"lantern item is sprite, not placed hanging geometry");
        Method parse=JsonBlockModelLoader.class.getDeclaredMethod("parseElement",JsonObject.class);parse.setAccessible(true);
        Method uvVector=JsonBlockModelLoader.class.getDeclaredMethod("vector4",JsonArray.class);uvVector.setAccessible(true);
        JsonArray invalidUv=new JsonArray();invalidUv.add(new JsonPrimitive(Double.NaN));for(int i=0;i<3;i++)invalidUv.add(new JsonPrimitive(0));
        try{uvVector.invoke(null,invalidUv);throw new AssertionError("non-finite UV accepted");}catch(InvocationTargetException ex){check(ex.getCause() instanceof IllegalArgumentException,"non-finite UV rejected");}
        overrides.put("htgt6:models/display_parent.json","{\"parent\":\"builtin/generated\",\"gui_light\":\"front\",\"display\":{\"gui\":{\"scale\":[2,2,2],\"rotation\":[10,20,30]},\"ground\":{\"scale\":[0.5,0.5,0.5]}}}");
        overrides.put("htgt6:models/display_child.json","{\"parent\":\"htgt6:display_parent\",\"display\":{\"gui\":{\"translation\":[1,2,3]}}}");
        Object child=load.invoke(loader,resources,new ResourceLocation("htgt6","display_child"),new ArrayList());
        Map<String,JsonObject> childDisplay=(Map)field(child,"display");
        check(!childDisplay.get("gui").has("scale")&&childDisplay.containsKey("ground"),"child replaces a display context and inherits other contexts");
        check("front".equals(field(child,"guiLight")),"gui light inherited");
        Object element=parse.invoke(null,json("{\"from\":[1,2,3],\"to\":[9,10,12],\"faces\":{\"down\":{\"texture\":\"#x\"},\"north\":{\"texture\":\"#x\"},\"east\":{\"texture\":\"#x\"}}}"));
        for(Object face:((Map)field(element,"faces")).values()) {
            String direction=field(face,"direction").toString();double[] uv=(double[])field(face,"uv");
            double[] expected=direction.equals("DOWN")?new double[]{1,4,9,13}:direction.equals("NORTH")?new double[]{7,6,15,14}:new double[]{4,6,13,14};
            check(Arrays.equals(uv,expected),"official asymmetric default UV for "+direction);
        }
        Map<String,String> aliases=new HashMap<String,String>();aliases.put("a","#b");aliases.put("b","#a");
        try{resolve.invoke(null,aliases,"#a");throw new AssertionError("cycle accepted");}catch(InvocationTargetException ex){check(ex.getCause() instanceof IllegalArgumentException,"texture cycle rejected");}
        try{resolve.invoke(null,aliases,"#missing");throw new AssertionError("missing alias accepted");}catch(InvocationTargetException ex){check(ex.getCause() instanceof IllegalArgumentException,"missing alias rejected");}
        overrides.put("htgt6:models/cycle_a.json","{\"parent\":\"htgt6:cycle_b\"}");overrides.put("htgt6:models/cycle_b.json","{\"parent\":\"htgt6:cycle_a\"}");
        try{load.invoke(loader,resources,new ResourceLocation("htgt6","cycle_a"),new ArrayList());throw new AssertionError("parent cycle accepted");}catch(InvocationTargetException ex){check(ex.getCause() instanceof IllegalArgumentException,"parent cycle rejected");}
        Object first=load.invoke(loader,resources,new ResourceLocation("htgt6","village_items114/item/barrel"),new ArrayList());
        ((Map)field(loader,"modelCache")).clear();
        overrides.put("htgt6:models/village_items114/item/barrel.json","{\"parent\":\"builtin/generated\",\"textures\":{\"layer0\":\"item/paper\"}}");
        Object second=load.invoke(loader,resources,new ResourceLocation("htgt6","village_items114/item/barrel"),new ArrayList());
        check(first!=second&&(Boolean)field(second,"generated"),"reload discards previous geometry");
        Scaffolding167ModelFixture.main(args);
        System.out.println("PASS JSON item models 1.14: "+checks+" checks; "+manifest.getAsJsonArray("items").size()+" item roots");
    }
}
