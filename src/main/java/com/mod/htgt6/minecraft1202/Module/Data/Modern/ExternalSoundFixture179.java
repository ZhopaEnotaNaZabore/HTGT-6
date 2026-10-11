package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.client.sounds.OfficialSoundCache;
import java.io.*;
import java.util.*;

/** Offline contract-fixture access to the same validated external sound files as the client. */
public final class ExternalSoundFixture179 {
    private static Map<String,OfficialSoundCache.Entry> entries;
    private ExternalSoundFixture179() {}
    public static synchronized InputStream open(String resource)throws IOException {
        if(entries==null){
            entries=new HashMap<String,OfficialSoundCache.Entry>();
            for(OfficialSoundCache.Entry entry:OfficialSoundCache.read(ExternalSoundFixture179.class.getResourceAsStream("/htgt6-external-sounds.json")))
                entries.put(entry.resource,entry);
        }
        OfficialSoundCache.Entry entry=entries.get(resource);
        if(entry==null)return ExternalSoundFixture179.class.getClassLoader().getResourceAsStream(resource);
        File root=new File(System.getProperty("htgt6.soundCache","run/resources/htgt6-sounds"));
        File sound=OfficialSoundCache.target(root,entry);
        if(!OfficialSoundCache.valid(sound,entry))throw new IOException("Missing/invalid external fixture sound: "+sound+"; populate cache or set -Dhtgt6.soundCache");
        return new FileInputStream(sound);
    }
}
