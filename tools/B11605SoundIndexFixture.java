package com.mod.htgt6.client.sounds;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
/** Exercises real manifest parsing, object resolution, checksum rejection and cache reuse. */
public final class B11605SoundIndexFixture {
    static int checks;
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
    static InputStream bytes(String text)throws Exception{return new ByteArrayInputStream(text.getBytes("UTF-8"));}
    public static void main(String[] args)throws Exception{
        String text=new String(Files.readAllBytes(Paths.get("src/main/resources/htgt6-external-sounds.json")),"UTF-8");
        OfficialSoundIndex116.Import spec=OfficialSoundIndex116.read(bytes(text));int lodestone=0;for(String source:spec.objects.values())if(source.contains("/lodestone/")||source.matches("minecraft/sounds/(dig|step)/stone[1-6]\\.ogg"))lodestone++;
        check(lodestone==16,"sixteen original lodestone/stone audio imports retained");
        for(String invalid:new String[]{text.replace(OfficialSoundIndex116.HASH,"0000000000000000000000000000000000000000"),text.replace("assets/htgt6/sounds/block/lodestone/lock1.ogg","assets/htgt6/sounds/../lock1.ogg"),text.replace(OfficialSoundIndex116.URL,"https://example.invalid/8.json")}){
            try{OfficialSoundIndex116.read(bytes(invalid));throw new AssertionError("invalid manifest accepted");}catch(IOException expected){checks++;}
        }
        // Synthetic data exercises validation; it is never passed off as official audio.
        JsonObject index=new JsonObject(),objects=new JsonObject();index.add("objects",objects);
        for(String source:spec.objects.values()){
            JsonObject object=new JsonObject();object.addProperty("hash","a9993e364706816aba3e25717850c26c9cd0d89d");object.addProperty("size",3);objects.add(source,object);
        }
        List<OfficialSoundCache.Entry> resolved=OfficialSoundIndex116.resolve(spec,index);check(resolved.size()==spec.objects.size(),"resolve every object");
        Path cache=Files.createTempDirectory("b11605-cache-");OfficialSoundCache.Entry first=resolved.get(0);
        try{
            check(OfficialSoundCache.ensure(cache.toFile(),first,Collections.<File>emptyList(),e->bytesUnchecked("abc")),"install verified synthetic bytes");
            check(!OfficialSoundCache.ensure(cache.toFile(),first,Collections.<File>emptyList(),e->{throw new AssertionError("unexpected network");}),"offline reuse");
            Files.delete(OfficialSoundCache.target(cache.toFile(),first).toPath());
            try{OfficialSoundCache.ensure(cache.toFile(),first,Collections.<File>emptyList(),e->bytesUnchecked("bad"));throw new AssertionError("bad bytes accepted");}catch(IOException expected){checks++;}
            check(!OfficialSoundCache.target(cache.toFile(),first).exists(),"corrupt object never published");
            String key=spec.objects.values().iterator().next();JsonElement removed=objects.remove(key);
            try{OfficialSoundIndex116.resolve(spec,index);throw new AssertionError("missing object accepted");}catch(IOException expected){checks++;}
            objects.add(key,removed);removed.getAsJsonObject().addProperty("size",Long.MAX_VALUE);
            try{OfficialSoundIndex116.resolve(spec,index);throw new AssertionError("unbounded size accepted");}catch(IOException expected){checks++;}
        }finally{try(java.util.stream.Stream<Path> files=Files.walk(cache)){for(Path path:(Iterable<Path>)files.sorted(Comparator.reverseOrder())::iterator)Files.deleteIfExists(path);}}
        System.out.println("PASS B116-05 sound index/cache: "+checks+" checks (no network)");
    }
    static InputStream bytesUnchecked(String text){return new ByteArrayInputStream(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
}
