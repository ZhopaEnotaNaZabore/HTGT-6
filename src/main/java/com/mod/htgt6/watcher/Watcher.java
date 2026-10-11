package com.mod.htgt6.watcher;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Embedded client integrity monitor. Preview remains opt-in on each JVM launch. */
@Mod(modid = Watcher.MOD_ID, name = "Watcher", version = "0.3.0", dependencies = "required-after:HTGT6")
public final class Watcher {
    public static final String MOD_ID = "htgt6_watcher";
    public static final WatcherState STATE = new WatcherState();
    private static final Logger LOG = LogManager.getLogger("HTGT6-Watcher");

    @SidedProxy(clientSide = "com.mod.htgt6.watcher.client.WatcherClient",
            serverSide = "com.mod.htgt6.watcher.WatcherProxy")
    public static WatcherProxy proxy;

    private void checkpoint(WatcherState.Phase phase) {
        STATE.checkpoint(phase);
        WatcherRuntime.checkpoint(phase);
        LOG.info("Watcher checkpoint: {}", phase);
    }

    @Mod.EventHandler public void construction(FMLConstructionEvent event) {
        checkpoint(WatcherState.Phase.CONSTRUCTION);
    }
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        checkpoint(WatcherState.Phase.PRE_INIT);
        proxy.initialize();
    }
    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        checkpoint(WatcherState.Phase.INIT);
    }
    @Mod.EventHandler public void postInit(FMLPostInitializationEvent event) {
        checkpoint(WatcherState.Phase.POST_INIT);
    }
    @Mod.EventHandler public void loadComplete(FMLLoadCompleteEvent event) {
        checkpoint(WatcherState.Phase.LOAD_COMPLETE);
    }
}
