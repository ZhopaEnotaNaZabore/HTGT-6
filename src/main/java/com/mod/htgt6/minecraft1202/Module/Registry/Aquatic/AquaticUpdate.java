package com.mod.htgt6.minecraft1202.Module.Registry.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.AquaticUpdateClient;

import com.mod.htgt6.minecraft1202.Minecraft1202;
import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDrowned;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.BuriedTreasureHandler;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntitySeaTurtle;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityPhantom120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanAquaticHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.RiptideAttackHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenOceanRuin;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.ZombieDrowningHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMapHandler120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenVanillaShipwreck;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanStructureHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.PhantomSpawnHandler120;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.EnumCreatureType;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulGuardian;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Single lifecycle entry point for the 1.13+ ocean backport.
 *
 * Keep module-owned registration here. The main mod must only call {@link #init()}.
 */
public final class AquaticUpdate {
    // All backport updates share Minecraft1202.instance, so these network IDs
    // must be unique across Frostburn (4..7), Bountiful (10..13), Combat
    // (14..19), Village/Pillage (20..21), Exploration (22..27) and caves
    // (28..29).  The old aquatic 1..9 range collided with Stray at ID 5.
    public static final int DROWNED_MOD_ID=30,TRIDENT_MOD_ID=31,TURTLE_MOD_ID=32,
            COD_MOD_ID=33,SALMON_MOD_ID=34,PUFFERFISH_MOD_ID=35,TROPICAL_FISH_MOD_ID=36,
            DOLPHIN_MOD_ID=37,PHANTOM_MOD_ID=38;
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-AquaticUpdate");
    private static boolean initialized;
    private static final Registration[] REGISTRATIONS = {
            new Registration() { @Override public void run() { registerBiomes(); } },
            new Registration() { @Override public void run() { registerContent(); } },
            new Registration() { @Override public void run() { registerWorldGeneration(); } },
            new Registration() { @Override public void run() { registerEntities(); } },
            new Registration() { @Override public void run() { registerClientContent(); } }
    };

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        if (!HTGT6Config.RUN_MINECRAFT_1_7_9999) {
            LOGGER.warn("Update Aquatic module was called while Minecraft 1.7.9999 backports are disabled");
            return;
        }

        for (Registration registration : REGISTRATIONS) registration.run();
        LOGGER.info("Update Aquatic module initialized");
    }

    private static void registerBiomes() {
        OceanBiomeRegistry.register();
    }

    private static void registerContent() {
        com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.AquaticEffects120.init();
        com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.SwimmingSystem120.init();
        OceanContent.registerBlocks();
        VanillaTreasureMapHandler120 treasureMaps = new VanillaTreasureMapHandler120();
        FMLCommonHandler.instance().bus().register(treasureMaps);
        MinecraftForge.EVENT_BUS.register(treasureMaps);
    }

    private static void registerWorldGeneration() {
        // Forge 1.7 posts DecorateBiomeEvent.Pre/Post on EVENT_BUS. Only the
        // cancellable per-feature Decorate event is posted on TERRAIN_GEN_BUS.
        MinecraftForge.EVENT_BUS.register(new OceanStructureHandler());
        MinecraftForge.EVENT_BUS.register(new OceanAquaticHandler());
        MinecraftForge.EVENT_BUS.register(new BuriedTreasureHandler());
        MinecraftForge.EVENT_BUS.register(new ZombieDrowningHandler());
        MinecraftForge.EVENT_BUS.register(new RiptideAttackHandler());
        FMLCommonHandler.instance().bus().register(new PhantomSpawnHandler120());
    }

    private static void registerEntities() {
        int drownedGlobalId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityDrowned.class, "HTGT6.Drowned", drownedGlobalId,
                0x2F5D62, 0x78A7A3);
        EntityRegistry.registerModEntity(EntityDrowned.class, "Drowned", DROWNED_MOD_ID, Minecraft1202.instance,
                64, 3, true);
        EntityRegistry.registerModEntity(EntityThrownTrident.class, "ThrownTrident", TRIDENT_MOD_ID, Minecraft1202.instance,
                64, 10, true);

        int turtleGlobalId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntitySeaTurtle.class, "HTGT6.SeaTurtle", turtleGlobalId,
                0x2E8B57, 0xE7D38B);
        EntityRegistry.registerModEntity(EntitySeaTurtle.class, "SeaTurtle", TURTLE_MOD_ID, Minecraft1202.instance,
                80, 3, true);
        registerAquaticEntity(EntityAquaticFish120.Cod.class,"HTGT6.Cod","Cod",COD_MOD_ID,0xC1C1C1,0x5A5A5A);
        registerAquaticEntity(EntityAquaticFish120.Salmon.class,"HTGT6.Salmon","Salmon",SALMON_MOD_ID,0xA00F10,0x0E8474);
        registerAquaticEntity(EntityAquaticFish120.Pufferfish.class,"HTGT6.Pufferfish","Pufferfish",PUFFERFISH_MOD_ID,0xF6B201,0x37C3F2);
        registerAquaticEntity(EntityAquaticFish120.Tropical.class,"HTGT6.TropicalFish","TropicalFish",TROPICAL_FISH_MOD_ID,0xEF6915,0xFFF9EF);
        registerAquaticEntity(EntityDolphin120.class,"HTGT6.Dolphin","Dolphin",DOLPHIN_MOD_ID,0x224D63,0xF9F9F9);
        registerAquaticEntity(EntityPhantom120.class,"HTGT6.Phantom","Phantom",PHANTOM_MOD_ID,0x43518A,0x88FF00);

        EntityRegistry.addSpawn(EntityDrowned.class, 5, 1, 1, EnumCreatureType.monster,
                BiomeGenBase.ocean, BiomeGenBase.deepOcean, BiomeGenBase.frozenOcean,
                OceanBiomeRegistry.warmOcean, OceanBiomeRegistry.deepWarmOcean,
                OceanBiomeRegistry.lukewarmOcean, OceanBiomeRegistry.deepLukewarmOcean,
                OceanBiomeRegistry.coldOcean, OceanBiomeRegistry.deepColdOcean,
                OceanBiomeRegistry.deepFrozenOcean);
        EntityRegistry.addSpawn(EntityDrowned.class, 100, 1, 1, EnumCreatureType.monster,
                BiomeGenBase.river);
        EntityRegistry.addSpawn(EntityDrowned.class, 1, 1, 1, EnumCreatureType.monster,
                BiomeGenBase.frozenRiver);
        EntityRegistry.addSpawn(EntitySeaTurtle.class, 5, 2, 5, EnumCreatureType.creature,
                BiomeGenBase.beach);
        BiomeGenBase[] oceans={BiomeGenBase.ocean,BiomeGenBase.deepOcean,OceanBiomeRegistry.warmOcean,
                OceanBiomeRegistry.deepWarmOcean,OceanBiomeRegistry.lukewarmOcean,
                OceanBiomeRegistry.deepLukewarmOcean,OceanBiomeRegistry.coldOcean,
                OceanBiomeRegistry.deepColdOcean,OceanBiomeRegistry.deepFrozenOcean};
        EntityRegistry.addSpawn(EntityAquaticFish120.Cod.class,15,3,6,EnumCreatureType.waterCreature,oceans);
        EntityRegistry.addSpawn(EntityAquaticFish120.Salmon.class,15,3,5,EnumCreatureType.waterCreature,oceans);
        EntityRegistry.addSpawn(EntityAquaticFish120.Pufferfish.class,5,1,3,EnumCreatureType.waterCreature,OceanBiomeRegistry.warmOcean,OceanBiomeRegistry.lukewarmOcean);
        EntityRegistry.addSpawn(EntityAquaticFish120.Tropical.class,20,4,8,EnumCreatureType.waterCreature,OceanBiomeRegistry.warmOcean,OceanBiomeRegistry.lukewarmOcean);
        EntityRegistry.addSpawn(EntityDolphin120.class,2,1,2,EnumCreatureType.waterCreature,oceans);
        EntityRegistry.addSpawn(EntityBountifulGuardian.class,1,2,4,EnumCreatureType.monster,
                OceanBiomeRegistry.deepWarmOcean,OceanBiomeRegistry.deepLukewarmOcean,
                OceanBiomeRegistry.deepColdOcean,OceanBiomeRegistry.deepFrozenOcean);
    }

    private static void registerAquaticEntity(Class<? extends net.minecraft.entity.Entity> type,String global,String mod,int id,int primary,int secondary){EntityRegistry.registerGlobalEntityID(type,global,EntityRegistry.findGlobalUniqueEntityId(),primary,secondary);EntityRegistry.registerModEntity(type,mod,id,Minecraft1202.instance,80,3,true);}

    private static void registerClientContent() {
        if (!FMLCommonHandler.instance().getSide().isClient()) return;
        try {
            Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Aquatic.AquaticUpdateClient")
                    .getMethod("init").invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize Update Aquatic client content", e);
        }
    }

    private interface Registration {
        void run();
    }

    private AquaticUpdate() { }
}
