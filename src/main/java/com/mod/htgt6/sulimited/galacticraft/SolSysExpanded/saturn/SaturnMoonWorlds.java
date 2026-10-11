package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import galaxyspace.api.BodiesHelper;
import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.core.dimension.TeleportTypeMoon;
import net.minecraft.world.WorldProvider;

/** Adds playable dimensions to GalaxySpace's existing Saturn moon entries. */
public final class SaturnMoonWorlds {
    public static Moon mimas, tethys, dione, rhea, iapetus;
    private SaturnMoonWorlds() { }

    public static void init(int mimasId, int tethysId, int dioneId, int rheaId, int iapetusId) {
        mimas = require(SolarSystemBodies.mimasSaturn, "mimasSaturn");
        attach(mimas, mimasId, WorldProviderMimas.class, 5, -196.0F, 81389L);
        tethys = require(SolarSystemBodies.tethysSaturn, "tethysSaturn");
        attach(tethys, tethysId, WorldProviderTethys.class, 5, -187.0F, 163106L);
        dione = require(SolarSystemBodies.dioneSaturn, "dioneSaturn");
        attach(dione, dioneId, WorldProviderDione.class, 6, -186.0F, 236469L);
        rhea = require(SolarSystemBodies.rheyaSaturn, "rheyaSaturn");
        attach(rhea, rheaId, WorldProviderRhea.class, 6, -174.0F, 390535L);
        iapetus = require(SolarSystemBodies.iapetusSaturn, "iapetusSaturn");
        attach(iapetus, iapetusId, WorldProviderIapetus.class, 7, -167.0F, 685337L);
    }

    private static Moon require(Moon moon, String field) {
        if (moon == null) throw new IllegalStateException("GalaxySpace did not register SolarSystemBodies." + field);
        return moon;
    }

    private static void attach(Moon moon, int dimensionId,
            Class<? extends WorldProvider> provider,
            int tier, float temperature, long dayLength) {
        moon.setDimensionInfo(dimensionId, provider).setTierRequired(tier);
        BodiesHelper.registerBodyWithClass(moon, new BodiesHelper.BodiesData(
                BodiesHelper.iceworld, 0.02F, 0, temperature, 0.0F, dayLength, false, true));
        GalacticraftRegistry.registerTeleportType(provider, new TeleportTypeMoon());
    }
}
