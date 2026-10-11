package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.asm.HTGT6Transformer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Owns the real extended-ID ocean biomes and their Overworld generation hook. */
public final class OceanBiomeRegistry {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-OceanBiomes");
    public static BiomeGenBase warmOcean;
    public static BiomeGenBase deepWarmOcean;
    public static BiomeGenBase lukewarmOcean;
    public static BiomeGenBase deepLukewarmOcean;
    public static BiomeGenBase coldOcean;
    public static BiomeGenBase deepColdOcean;
    public static BiomeGenBase deepFrozenOcean;

    public static void register() {
        if (!HTGT6Config.ENABLE_EXTENDED_BIOME_ASM) return;

        int[] ids = {HTGT6Config.WARM_OCEAN_BIOME_ID, HTGT6Config.DEEP_WARM_OCEAN_BIOME_ID,
                HTGT6Config.LUKEWARM_OCEAN_BIOME_ID, HTGT6Config.DEEP_LUKEWARM_OCEAN_BIOME_ID,
                HTGT6Config.COLD_OCEAN_BIOME_ID, HTGT6Config.DEEP_COLD_OCEAN_BIOME_ID,
                HTGT6Config.DEEP_FROZEN_OCEAN_BIOME_ID};
        String[] names = {"Warm Ocean", "Deep Warm Ocean", "Lukewarm Ocean",
                "Deep Lukewarm Ocean", "Cold Ocean", "Deep Cold Ocean", "Deep Frozen Ocean"};
        for (int i = 0; i < ids.length; i++) validateFreeId(ids[i], names[i]);

        // Backport the modern water palette. GT6 BlockOcean reads the biome
        // multiplier, so this colours both vanilla and GT sea water without
        // replacing blocks or scheduling liquid updates.
        BiomeGenBase.ocean.waterColorMultiplier = 0x3F76E4;
        BiomeGenBase.deepOcean.waterColorMultiplier = 0x3F76E4;
        BiomeGenBase.frozenOcean.waterColorMultiplier = 0x3938C9;

        warmOcean = new BiomeGenWarmOcean(ids[0], false);
        deepWarmOcean = new BiomeGenWarmOcean(ids[1], true);
        lukewarmOcean = new BiomeGenModernOcean(ids[2], names[2], 0.5F, false, 0x45ADF2, true);
        deepLukewarmOcean = new BiomeGenModernOcean(ids[3], names[3], 0.5F, true, 0x45ADF2, true);
        // warm/lukewarm/cold ocean JSONs all use climate temperature 0.5 in
        // 1.20.2; their names come from the MultiNoise ocean-temperature axis.
        coldOcean = new BiomeGenModernOcean(ids[4], names[4], 0.5F, false, 0x3D57D6, false);
        deepColdOcean = new BiomeGenModernOcean(ids[5], names[5], 0.5F, true, 0x3D57D6, false);
        // 1.20.2 data/minecraft/worldgen/biome/deep_frozen_ocean.json uses
        // climate temperature 0.5. Freezing is selected by the climate source,
        // not by changing the biome's declared temperature to zero.
        deepFrozenOcean = new BiomeGenModernOcean(ids[6], names[6], 0.5F, true, 0x3938C9, false);
        cpw.mods.fml.common.registry.EntityRegistry.addSpawn(
                com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnPolarBear.class,
                1,1,2,net.minecraft.entity.EnumCreatureType.creature,deepFrozenOcean);

        BiomeDictionary.registerBiomeType(warmOcean,
                BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER,
                BiomeDictionary.Type.WET, BiomeDictionary.Type.HOT,
                BiomeDictionary.Type.SANDY);
        BiomeDictionary.registerBiomeType(deepWarmOcean,
                BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.WATER,
                BiomeDictionary.Type.WET, BiomeDictionary.Type.HOT,
                BiomeDictionary.Type.SANDY);
        registerOceanTypes(lukewarmOcean, false, false, true);
        registerOceanTypes(deepLukewarmOcean, false, false, true);
        registerOceanTypes(coldOcean, true, false, false);
        registerOceanTypes(deepColdOcean, true, false, false);
        registerOceanTypes(deepFrozenOcean, true, true, false);

        MinecraftForge.TERRAIN_GEN_BUS.register(new OceanBiomeLayerHandler());
        LOGGER.info("Registered complete 1.13 ocean family with extended IDs {}-{}",
                ids[0], ids[ids.length - 1]);
    }

    private static void registerOceanTypes(BiomeGenBase biome, boolean cold, boolean snowy,
                                           boolean sandy) {
        BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.OCEAN,
                BiomeDictionary.Type.WATER, BiomeDictionary.Type.WET);
        if (cold) BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.COLD);
        if (snowy) BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.SNOWY);
        if (sandy) BiomeDictionary.registerBiomeType(biome, BiomeDictionary.Type.SANDY);
    }

    public static boolean usesWarmRuins(BiomeGenBase biome) {
        return biome == warmOcean || biome == deepWarmOcean
                || biome == lukewarmOcean || biome == deepLukewarmOcean;
    }

    public static boolean isWarm(BiomeGenBase biome) {
        return biome == warmOcean || biome == deepWarmOcean;
    }

    public static boolean isLukewarm(BiomeGenBase biome) {
        return biome == lukewarmOcean || biome == deepLukewarmOcean;
    }

    private static void validateFreeId(int id, String name) {
        if (id < 300 || id > 600) {
            throw new IllegalStateException(name + " biome ID " + id + " is outside configured range 300-600");
        }
        BiomeGenBase occupied = HTGT6Transformer.getRegisteredBiomeExact(id);
        if (occupied != null) {
            throw new IllegalStateException(name + " biome ID " + id + " is already occupied by "
                    + occupied.biomeName);
        }
    }

    private OceanBiomeRegistry() {}
}
