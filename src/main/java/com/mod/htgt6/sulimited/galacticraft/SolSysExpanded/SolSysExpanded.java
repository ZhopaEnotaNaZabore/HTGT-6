package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.charon.WorldProviderCharon;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.proteus.WorldProviderProteus;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune.WorldProviderNeptune;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune.TeleportTypeNeptune;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars.WorldProviderPhobos;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars.WorldProviderDeimos;
import galaxyspace.api.BodiesHelper;
import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.core.dimension.TeleportTypeMoon;

/** Adds gameplay dimensions to bodies already owned by GalaxySpace. */
public final class SolSysExpanded {
    public static Moon charon;
    public static Moon proteus;
    public static Planet neptune;
    public static Moon phobos;
    public static Moon deimos;

    private SolSysExpanded() { }

    public static void init(int charonDimensionId, int proteusDimensionId, int neptuneDimensionId,
                            int phobosDimensionId, int deimosDimensionId) {
        charon = SolarSystemBodies.charonPluto;
        if (charon == null) {
            throw new IllegalStateException("GalaxySpace did not register SolarSystemBodies.charonPluto");
        }
        // Expand the existing catalog entry.  Do not create/register a duplicate moon.
        charon.setDimensionInfo(charonDimensionId, WorldProviderCharon.class)
                .setTierRequired(8);
        BodiesHelper.registerBodyWithClass(charon,
                new BodiesHelper.BodiesData(BodiesHelper.iceworld, 0.077F, 0, -220.0F,
                        0.0F, 153288L, false, true));
        GalacticraftRegistry.registerTeleportType(WorldProviderCharon.class, new TeleportTypeMoon());

        proteus = SolarSystemBodies.proteusNeptune;
        if (proteus == null) {
            throw new IllegalStateException("GalaxySpace did not register SolarSystemBodies.proteusNeptune");
        }
        proteus.setDimensionInfo(proteusDimensionId, WorldProviderProteus.class).setTierRequired(7);
        BodiesHelper.registerBodyWithClass(proteus,
                new BodiesHelper.BodiesData(BodiesHelper.asteroid, 0.079F, 0, -222.0F,
                        0.0F, 26928L, false, true));
        GalacticraftRegistry.registerTeleportType(WorldProviderProteus.class, new TeleportTypeMoon());

        neptune = SolarSystemBodies.planetNeptune;
        if (neptune == null) throw new IllegalStateException("GalaxySpace did not register planetNeptune");
        neptune.setDimensionInfo(neptuneDimensionId, WorldProviderNeptune.class).setTierRequired(8);
        BodiesHelper.registerBodyWithClass(neptune,
                new BodiesHelper.BodiesData(BodiesHelper.icegiant, -0.012F, 100, -214.0F,
                        10.0F, 16110L, false, true));
        GalacticraftRegistry.registerTeleportType(WorldProviderNeptune.class, new TeleportTypeNeptune());

        phobos = requireMoon(SolarSystemBodies.phobosMars, "phobosMars");
        phobos.setDimensionInfo(phobosDimensionId, WorldProviderPhobos.class).setTierRequired(2);
        BodiesHelper.registerBodyWithClass(phobos,
                new BodiesHelper.BodiesData(BodiesHelper.asteroid, 0.058F, 0, -55.0F,
                        0.0F, 27552L, false, true));
        GalacticraftRegistry.registerTeleportType(WorldProviderPhobos.class, new TeleportTypeMoon());

        deimos = requireMoon(SolarSystemBodies.deimosMars, "deimosMars");
        deimos.setDimensionInfo(deimosDimensionId, WorldProviderDeimos.class).setTierRequired(2);
        BodiesHelper.registerBodyWithClass(deimos,
                new BodiesHelper.BodiesData(BodiesHelper.asteroid, 0.031F, 0, -65.0F,
                        0.0F, 109080L, false, true));
        GalacticraftRegistry.registerTeleportType(WorldProviderDeimos.class, new TeleportTypeMoon());
    }

    private static Moon requireMoon(Moon moon, String field) {
        if (moon == null) throw new IllegalStateException("GalaxySpace did not register SolarSystemBodies." + field);
        return moon;
    }
}
