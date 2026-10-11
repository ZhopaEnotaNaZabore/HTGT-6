import com.mod.htgt6.client.sounds.*;
import net.minecraft.util.ResourceLocation;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public final class OfficialSounds178Fixture {
    static int checks, fetches;
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
    public static void main(String[] args)throws Exception {
        List<OfficialSoundCache.Entry> entries=OfficialSoundCache.read(new FileInputStream("src/main/resources/htgt6-external-sounds.json"));
        if(args.length>0&&args[0].equals("--network-probe")) {
            for(OfficialSoundCache.Entry e:entries)if(e.resource.endsWith("composter/ready1.ogg")) {
                File probe=Files.createTempDirectory(Paths.get("build/sounds178"),"https-probe-").toFile();
                check(OfficialSoundCache.ensure(probe,e,Collections.<File>emptyList(),OfficialSoundCache.HTTPS),"official HTTPS download");
                check(OfficialSoundCache.valid(OfficialSoundCache.target(probe,e),e),"official hash");
                System.out.println("PASS Java8 official HTTPS probe: "+checks);return;
            }
            throw new AssertionError("Missing probe entry");
        }
        File root=Files.createTempDirectory(Paths.get("build/sounds178"),"cache-test-").toFile();
        List<File> noCache=Collections.emptyList();
        OfficialSoundCache.Download source=new OfficialSoundCache.Download(){public InputStream open(OfficialSoundCache.Entry entry)throws IOException {
            fetches++;File existing=OfficialSoundCache.target(new File(System.getProperty("htgt6.soundCache","run/resources/htgt6-sounds")),entry);
            if(!OfficialSoundCache.valid(existing,entry))throw new IOException("Prepare external cache before integration fixture: "+entry.resource);
            return new FileInputStream(existing);
        }};
        OfficialSoundCache.Download offline=new OfficialSoundCache.Download(){public InputStream open(OfficialSoundCache.Entry entry)throws IOException {
            throw new AssertionError("Unexpected network for valid cache");
        }};
        Set<String> ready=new HashSet<String>();
        for(OfficialSoundCache.Entry entry:entries) {
            check(OfficialSoundCache.ensure(root,entry,noCache,source),"first install "+entry.resource);
            check(OfficialSoundCache.valid(OfficialSoundCache.target(root,entry),entry),"installed bytes");
            check(!OfficialSoundCache.ensure(root,entry,noCache,offline),"offline reuse");ready.add(entry.resource);
        }
        check(fetches==598,"full manifest download simulation");
        OfficialSoundCache.Entry first=entries.get(0);File dest=OfficialSoundCache.target(root,first);
        Files.write(dest.toPath(),new byte[]{1,2,3});
        check(OfficialSoundCache.ensure(root,first,noCache,source)&&OfficialSoundCache.valid(dest,first),"corrupt cache repaired");
        OfficialSoundsClient.CachePack pack=new OfficialSoundsClient.CachePack(root,ready,Collections.singleton("htgt6"));
        ResourceLocation sound=new ResourceLocation("htgt6",first.resource.substring("assets/htgt6/".length()));
        check(pack.resourceExists(sound),"mounted resource namespace");
        try(InputStream in=pack.getInputStream(sound)){check(in.read()!=-1,"mounted bytes readable");}
        ready.remove(first.resource);check(!pack.resourceExists(sound),"unverified file invisible");
        try{pack.getInputStream(sound);throw new AssertionError("unverified readable");}catch(FileNotFoundException expected){checks++;}
        File bad=Files.createTempDirectory(root.toPath(),"bad-").toFile();
        try{OfficialSoundCache.ensure(bad,first,noCache,e->new ByteArrayInputStream(new byte[]{9}));throw new AssertionError("bad accepted");}catch(IOException expected){checks++;}
        check(!OfficialSoundCache.target(bad,first).exists(),"failed download not published");
        try(java.util.stream.Stream<Path> files=Files.walk(bad.toPath())){check(!files.anyMatch(p->p.toString().endsWith(".part")),"temporary removed");}
        File objects=new File(root,"launcher-objects");File object=new File(objects,first.sha1.substring(0,2)+"/"+first.sha1);Files.createDirectories(object.getParentFile().toPath());Files.copy(dest.toPath(),object.toPath());
        check(OfficialSoundCache.ensure(bad,first,Collections.singletonList(objects),offline),"launcher cache avoids network");
        String json=new String(Files.readAllBytes(Paths.get("src/main/resources/htgt6-external-sounds.json")),"UTF-8");
        try{OfficialSoundCache.read(new ByteArrayInputStream(json.replace(first.resource,"assets/htgt6/sounds/../../evil.ogg").getBytes("UTF-8")));throw new AssertionError("traversal accepted");}catch(IOException expected){checks++;}
        check(first.url().startsWith("https://resources.download.minecraft.net/"),"pinned official origin");
        System.out.println("PASS official-sounds178: "+checks+" checks; cache="+root);
    }
}
