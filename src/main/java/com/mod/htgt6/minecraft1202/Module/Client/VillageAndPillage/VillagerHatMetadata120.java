package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import java.util.*;
import java.io.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import com.google.gson.*;
/** Resource-pack metadata cache, invalidated by the native 1.7 reload manager. */
public final class VillagerHatMetadata120 implements IResourceManagerReloadListener {
    private static final Map<String,String> CACHE=new HashMap<String,String>();
    public void onResourceManagerReload(IResourceManager manager){CACHE.clear();}
    public static String hat(String texture){
        String result=CACHE.get(texture);if(result!=null)return result;result="none";
        try(InputStream in=Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(texture+".mcmeta")).getInputStream()){
            JsonObject root=new JsonParser().parse(new InputStreamReader(in,"UTF-8")).getAsJsonObject();
            if(root.has("villager")){JsonObject meta=root.getAsJsonObject("villager");if(meta.has("hat"))result=meta.get("hat").getAsString();}
        }catch(IOException ignored){}catch(JsonParseException ignored){}
        CACHE.put(texture,result);return result;
    }
}
