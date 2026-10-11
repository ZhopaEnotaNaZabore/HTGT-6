package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded;

import galaxyspace.core.configs.GSConfigDimensions;
import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import galaxyspace.systems.SolarSystem.satellites.TeleportTypeSS;
import galaxyspace.systems.SolarSystem.satellites.venus.dimension.WorldProviderVenusSS;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Satellite;

/** Completes the Venus station which GalaxySpace 1.2.15 constructs but never registers. */
public final class VenusSpaceStationFix {
    private VenusSpaceStationFix() { }

    public static void register() {
        if (!GSConfigDimensions.enableVenusSS) return;
        Satellite station = SolarSystemBodies.venusSpaceStation;
        if (station == null) throw new IllegalStateException("GalaxySpace did not construct venusSpaceStation");

        station.setDimensionInfo(GSConfigDimensions.dimensionIDVenusOrbit,
                GSConfigDimensions.dimensionIDVenusOrbitStatic, WorldProviderVenusSS.class);
        GalacticraftRegistry.registerTeleportType(WorldProviderVenusSS.class, new TeleportTypeSS());
        GalacticraftRegistry.registerProvider(GSConfigDimensions.dimensionIDVenusOrbit,
                WorldProviderVenusSS.class, false, -1043);
        GalacticraftRegistry.registerProvider(GSConfigDimensions.dimensionIDVenusOrbitStatic,
                WorldProviderVenusSS.class, true, -1042);
        GalaxyRegistry.registerSatellite(station);
    }
}
