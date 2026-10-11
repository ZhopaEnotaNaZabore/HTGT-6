package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.VillageAndPillageUpdateClient;

import com.mod.htgt6.minecraft1202.Minecraft1202;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager;
import com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage.PillagerOutpostGenerator;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.RaidAndPatrolHandler;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.VillagePoiHandler120;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Single lifecycle entry point for the Minecraft 1.14 Village & Pillage backport. */
public final class VillageAndPillageUpdate {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-VillageAndPillage");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120.init();
        VillagePillageContent.register();
        registerEntities();
        com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.init();
        RaidAndPatrolHandler handler = new RaidAndPatrolHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
        VillagePoiHandler120 poiHandler = new VillagePoiHandler120();
        FMLCommonHandler.instance().bus().register(poiHandler);
        MinecraftForge.EVENT_BUS.register(poiHandler);
        PillagerOutpostGenerator.validateTemplates();
        PillagerOutpostGenerator outposts=new PillagerOutpostGenerator();
        GameRegistry.registerWorldGenerator(outposts, 20);
        MinecraftForge.EVENT_BUS.register(outposts);
        registerClientContent();
        LOGGER.info("Village & Pillage module initialized");
    }

    private static void registerEntities() {
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRaidWitch151.class,
                "HTGT6.RaidWitch", 52, Minecraft1202.instance, 80, 3, true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityCrossbowRocket143.class,
                "CrossbowRocket", 51, Minecraft1202.instance, 64, 1, true);
        int pillagerId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityPillager.class, "HTGT6.Pillager", pillagerId,
                0x532F36, 0x959B9B);
        EntityRegistry.registerModEntity(EntityPillager.class, "Pillager", 20, Minecraft1202.instance,
                80, 3, true);

        int ravagerId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityRavager.class, "HTGT6.Ravager", ravagerId,
                0x4B3B36, 0x78716A);
        EntityRegistry.registerModEntity(EntityRavager.class, "Ravager", 21, Minecraft1202.instance,
                96, 3, true);
    }

    private static void registerClientContent() {
        if (!FMLCommonHandler.instance().getSide().isClient()) return;
        try {
            Class.forName("com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.VillageAndPillageUpdateClient")
                    .getMethod("init").invoke(null);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize Village & Pillage client content", exception);
        }
    }

    private VillageAndPillageUpdate() { }
}
