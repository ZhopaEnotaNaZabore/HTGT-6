package com.mod.htgt6.client.sounds;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;

/** Additive schema-1 import of exact objects from a SHA-1/size-pinned Mojang index.
 * The index is verified before parsing; each resolved OGG is then verified by the
 * existing OfficialSoundCache. Neither metadata nor audio is executable code. */
public final class OfficialSoundIndex116 {
    public static final String HASH="117379a04a44c80fc5a5806f2e676ca4ac81f5ce";
    public static final long SIZE=416952L;
    public static final String URL="https://piston-meta.mojang.com/v1/packages/"+HASH+"/8.json";
    public static final class Import {
        public final Map<String,String> objects;
        private Import(Map<String,String> objects){this.objects=Collections.unmodifiableMap(objects);}
    }
    public static Import read(InputStream stream)throws IOException {
        if(stream==null)throw new IOException("Missing external sound manifest");
        try(Reader reader=new InputStreamReader(stream,"UTF-8")){
            JsonObject root=new JsonParser().parse(reader).getAsJsonObject();
            if(root.get("schema").getAsInt()!=1)throw new IOException("Unsupported sound schema");
            Map<String,String> objects=new LinkedHashMap<String,String>();
            if(!root.has("officialIndex116"))return new Import(objects); // older schema-1 manifests remain valid
            JsonObject spec=root.getAsJsonObject("officialIndex116");
            if(!"8".equals(spec.get("id").getAsString())||!HASH.equals(spec.get("sha1").getAsString())
                    ||spec.get("size").getAsLong()!=SIZE||!URL.equals(spec.get("url").getAsString()))
                throw new IOException("Untrusted official sound index");
            Set<String> used=new HashSet<String>();
            for(JsonElement element:root.getAsJsonArray("entries"))used.add(element.getAsJsonObject().get("resource").getAsString());
            for(JsonElement element:spec.getAsJsonArray("entries")){
                JsonObject entry=element.getAsJsonObject();String path=entry.get("resource").getAsString(),source=entry.get("officialResource").getAsString();
                if(!path.matches("assets/htgt6/sounds/[a-zA-Z0-9_./-]+\\.ogg")||path.contains("..")||path.contains("//")
                        ||!source.equals("minecraft/"+path.substring("assets/htgt6/".length()))||!used.add(path))
                    throw new IOException("Invalid official index import: "+path);
                objects.put(path,source);
            }
            if(objects.size()>64)throw new IOException("Too many index imports");
            return new Import(objects);
        }catch(RuntimeException invalid){throw new IOException("Malformed official index import",invalid);}
    }
    public static List<OfficialSoundCache.Entry> load(File root,Import spec,List<File> objectRoots)throws IOException {
        if(spec.objects.isEmpty())return Collections.emptyList();
        final OfficialSoundCache.Entry index=new OfficialSoundCache.Entry("indexes/8.json",HASH,SIZE);
        // Reuse a launcher's existing index without downloading it, when present.
        File cached=OfficialSoundCache.target(root,index);
        if(!OfficialSoundCache.valid(cached,index)){
            for(File objects:objectRoots){
                final File local=new File(objects.getParentFile(),"indexes/8.json");
                if(OfficialSoundCache.valid(local,index)){
                    OfficialSoundCache.ensure(root,index,Collections.<File>emptyList(),new OfficialSoundCache.Download(){
                        public InputStream open(OfficialSoundCache.Entry ignored)throws IOException{return new FileInputStream(local);}
                    });break;
                }
            }
        }
        OfficialSoundCache.ensure(root,index,Collections.<File>emptyList(),new OfficialSoundCache.Download(){
            public InputStream open(OfficialSoundCache.Entry ignored)throws IOException {
                final HttpURLConnection connection=(HttpURLConnection)new java.net.URL(URL).openConnection();
                connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(5000);connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent","HTGT6-OfficialSoundCache/1");
                try{
                    if(connection.getResponseCode()!=200)throw new IOException("Index HTTP "+connection.getResponseCode());
                    return new FilterInputStream(connection.getInputStream()){
                        private final long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
                        @Override public int read(byte[] b,int off,int len)throws IOException{
                            if(System.nanoTime()>deadline)throw new IOException("Index download deadline exceeded");
                            return super.read(b,off,len);
                        }
                        @Override public void close()throws IOException{try{super.close();}finally{connection.disconnect();}}
                    };
                }catch(IOException failure){connection.disconnect();throw failure;}
            }
        });
        // ensure has already checked the exact bytes, length and SHA-1.
        try(Reader reader=new InputStreamReader(new FileInputStream(cached),"UTF-8")){
            return resolve(spec,new JsonParser().parse(reader).getAsJsonObject());
        }catch(RuntimeException invalid){throw new IOException("Malformed verified asset index",invalid);}
    }
    /** Package-private seam for data validation fixtures; runtime calls it only after verification. */
    static List<OfficialSoundCache.Entry> resolve(Import spec,JsonObject index)throws IOException {
        List<OfficialSoundCache.Entry> result=new ArrayList<OfficialSoundCache.Entry>();
        try{
            JsonObject objects=index.getAsJsonObject("objects");
            for(Map.Entry<String,String> name:spec.objects.entrySet()){
                if(!objects.has(name.getValue()))throw new IOException("Official object missing: "+name.getValue());
                JsonObject object=objects.getAsJsonObject(name.getValue());String hash=object.get("hash").getAsString();long size=object.get("size").getAsLong();
                if(!hash.matches("[0-9a-f]{40}")||size<=0||size>32L*1024*1024)throw new IOException("Invalid official audio object: "+name.getValue());
                result.add(new OfficialSoundCache.Entry(name.getKey(),hash,size));
            }
            return Collections.unmodifiableList(result);
        }catch(RuntimeException invalid){throw new IOException("Malformed audio object",invalid);}
    }
    private OfficialSoundIndex116(){}
}
