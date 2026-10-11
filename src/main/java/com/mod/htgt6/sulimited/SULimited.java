package com.mod.htgt6.sulimited;

import com.mod.htgt6.common.logging.LifecycleAudit;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;


/**
 * SpaceUltimate is a child Forge mod shipped in the HTGT6 JAR.  Keeping its
 * own mod id separates Galacticraft content in the Mods list and lifecycle,
 * while the parent retains legacy registry names required by existing worlds.
 */
@Mod(modid = SULimited.MOD_ID, name = "SU-Limited", version = "1.0.4",
        dependencies = "required-after:HTGT6;required-after:GalacticraftCore;required-after:GalaxySpace")
public final class SULimited {
    public static final String MOD_ID = "SU-Limited";

    @SidedProxy(clientSide = "com.mod.htgt6.sulimited.SULimitedClientProxy",
            serverSide = "com.mod.htgt6.sulimited.SULimitedProxy")
    public static SULimitedProxy proxy;

    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        long started = LifecycleAudit.begin(MOD_ID, "PRE_INIT");
        proxy.preInit(event);
        LifecycleAudit.end(MOD_ID, "PRE_INIT", started);
    }

    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        long started = LifecycleAudit.begin(MOD_ID, "INIT");
        proxy.init(event);
        LifecycleAudit.end(MOD_ID, "INIT", started);
    }

    @Mod.EventHandler public void postInit(FMLPostInitializationEvent event) {
        long started = LifecycleAudit.begin(MOD_ID, "POST_INIT");
        proxy.postInit(event);
        LifecycleAudit.end(MOD_ID, "POST_INIT", started);
    }

    @Mod.EventHandler public void loadComplete(FMLLoadCompleteEvent event) {
        long started = LifecycleAudit.begin(MOD_ID, "LOAD_COMPLETE");
        proxy.loadComplete(event);
        LifecycleAudit.end(MOD_ID, "LOAD_COMPLETE", started);
    }
}
