package com.mod.htgt6.sulimited.galacticraft;

import com.mod.htgt6.api.hitech6.SpaceTechExpanded.BinaryStarSystemData;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceHabitableZoneData;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceStarData;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceTechExpandedRegistry;
import galaxyspace.api.BodiesHelper;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;

/**
 * Supplies GalaxySpace metadata for AmunRa after its original registry code
 * has finished.  The original body and dimension registrations remain in
 * place, so existing level.dat and dimension IDs are never remapped.
 */
public final class AmunRaGalaxySpaceBridge {
    private static final String RA_SYSTEM = "systemAmunRa";
    private static final String RA_STAR = "starRa";
    private static final String AMON_STAR = "starAmun";
    private static boolean registered;

    private AmunRaGalaxySpaceBridge() {}

    public static synchronized void register() {
        if (registered) return;
        SolarSystem system = findRaSystem();
        if (system == null || !"galaxy.Micro".equalsIgnoreCase(system.getUnlocalizedParentGalaxyName())) {
            return;
        }

        int travelTier = findConfiguredTravelTier(system);
        registerSpaceTechData(travelTier);

        // Keep AmunRa's native instances and dimension IDs.  registerBodyWithClass
        // only attaches GalaxySpace data; registerBody(..., true) would attempt
        // to insert every already-registered planet and moon a second time.
        if (system.getMainStar() != null) {
            BodiesHelper.registerBodyWithClass(system.getMainStar(), new BodiesHelper.BodiesData(
                    BodiesHelper.red + " " + BodiesHelper.giant,
                    0.0F, 0, 4800.0F, 0.0F, 0L, false, false));
        }
        Star companion = SpaceTechExpandedRegistry.getSecondaryStar(AMON_STAR);
        if (companion != null) {
            BodiesHelper.registerBodyWithClass(companion, new BodiesHelper.BodiesData(
                    BodiesHelper.lightblue + " " + BodiesHelper.giant,
                    0.0F, 0, 12000.0F, 0.0F, 0L, false, false));
        }

        for (Planet planet : GalaxyRegistry.getPlanetsForSolarSystem(system)) {
            normalizeTravelTier(planet, travelTier);
            BodiesHelper.registerBodyWithClass(planet, planetData(planet));
            for (Moon moon : GalaxyRegistry.getMoonsForPlanet(planet)) {
                normalizeTravelTier(moon, travelTier);
                BodiesHelper.registerBodyWithClass(moon, moonData(moon));
            }
        }
        registered = true;
        System.out.println("[SU-Limited] Registered the AmunRa Ra system in GalaxySpace Micro Galaxy.");
    }

    private static void registerSpaceTechData(int travelTier) {
        SpaceTechExpandedRegistry.registerBinaryStarSystem(new BinaryStarSystemData(
                RA_SYSTEM, RA_STAR, AMON_STAR, 20.0F, 0.9F, 0.8F, 1.2F));
        SpaceTechExpandedRegistry.registerStarData(new SpaceStarData(
                RA_STAR, "sulimited.star.type.red_giant", 4800.0D));
        SpaceTechExpandedRegistry.registerStarData(new SpaceStarData(
                AMON_STAR, "sulimited.star.type.blue_giant", 12000.0D));
        // Neper is the breathable world of the system and orbits Baal.  Baal's
        // system orbit is therefore the stable visual reference for the zone.
        SpaceTechExpandedRegistry.registerHabitableZone(new SpaceHabitableZoneData(
                RA_SYSTEM, "baal", 0.90F, 1.10F));
        SpaceTechExpandedRegistry.registerSystemTravelTier(RA_SYSTEM, travelTier);
    }

    private static int findConfiguredTravelTier(SolarSystem system) {
        for (Planet planet : GalaxyRegistry.getPlanetsForSolarSystem(system)) {
            if (planet.getReachable() && planet.getTierRequirement() > 0) {
                return planet.getTierRequirement();
            }
            for (Moon moon : GalaxyRegistry.getMoonsForPlanet(planet)) {
                if (moon.getReachable() && moon.getTierRequirement() > 0) {
                    return moon.getTierRequirement();
                }
            }
        }
        return 3;
    }

    private static void normalizeTravelTier(CelestialBody body, int requiredTier) {
        String name = body.getName().toLowerCase(java.util.Locale.ROOT);
        boolean destination = name.equals("horus") || name.equals("neper")
                || name.equals("asteroidbeltmehen") || name.equals("maahes")
                || name.equals("seth") || name.equals("anubis");
        if (destination) {
            body.setTierRequired(requiredTier);
        } else {
            body.setTierRequired(Integer.MAX_VALUE);
            body.setUnreachable();
        }
    }

    /** GalaxyRegistry normalizes some keys, so do not rely on their case. */
    private static SolarSystem findRaSystem() {
        for (SolarSystem candidate : GalaxyRegistry.getRegisteredSolarSystems().values()) {
            if (RA_SYSTEM.equalsIgnoreCase(candidate.getName())) return candidate;
        }
        return null;
    }

    private static BodiesHelper.BodiesData planetData(Planet planet) {
        String name = planet.getName().toLowerCase(java.util.Locale.ROOT);
        if (name.contains("baal") || name.contains("sekhmet")) return data(BodiesHelper.gasgiant, 0, 0.0F, false);
        if (name.contains("mehen")) return data(BodiesHelper.asteroid, 0, 0.0F, false);
        if (name.contains("horus")) return data(BodiesHelper.desert, 0, 410.0F, false);
        if (name.contains("anubis")) return data(BodiesHelper.selena, 0, 0.0F, false);
        return data(BodiesHelper.terra, 1, 288.0F, false);
    }

    private static BodiesHelper.BodiesData moonData(Moon moon) {
        String name = moon.getName().toLowerCase(java.util.Locale.ROOT);
        if (name.contains("seth")) return data(BodiesHelper.iceworld, 0, 180.0F, false);
        if (name.contains("neper")) return data(BodiesHelper.terra, 1, 288.0F, true);
        if (name.contains("maahes")) return data(BodiesHelper.terra, 3, 310.0F, false);
        return data(BodiesHelper.selena, 0, 0.0F, false);
    }

    private static BodiesHelper.BodiesData data(String type, int pressure, float temperature, boolean breathable) {
        return new BodiesHelper.BodiesData(type, 0.058F, pressure, temperature, 0.0F, 24000L,
                breathable, false);
    }
}
