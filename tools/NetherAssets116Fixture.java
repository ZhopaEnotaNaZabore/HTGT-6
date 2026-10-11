import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;

public final class NetherAssets116Fixture {
    static int checks;
    static Object field(Object o,String key)throws Exception{Field f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);}
    public static void verify()throws Exception{
        IResourceManager resources=(IResourceManager)Proxy.newProxyInstance(NetherAssets116Fixture.class.getClassLoader(),new Class[]{IResourceManager.class},(p,m,a)->{
            ResourceLocation id=(ResourceLocation)a[0];Path path=Paths.get("src/main/resources/assets",id.getResourceDomain(),id.getResourcePath());
            return Proxy.newProxyInstance(NetherAssets116Fixture.class.getClassLoader(),new Class[]{IResource.class},(q,n,b)->n.getName().equals("getInputStream")?Files.newInputStream(path):null);
        });
        Constructor<?> ctor=JsonBlockModelLoader.class.getDeclaredConstructor(int.class);ctor.setAccessible(true);JsonBlockModelLoader loader=(JsonBlockModelLoader)ctor.newInstance(1);
        Method load=JsonBlockModelLoader.class.getDeclaredMethod("loadModel",IResourceManager.class,ResourceLocation.class,List.class);load.setAccessible(true);
        for(String method:new String[]{"bindNetherWoodFamilies","bindNetherVegetation"}){
            Method bind=com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient.class.getDeclaredMethod(method,JsonBlockModelLoader.class,JsonBlockModelLoader.TextureResolver.class);bind.setAccessible(true);bind.invoke(null,loader,(JsonBlockModelLoader.TextureResolver)(s->new ResourceLocation(s)));
        }
        List<String> names=new ArrayList<String>(Arrays.asList(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.NAMES));
        names.addAll(Arrays.asList("twisting_vines","twisting_vines_plant","weeping_vines","weeping_vines_plant","soul_campfire","native_cobblestone_wall","redstone_wire","potted_crimson_fungus","potted_warped_fungus","potted_crimson_roots","potted_warped_roots"));
        for(String family:new String[]{"crimson","warped"})for(String suffix:new String[]{"stem","hyphae","planks","stairs","slab","fence","fence_gate","door","trapdoor","button","pressure_plate"})names.add(family+"_"+suffix);
        for(String name:names){
            JsonElement state=new JsonParser().parse(new String(Files.readAllBytes(Paths.get("src/main/resources/assets/htgt6/blockstates/1_20_backport/"+name+".json")),"UTF-8"));
            Set<String> models=new HashSet<String>();collect(state,models);
            for(String id:models){
                Object model=load.invoke(loader,resources,new ResourceLocation(id),new ArrayList());
                if(((List)field(model,"elements")).isEmpty())throw new AssertionError("Empty block model "+id);checks++;
                for(Object value:((Map)field(model,"textures")).values()){
                    String texture=(String)value;if(texture.startsWith("#"))continue;ResourceLocation loc=new ResourceLocation(texture);
                    if(!Files.isRegularFile(Paths.get("src/main/resources/assets",loc.getResourceDomain(),"textures/blocks",loc.getResourcePath()+".png")))throw new AssertionError("Missing texture "+texture);checks++;
                }
            }
        }
        for(String name:Arrays.asList("crimson_door","warped_door","crimson_sign","warped_sign","crimson_fungus","warped_fungus","crimson_roots","warped_roots","nether_sprouts","twisting_vines","weeping_vines","soul_campfire","warped_fungus_on_a_stick","piglin_banner_pattern")){
            Object model=load.invoke(loader,resources,new ResourceLocation("htgt6:item/1_20_backport/"+name),new ArrayList());
            if(!Boolean.TRUE.equals(field(model,"generated")))throw new AssertionError("Item did not inherit generated geometry: "+name);checks++;
            String texture=(String)((Map)field(model,"textures")).get("layer0");
            if(texture==null||texture.startsWith("#"))throw new AssertionError("Missing item layer: "+name);
            ResourceLocation loc=new ResourceLocation(texture);
            if(!Files.isRegularFile(Paths.get("src/main/resources/assets",loc.getResourceDomain(),"textures/blocks",loc.getResourcePath()+".png")))throw new AssertionError("Missing item texture "+texture);checks++;
        }
        System.out.println("Nether assets 116: "+checks+" actual model/texture checks passed");
    }
    static void collect(JsonElement node,Set<String> out){if(node.isJsonArray())for(JsonElement e:node.getAsJsonArray())collect(e,out);else if(node.isJsonObject())for(Map.Entry<String,JsonElement> e:node.getAsJsonObject().entrySet()){if(e.getKey().equals("model"))out.add(e.getValue().getAsString());else collect(e.getValue(),out);}}
}
