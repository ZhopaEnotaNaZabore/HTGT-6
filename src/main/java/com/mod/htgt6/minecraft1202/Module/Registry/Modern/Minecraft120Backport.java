package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Lifecycle entry point for the staged Minecraft Java 1.20.2 content and worldgen backport. */
public final class Minecraft120Backport {
    private static final Logger LOGGER = LogManager.getLogger("Minecraft_1.20.2-Futura");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        Backport120Content.register();
        EtFVanillaGapContent120.register();
        LodestoneContent116.register();
        NetherUtilityContent116.register();
        NetherMasonry116.register();
        BlackstoneShapes116.register();
        BlackstoneCrafting116.register();
        NetherWoodContent116.register();
        NetherVegetationContent116.register();
        StriderContent116.register();
        com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneWire116.register();
        com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.FishingLoot116.register();
        NetherTreasure116.register();
        com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageBuildingContent114.register();
        com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.register();
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.SignDye114.init();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerGossip114());
        com.mod.htgt6.minecraft1202.Module.World.Modern.VillageFauna114 fauna=new com.mod.htgt6.minecraft1202.Module.World.Modern.VillageFauna114();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(fauna);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(fauna);
        com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageFoodRecipes114.register();
        com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent.registerModernIngredientRecipes();
        com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.CamelMountSystem120.init();
        Backport120Entities.register();
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NeutralAnger116());
        com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.GT6DeepslateOreIntegration.register();
        com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120.register();
        Backport120Entities.registerFuturaBiomeSpawns();
        cpw.mods.fml.common.registry.GameRegistry.registerWorldGenerator(
                new com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaRuinedPortalGenerator120(), 19);
        com.mod.htgt6.minecraft1202.Module.World.Modern.TrailRuinsGenerator120.validateTemplates();
        com.mod.htgt6.minecraft1202.Module.World.Modern.TrailRuinsGenerator120 trailRuins=
                new com.mod.htgt6.minecraft1202.Module.World.Modern.TrailRuinsGenerator120();
        cpw.mods.fml.common.registry.GameRegistry.registerWorldGenerator(trailRuins, 18);
        MinecraftForge.EVENT_BUS.register(trailRuins);
        com.mod.htgt6.minecraft1202.Module.World.Modern.NetherFossilGenerator120.validateTemplates();
        com.mod.htgt6.minecraft1202.Module.World.Modern.NetherFossilGenerator120 netherFossils=new com.mod.htgt6.minecraft1202.Module.World.Modern.NetherFossilGenerator120();
        cpw.mods.fml.common.registry.GameRegistry.registerWorldGenerator(netherFossils, 18);
        MinecraftForge.EVENT_BUS.register(netherFossils);
        // Keep production startup limited to cheap safety contracts. Fixed-seed
        // fingerprints, bytecode fixtures and full NBT/template walks are CI/dev
        // tests; running them on every client and dedicated server launch made
        // the backport appear to stall before a world was even opened.
        com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.validateContract();
        com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.validateContract();
        com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120DeathTracker.init();
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120BucketHandler());
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120AxeStrippingHandler());
        com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120SculkHandler sculkHandler=
                new com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120SculkHandler();
        MinecraftForge.EVENT_BUS.register(sculkHandler);
        FMLCommonHandler.instance().bus().register(sculkHandler);
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSpawnHandler120());
        FMLCommonHandler.instance().bus().register(
                new com.mod.htgt6.minecraft1202.Module.World.Modern.WanderingTraderSpawner120());
        registerClientContent();
        initialized = true;
        LOGGER.info("Minecraft Java 1.20.2 staged backport content initialized");
    }

    private static void registerClientContent() {
        if (!FMLCommonHandler.instance().getSide().isClient()) return;
        try {
            Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient")
                    .getMethod("init").invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize Minecraft 1.20.2 client content", e);
        }
    }

    private Minecraft120Backport() { }
}
