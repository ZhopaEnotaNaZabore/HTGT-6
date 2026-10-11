import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
public final class Scaffolding167ModelFixture {
 static Object field(Object obj,String name)throws Exception{Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj);}
 public static void main(String[] args)throws Exception{
  Method parse=JsonBlockModelLoader.class.getDeclaredMethod("parseElement",JsonObject.class);parse.setAccessible(true);
  int count=0,unknown=0;
  for(String name:new String[]{"stable","unstable"}){
   String path="src/main/resources/assets/htgt6/models/block/village_pillage/scaffolding_"+name+".json";
   JsonObject json=new JsonParser().parse(new String(Files.readAllBytes(Paths.get(path)),"UTF-8")).getAsJsonObject();
   for(JsonElement e:json.getAsJsonArray("elements")){
    Object parsed=parse.invoke(null,e.getAsJsonObject());count++;
    for(Object face:((Map)field(parsed,"faces")).values()){
     String direction=field(face,"direction").toString().toLowerCase(Locale.ROOT);
     JsonObject raw=e.getAsJsonObject().getAsJsonObject("faces").getAsJsonObject(direction);
     if(raw.has("cullface")&&raw.get("cullface").getAsString().equals("bottom")){
      if(field(face,"cull")!=null)throw new AssertionError("bottom must disable culling, not become DOWN");unknown++;
     }else if(raw.has("cullface")&&field(face,"cull")==null)throw new AssertionError("valid cullface lost");
    }
   }
  }
  if(unknown!=4)throw new AssertionError("Expected four vanilla bottom cullfaces");
  JsonObject invalid=new JsonParser().parse("{\"from\":[0,0,0],\"to\":[16,16,16],\"faces\":{\"bottom\":{\"texture\":\"#x\"}}}").getAsJsonObject();
  try{parse.invoke(null,invalid);throw new AssertionError("Invalid face name accepted");}catch(InvocationTargetException ex){if(!(ex.getCause() instanceof IllegalArgumentException))throw ex;}
  System.out.println("PASS scaffolding167: "+count+" elements, 4 nullable cullfaces, strict invalid face");
  Campfire157ModelFixture.main(args);
 }
}
