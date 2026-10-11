package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;

import galaxyspace.api.BodiesHelper;
import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.dimension.TeleportTypeMoon;
import net.minecraft.block.Block;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;

/** Completes GalaxySpace's remaining outer-Sol catalogue entries. */
public final class MinorWorlds {
    public static Moon ariel;
    public static Planet haumea, makemake, eris, deeDee;
    public static Definition ARIEL, HAUMEA, MAKEMAKE, ERIS, DEEDEE;

    private MinorWorlds() { }

    public static void init(int arielId, int haumeaId, int makemakeId, int erisId, int deeDeeId) {
        ariel = require(SolarSystemBodies.arielUranus, "arielUranus");
        ariel.setDimensionInfo(arielId, WorldProviderAriel.class).setTierRequired(6);
        register(ariel, WorldProviderAriel.class, BodiesHelper.iceworld, 0.027F, -213.0F, 217728L, false);

        haumea = dwarf("haumea", WorldProviderHaumea.class, haumeaId, 8, 0.23F, 0.36F, 11.0F, 283.0F);
        makemake = dwarf("makemake", WorldProviderMakemake.class, makemakeId, 8, 1.37F, 0.31F, 12.0F, 306.0F);
        eris = dwarf("eris", WorldProviderEris.class, erisId, 9, 2.41F, 0.34F, 13.4F, 559.0F);
        deeDee = dwarf("deedee", WorldProviderDeeDee.class, deeDeeId, 9, 3.52F, 0.29F, 14.7F, 700.0F);
        SolarSystemBodies.planetHaumea = haumea;
        SolarSystemBodies.planetMakemake = makemake;
        SolarSystemBodies.planetEris = eris;
        SolarSystemBodies.planetDeeDee = deeDee;

        register(haumea, WorldProviderHaumea.class, BodiesHelper.iceworld, 0.044F, -241.0F, 15600L, true);
        register(makemake, WorldProviderMakemake.class, BodiesHelper.iceworld, 0.043F, -239.0F, 27900L, true);
        register(eris, WorldProviderEris.class, BodiesHelper.iceworld, 0.045F, -243.0F, 93240L, true);
        register(deeDee, WorldProviderDeeDee.class, BodiesHelper.iceworld, 0.039F, -245.0F, 111600L, true);

        ARIEL = new Definition("Ariel", arielId, ariel, BiomeGenSpaceUltimate.ariel,
                MinorWorldBlocks.arielSurface, MinorWorldBlocks.arielCrust, MinorWorldBlocks.arielCore,
                0.027F, -5.3F, 217728L, 6, 0.08D, 0);
        HAUMEA = new Definition("Haumea", haumeaId, haumea, BiomeGenSpaceUltimate.haumea,
                MinorWorldBlocks.haumeaSurface, MinorWorldBlocks.haumeaCrust, MinorWorldBlocks.haumeaCore,
                0.044F, -6.2F, 15600L, 8, 0.025D, 1);
        MAKEMAKE = new Definition("Makemake", makemakeId, makemake, BiomeGenSpaceUltimate.makemake,
                MinorWorldBlocks.makemakeSurface, MinorWorldBlocks.makemakeCrust, MinorWorldBlocks.makemakeCore,
                0.043F, -6.1F, 27900L, 8, 0.021D, 2);
        ERIS = new Definition("Eris", erisId, eris, BiomeGenSpaceUltimate.eris,
                MinorWorldBlocks.erisSurface, MinorWorldBlocks.erisCrust, MinorWorldBlocks.erisCore,
                0.045F, -6.4F, 93240L, 9, 0.016D, 3);
        DEEDEE = new Definition("DeeDee", deeDeeId, deeDee, BiomeGenSpaceUltimate.deeDee,
                MinorWorldBlocks.deeDeeSurface, MinorWorldBlocks.deeDeeCrust, MinorWorldBlocks.deeDeeCore,
                0.039F, -6.6F, 111600L, 9, 0.012D, 4);
    }

    private static Planet dwarf(String name, Class<? extends WorldProvider> provider, int dimensionId,
                                int tier, float phase, float size, float distance, float orbit) {
        return BodiesHelper.registerPlanet(GalacticraftCore.solarSystemSol, name, "htgt6", provider,
                dimensionId, tier, phase, size, distance, orbit);
    }

    private static void register(CelestialBody body, Class<? extends WorldProvider> provider,
                                 String type, float gravity, float temperature, long day, boolean newBody) {
        BodiesHelper.BodiesData data = new BodiesHelper.BodiesData(type, gravity, 0, temperature,
                0.0F, day, false, true);
        boolean missingMoonRegistration = body instanceof Moon
                && !GalaxyRegistry.getRegisteredMoons().containsValue(body);
        if (newBody || missingMoonRegistration) BodiesHelper.registerBody(body, data, true);
        else BodiesHelper.registerBodyWithClass(body, data);
        GalacticraftRegistry.registerTeleportType(provider, new TeleportTypeMoon());
    }

    private static Moon require(Moon moon, String field) {
        if (moon == null) throw new IllegalStateException("GalaxySpace did not register SolarSystemBodies." + field);
        return moon;
    }

    public static Definition forDimension(int dimensionId) {
        Definition[] all = {ARIEL, HAUMEA, MAKEMAKE, ERIS, DEEDEE};
        for (Definition definition : all) if (definition != null && definition.dimensionId == dimensionId) return definition;
        throw new IllegalStateException("No outer-Sol definition for dimension " + dimensionId);
    }

    public static final class Definition {
        public final String name;
        public final int dimensionId;
        public final CelestialBody body;
        public final BiomeGenBase biome;
        public final Block surface, crust, core;
        public final float gravity, thermal;
        public final long dayLength;
        public final int tier, terrainStyle;
        public final double solarMultiplier;

        Definition(String name, int dimensionId, CelestialBody body, BiomeGenBase biome,
                   Block surface, Block crust, Block core, float gravity, float thermal,
                   long dayLength, int tier, double solarMultiplier, int terrainStyle) {
            this.name = name; this.dimensionId = dimensionId; this.body = body; this.biome = biome;
            this.surface = surface; this.crust = crust; this.core = core; this.gravity = gravity;
            this.thermal = thermal; this.dayLength = dayLength; this.tier = tier;
            this.solarMultiplier = solarMultiplier; this.terrainStyle = terrainStyle;
        }
    }
}
