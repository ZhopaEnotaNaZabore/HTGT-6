package com.mod.htgt6.sulimited.galacticraft;

import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceStarData;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceTechExpandedRegistry;
import galaxyspace.api.BodiesHelper;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.vector.Vector3;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Real, named stars represented as non-travel map markers through GalaxySpace.
 * Coordinates are intentionally visual rather than astronomical distances.
 */
public final class CataloguedStarRegistry {
    private static final Map<String, Star> STARS = new LinkedHashMap<String, Star>();

    private CataloguedStarRegistry() {}

    public static void registerStars() {
        // UY Sct is a Milky Way M4 Ia red supergiant.  The map value uses a
        // representative 3,400 K photospheric temperature.
        register("uy_scuti", BodiesHelper.Galaxies.MILKYWAY.getName(), 18.0F, 6.0F,
                "sulimited.star.type.red_supergiant", 3400.0D,
                BodiesHelper.red + " " + BodiesHelper.giant);
        // PSR B0531+21 is the Crab Pulsar: an isolated neutron star in the
        // Milky Way, rotating every ~33 ms.  2.1 MK is the observed surface
        // temperature upper limit used for the map classification.
        register("crab_pulsar", BodiesHelper.Galaxies.MILKYWAY.getName(), -16.0F, 7.5F,
                "sulimited.star.type.pulsar", 2100000.0D,
                BodiesHelper.white + " " + BodiesHelper.dwarf);

        // M31 luminous blue variables: AE And ~21,000 K in quiescence and
        // AF And 33,000 +/- 3,000 K at visual minimum.
        register("ae_andromedae", "andromeda", -8.5F, 5.5F,
                "sulimited.star.type.luminous_blue_variable", 21000.0D,
                BodiesHelper.lightblue + " " + BodiesHelper.giant);
        register("af_andromedae", "andromeda", 7.0F, -6.0F,
                "sulimited.star.type.luminous_blue_variable", 33000.0D,
                BodiesHelper.lightblue + " " + BodiesHelper.giant);

        // M33: GR 290 (Romano's Star), Var B and B324 are observed members
        // of the Triangulum galaxy.  Temperatures represent published states.
        register("romanos_star", "Triangle", -8.0F, -5.5F,
                "sulimited.star.type.wolf_rayet_lbv", 28500.0D,
                BodiesHelper.lightblue + " " + BodiesHelper.giant);
        register("m33_var_b", "Triangle", 6.5F, 6.0F,
                "sulimited.star.type.luminous_blue_variable", 20000.0D,
                BodiesHelper.lightblue + " " + BodiesHelper.giant);
        register("m33_b324", "Triangle", 9.5F, -3.5F,
                "sulimited.star.type.yellow_hypergiant", 6970.0D,
                BodiesHelper.yellow + " " + BodiesHelper.giant);
    }

    public static Star getStar(String id) {
        return STARS.get(id);
    }

    private static void register(String id, String galaxyName, float mapX, float mapY,
                                 String typeKey, double temperatureKelvin, String bodyClass) {
        String systemName = "htgt6_catalogued_" + id;
        if (GalaxyRegistry.getRegisteredSolarSystems().containsKey(systemName)) return;

        SolarSystem system = BodiesHelper.registerSolarSystem("htgt6", systemName, galaxyName,
                new Vector3(mapX, mapY, 0.0D), id, 1.25F);
        Star star = system.getMainStar();
        star.setTierRequired(Integer.MAX_VALUE);
        star.setUnreachable();
        GalaxyRegistry.registerSolarSystem(system);
        BodiesHelper.registerBodyWithClass(star,
                new BodiesHelper.BodiesData(bodyClass, 0.0F, 0, (float) temperatureKelvin,
                        0.0F, 0L, false, false));
        SpaceTechExpandedRegistry.registerStarData(new SpaceStarData(id, typeKey, temperatureKelvin));
        STARS.put(id, star);
    }
}
