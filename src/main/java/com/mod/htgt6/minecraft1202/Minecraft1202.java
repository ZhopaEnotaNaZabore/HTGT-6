package com.mod.htgt6.minecraft1202;

import com.mod.htgt6.common.logging.LifecycleAudit;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.MinecraftBackportGuiHandler;

import com.mod.htgt6.minecraft1202.Module.World.Shared.CommandFindBiome120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.MinecraftBackports;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

/**
 * Forge mod container for every Minecraft gameplay/content backport bundled in
 * the HTGT6 JAR. The coremod manifest and every ASM transformer deliberately
 * remain owned by HTGT6; this container only owns ordinary Forge lifecycle
 * registration such as blocks, items, entities, handlers and Futura worldgen.
 */
@Mod(modid = Minecraft1202.MOD_ID, name = Minecraft1202.NAME, version = "1.0.4",
        dependencies = "required-after:HTGT6")
public final class Minecraft1202 {
    public static final String MOD_ID = "Minecraft_1.20.2";
    public static final String NAME = "Minecraft_1.20.2";

    @Mod.Instance(MOD_ID)
    public static Minecraft1202 instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        long lifecycleStarted = LifecycleAudit.begin(MOD_ID, "PRE_INIT");
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new MinecraftBackportGuiHandler());
        MinecraftBackports.init();
        LifecycleAudit.end(MOD_ID, "PRE_INIT", lifecycleStarted);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        long lifecycleStarted = LifecycleAudit.begin(MOD_ID, "INIT");
        LifecycleAudit.end(MOD_ID, "INIT", lifecycleStarted);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        long lifecycleStarted = LifecycleAudit.begin(MOD_ID, "POST_INIT");
        com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.init();
        LifecycleAudit.end(MOD_ID, "POST_INIT", lifecycleStarted);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        long lifecycleStarted = LifecycleAudit.begin(MOD_ID, "LOAD_COMPLETE");
        LifecycleAudit.end(MOD_ID, "LOAD_COMPLETE", lifecycleStarted);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandFindBiome120());
        event.registerServerCommand(new com.mod.htgt6.minecraft1202.Module.World.Shared.CommandAttribute116());
    }

}
