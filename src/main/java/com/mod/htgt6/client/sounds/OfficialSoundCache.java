package com.mod.htgt6.client.sounds;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Pinned data-only downloads. No Minecraft/client classes, so cache logic is testable offline. */
public final class OfficialSoundCache {
    public static final class Entry {
        public final String resource, sha1;
        public final long size;
        Entry(String resource, String sha1, long size) { this.resource=resource; this.sha1=sha1; this.size=size; }
        public String url() { return "https://resources.download.minecraft.net/"+sha1.substring(0,2)+"/"+sha1; }
    }
    public interface Download { InputStream open(Entry entry) throws IOException; }
    public static final Download HTTPS = new Download() {
        public InputStream open(Entry entry) throws IOException {
            final HttpURLConnection connection=(HttpURLConnection)new URL(entry.url()).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(5000); connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent","HTGT6-OfficialSoundCache/1");
            try {
                if(connection.getResponseCode()!=200)throw new IOException("HTTP "+connection.getResponseCode());
                return new FilterInputStream(connection.getInputStream()) {
                    private final long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
                    @Override public int read(byte[] buffer,int offset,int length)throws IOException {
                        if(System.nanoTime()>deadline)throw new IOException("Sound download deadline exceeded");
                        return super.read(buffer,offset,length);
                    }
                    @Override public void close() throws IOException { try { super.close(); } finally { connection.disconnect(); } }
                };
            } catch(IOException failure) { connection.disconnect(); throw failure; }
        }
    };
    private OfficialSoundCache() {}

    public static List<Entry> read(InputStream stream) throws IOException {
        if(stream==null)throw new IOException("Missing official sound manifest");
        try(Reader reader=new InputStreamReader(stream,"UTF-8")) {
            JsonObject root=new JsonParser().parse(reader).getAsJsonObject();
            if(root.get("schema").getAsInt()!=1)throw new IOException("Unsupported sound manifest schema");
            List<Entry> entries=new ArrayList<Entry>(); Set<String> names=new HashSet<String>();
            for(JsonElement value:root.getAsJsonArray("entries")) {
                JsonObject item=value.getAsJsonObject();
                String path=item.get("resource").getAsString(), hash=item.get("sha1").getAsString();
                long size=item.get("size").getAsLong();
                if(!path.matches("assets/[a-z0-9_]+/sounds/[a-zA-Z0-9_./-]+\\.ogg")
                        ||path.contains("..")||path.contains("//")||!hash.matches("[0-9a-f]{40}")
                        ||size<=0||size>32L*1024*1024||!names.add(path))throw new IOException("Invalid sound manifest entry: "+path);
                entries.add(new Entry(path,hash,size));
            }
            return Collections.unmodifiableList(entries);
        } catch(RuntimeException failure) { throw new IOException("Malformed sound manifest",failure); }
    }
    public static File target(File root,Entry entry) throws IOException {
        File canonicalRoot=root.getCanonicalFile(), file=new File(canonicalRoot,entry.resource).getCanonicalFile();
        if(!file.toPath().startsWith(canonicalRoot.toPath()))throw new IOException("Sound path escapes cache");
        return file;
    }
    public static boolean valid(File file,Entry entry) throws IOException {
        if(!file.isFile()||file.length()!=entry.size)return false;
        MessageDigest digest;
        try { digest=MessageDigest.getInstance("SHA-1"); } catch(NoSuchAlgorithmException impossible) { throw new IOException(impossible); }
        try(InputStream in=new FileInputStream(file)) {
            byte[] buffer=new byte[16384]; int count;
            while((count=in.read(buffer))!=-1)digest.update(buffer,0,count);
        }
        StringBuilder hash=new StringBuilder();
        for(byte b:digest.digest())hash.append(String.format(Locale.ROOT,"%02x",b&255));
        return entry.sha1.equals(hash.toString());
    }
    /** Returns false for an already-valid cache entry, true for a newly installed object. */
    public static boolean ensure(File root,Entry entry,List<File> objectRoots,Download download) throws IOException {
        File dest=target(root,entry);
        if(valid(dest,entry))return false;
        for(File objects:objectRoots) {
            File cached=new File(objects,entry.sha1.substring(0,2)+"/"+entry.sha1);
            if(valid(cached,entry)) {
                try(InputStream in=new FileInputStream(cached)) { install(dest,entry,in); }
                return true;
            }
        }
        try(InputStream in=download.open(entry)) { install(dest,entry,in); }
        return true;
    }
    private static void install(File dest,Entry entry,InputStream in) throws IOException {
        Files.createDirectories(dest.getParentFile().toPath());
        Path temporary=Files.createTempFile(dest.getParentFile().toPath(),".sound-",".part");
        try {
            try(OutputStream out=Files.newOutputStream(temporary)) {
                byte[] buffer=new byte[16384]; int count; long total=0;
                while((count=in.read(buffer))!=-1) {
                    total+=count;if(total>entry.size)throw new IOException("Oversized sound: "+entry.resource);
                    out.write(buffer,0,count);
                }
            }
            if(!valid(temporary.toFile(),entry))throw new IOException("Sound checksum/size mismatch: "+entry.resource);
            try { Files.move(temporary,dest.toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch(AtomicMoveNotSupportedException ignored) { Files.move(temporary,dest.toPath(),StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
