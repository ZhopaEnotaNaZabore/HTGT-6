package com.mod.htgt6.client.sounds;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Client proxy entry point only. Server startup never loads this class. */
public final class OfficialSoundsClient {
    private static final Logger LOG=LogManager.getLogger("HTGT6-SoundCache");
    private static boolean started;
    private volatile boolean reload;
    private volatile boolean finished;

    public static synchronized void start() {
        if(started)return;
        started=true;
        try {
            final List<OfficialSoundCache.Entry> entries=OfficialSoundCache.read(
                    OfficialSoundsClient.class.getResourceAsStream("/htgt6-external-sounds.json"));
            final OfficialSoundIndex116.Import imported=OfficialSoundIndex116.read(
                    OfficialSoundsClient.class.getResourceAsStream("/htgt6-external-sounds.json"));
            final Minecraft mc=Minecraft.getMinecraft();
            final File root=new File(mc.mcDataDir,"resources/htgt6-sounds");
            Files.createDirectories(root.toPath());
            Files.write(new File(root,"pack.mcmeta").toPath(),
                    "{\"pack\":{\"pack_format\":1,\"description\":\"HTGT6 official sound cache\"}}".getBytes(StandardCharsets.UTF_8));
            try(InputStream manifest=OfficialSoundsClient.class.getResourceAsStream("/htgt6-external-sounds.json")) {
                Files.copy(manifest,new File(root,"manifest.json").toPath(),StandardCopyOption.REPLACE_EXISTING);
            }
            final Set<String> ready=Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());
            final Set<String> domains=new HashSet<String>();
            for(OfficialSoundCache.Entry entry:entries)domains.add(entry.resource.split("/")[1]);
            for(String resource:imported.objects.keySet())domains.add(resource.split("/")[1]);
            List<IResourcePack> packs=ReflectionHelper.getPrivateValue(FMLClientHandler.class,FMLClientHandler.instance(),"resourcePackList");
            if(packs==null)throw new IOException("FML resource packs are not initialized");
            // Lowest priority: user packs and bundled fallback resources retain precedence.
            packs.add(0,new CachePack(root,ready,domains));
            final OfficialSoundsClient listener=new OfficialSoundsClient();
            FMLCommonHandler.instance().bus().register(listener);
            final List<File> sources=new ArrayList<File>();
            sources.add(new File(mc.mcDataDir,"assets/objects"));
            sources.add(new File(System.getProperty("user.home"),".minecraft/assets/objects"));
            sources.add(new File(System.getProperty("user.home"),".gradle/caches/minecraft/assets/objects"));
            String appData=System.getenv("APPDATA");
            if(appData!=null)sources.add(new File(appData,".minecraft/assets/objects"));
            Thread coordinator=new Thread(new Runnable(){public void run(){listener.prepare(root,entries,imported,sources,ready);}},"HTGT6 sound cache");
            coordinator.setDaemon(true);coordinator.start();
        } catch(Exception failure) { LOG.error("Cannot initialize external sounds; see resources/htgt6-sounds and restart after fixing the cause",failure); }
    }

    private void prepare(final File root,final List<OfficialSoundCache.Entry> existing,final OfficialSoundIndex116.Import imported,
                         final List<File> sources,final Set<String> ready) {
        final List<OfficialSoundCache.Entry> entries=new ArrayList<OfficialSoundCache.Entry>(existing);
        int unresolved=0;
        try { entries.addAll(OfficialSoundIndex116.load(root,imported,sources)); }
        catch(IOException failure) {
            unresolved=imported.objects.size();
            LOG.warn("Pinned lodestone sound index unavailable; existing sound entries are unaffected and imports retry next launch",failure);
        }
        final int expected=existing.size()+imported.objects.size();
        final AtomicInteger installed=new AtomicInteger(), failed=new AtomicInteger(unresolved), completed=new AtomicInteger();
        final long deadline=System.nanoTime()+TimeUnit.MINUTES.toNanos(3);
        ExecutorService workers=Executors.newFixedThreadPool(2,new ThreadFactory(){
            public Thread newThread(Runnable job){Thread t=new Thread(job,"HTGT6 sound download");t.setDaemon(true);return t;}
        });
        LOG.info("Preparing {} official sounds in {}",entries.size(),root);
        try {
            for(final OfficialSoundCache.Entry entry:entries)workers.submit(new Runnable(){public void run(){
                Exception error=null;
                for(int attempt=0;attempt<2;attempt++) {
                    if(System.nanoTime()>deadline) { error=new IOException("Startup sound download time budget exceeded");break; }
                    try {
                        if(OfficialSoundCache.ensure(root,entry,sources,OfficialSoundCache.HTTPS))installed.incrementAndGet();
                        ready.add(entry.resource);error=null;break;
                    } catch(Exception failure) { error=failure; }
                }
                if(error!=null) {
                    if(failed.incrementAndGet()<=5)LOG.warn("Sound unavailable: {} ({})",entry.resource,error.toString());
                }
                int count=completed.incrementAndGet();
                if(count%100==0)LOG.info("Official sounds checked: {}/{}, unavailable: {}",count,entries.size(),failed.get());
            }});
            workers.shutdown();
            while(!workers.awaitTermination(1,TimeUnit.SECONDS)) { /* bounded network operations in workers */ }
            String status="ready="+ready.size()+"\ntotal="+expected+"\ninstalled="+installed.get()+"\nmissing="+failed.get()+"\n";
            Files.write(new File(root,"status.txt").toPath(),status.getBytes(StandardCharsets.UTF_8));
            LOG.info("Official sounds ready: {}/{}, installed: {}, missing: {}. Missing files retry on next launch.",ready.size(),expected,installed.get(),failed.get());
            reload=!ready.isEmpty();
        } catch(Exception failure) { LOG.error("Official sound cache preparation failed",failure);reload=!ready.isEmpty(); }
        finally { workers.shutdownNow();finished=true; }
    }
    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||!finished)return;
        if(!reload){FMLCommonHandler.instance().bus().unregister(this);return;}
        Minecraft mc=Minecraft.getMinecraft();
        // Never stop/rebuild the sound engine while playing or connecting. The
        // verified folder stays mounted; activate newly available files in a menu.
        if(mc.theWorld!=null || mc.getNetHandler()!=null)return;
        reload=false;
        // The resource pack was mounted in preInit. Rebuild only sound events;
        // refreshing every resource also rebuilds textures/models and stalls play.
        try { mc.getSoundHandler().onResourceManagerReload(mc.getResourceManager());LOG.info("External sound cache activated from resources/htgt6-sounds (sound-only reload)"); }
        catch(Exception failure) { LOG.error("External sound reload failed",failure); }
        finally { FMLCommonHandler.instance().bus().unregister(this); }
    }
    public static final class CachePack extends FolderResourcePack {
        private final Set<String> ready,domains;
        public CachePack(File root,Set<String> ready,Set<String> domains){super(root);this.ready=ready;this.domains=Collections.unmodifiableSet(new HashSet<String>(domains));}
        @Override protected boolean hasResourceName(String name){return ready.contains(name)&&super.hasResourceName(name);}
        @Override protected InputStream getInputStreamByName(String name)throws IOException {
            if(!"pack.mcmeta".equals(name)&&!ready.contains(name))throw new FileNotFoundException(name);
            return super.getInputStreamByName(name);
        }
        @Override public Set<String> getResourceDomains(){return domains;}
        @Override public String getPackName(){return "HTGT6 official sounds";}
    }
}
