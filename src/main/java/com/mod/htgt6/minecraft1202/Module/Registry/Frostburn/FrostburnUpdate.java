package com.mod.htgt6.minecraft1202.Module.Registry.Frostburn;


import com.mod.htgt6.minecraft1202.Minecraft1202;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnHusk;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnPolarBear;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnStray;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnStrayArrow;
import com.mod.htgt6.minecraft1202.Module.World.Frostburn.FrostburnFossilGenerator;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/** Lifecycle entry point for the Minecraft Java 1.10 Frostburn Update backport. */
public final class FrostburnUpdate {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-FrostburnUpdate");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        FrostburnContent.register();
        com.mod.htgt6.minecraft1202.Module.Network.Frostburn.StructureNetwork120.init();
        registerEntities();
        GameRegistry.registerWorldGenerator(new FrostburnFossilGenerator(), 18);
        GameRegistry.registerWorldGenerator(new com.mod.htgt6.minecraft1202.Module.World.Frostburn.FrostburnMagmaGenerator120(), 18);
        registerClientContent();
        LOGGER.info("Minecraft Java 1.10 - Frostburn Update module initialized");
    }

    private static void registerEntities() {
        int huskId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityFrostburnHusk.class, "HTGT6.Husk", huskId,
                0x797061, 0xE6CC94);
        EntityRegistry.registerModEntity(EntityFrostburnHusk.class, "Husk", 4, Minecraft1202.instance,
                64, 3, true);

        int strayId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityFrostburnStray.class, "HTGT6.Stray", strayId,
                0x617677, 0xDDEAEA);
        EntityRegistry.registerModEntity(EntityFrostburnStray.class, "Stray", 5, Minecraft1202.instance,
                64, 3, true);
        EntityRegistry.registerModEntity(EntityFrostburnStrayArrow.class, "StrayArrow", 6,
                Minecraft1202.instance, 96, 1, true);

        int polarBearId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityFrostburnPolarBear.class, "HTGT6.PolarBear",
                polarBearId, 0xF2F2F2, 0x959590);
        EntityRegistry.registerModEntity(EntityFrostburnPolarBear.class, "PolarBear", 7,
                Minecraft1202.instance, 80, 3, true);

        splitMonsterSpawn(BiomeGenBase.desert, EntityZombie.class, EntityFrostburnHusk.class);
        splitMonsterSpawn(BiomeGenBase.desertHills, EntityZombie.class, EntityFrostburnHusk.class);
        splitMonsterSpawn(BiomeGenBase.icePlains, EntitySkeleton.class, EntityFrostburnStray.class);
        splitMonsterSpawn(BiomeGenBase.iceMountains, EntitySkeleton.class, EntityFrostburnStray.class);
        splitMonsterSpawn(BiomeGenBase.frozenOcean, EntitySkeleton.class, EntityFrostburnStray.class);
        splitMonsterSpawn(BiomeGenBase.frozenRiver, EntitySkeleton.class, EntityFrostburnStray.class);
        EntityRegistry.addSpawn(EntityFrostburnPolarBear.class, 1, 1, 2, EnumCreatureType.creature,
                BiomeGenBase.icePlains, BiomeGenBase.iceMountains, BiomeGenBase.frozenOcean);
    }

    /** Keeps the vanilla family weight stable while making the 1.10 variant 80% of that family. */
    @SuppressWarnings("unchecked")
    private static void splitMonsterSpawn(BiomeGenBase biome, Class<?> vanillaType, Class<?> frostburnType) {
        if (biome == null) return;
        List<BiomeGenBase.SpawnListEntry> entries = biome.getSpawnableList(EnumCreatureType.monster);
        for (BiomeGenBase.SpawnListEntry entry : entries) {
            if (entry.entityClass != vanillaType) continue;
            int familyWeight = entry.itemWeight;
            entry.itemWeight = Math.max(1, familyWeight / 5);
            entries.add(new BiomeGenBase.SpawnListEntry(frostburnType,
                    Math.max(1, familyWeight - entry.itemWeight), entry.minGroupCount, entry.maxGroupCount));
            return;
        }
    }

    private static void registerClientContent() {
        if (!FMLCommonHandler.instance().getSide().isClient()) return;
        try {
            Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Frostburn.FrostburnUpdateClient").getMethod("init").invoke(null);
        } catch (Exception error) {
            throw new IllegalStateException("Unable to initialize Minecraft Java 1.10 client content", error);
        }
    }

    private FrostburnUpdate() { }
}
