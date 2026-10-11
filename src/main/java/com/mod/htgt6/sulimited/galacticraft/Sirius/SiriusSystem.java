package com.mod.htgt6.sulimited.galacticraft.Sirius;

import com.mod.htgt6.api.hitech6.SpaceTechExpanded.BinaryStarSystemData;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceStarData;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceTechExpandedRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import galaxyspace.api.BodiesHelper;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.vector.Vector3;

/**
 * Galacticraft supports one native main Star per system.  SpaceTechExpanded
 * supplies the binary-pair semantics, while an unreachable body is only the
 * renderer anchor for Sirius B and can never be selected as a planet.
 */
public final class SiriusSystem {
    public static SolarSystem solarSystemSirius;
    public static Star starSiriusA;
    public static Planet planetSiriusB;

    private SiriusSystem() {}

    public static void init(FMLInitializationEvent event) {
        solarSystemSirius = BodiesHelper.registerSolarSystem(
                "htgt6", "sirius_system", BodiesHelper.Galaxies.MILKYWAY,
                new Vector3(14.5F, -1.2F, 0.0F), "sirius_a", 1.5F);
        starSiriusA = solarSystemSirius.getMainStar();
        GalaxyRegistry.registerSolarSystem(solarSystemSirius);
        SpaceTechExpandedRegistry.registerBinaryStarSystem(new BinaryStarSystemData(
                "sirius_system", "sirius_a", "sirius_b", 20.0F, 50.1F, 25.4F, 0.056F));
        SpaceTechExpandedRegistry.registerStarData(new SpaceStarData(
                "sirius_a", "sulimited.star.type.main_sequence", 9940.0D));
        SpaceTechExpandedRegistry.registerStarData(new SpaceStarData(
                "sirius_b", "sulimited.star.type.white_dwarf", 25200.0D));

        BodiesHelper.registerBodyWithClass(starSiriusA,
                new BodiesHelper.BodiesData(BodiesHelper.white + " " + BodiesHelper.dwarf,
                        0.0F, 0, 25000.0F, 0.0F, 0L, false, false));

        planetSiriusB = new Planet("sirius_b");
        planetSiriusB.setParentSolarSystem(solarSystemSirius);
        planetSiriusB.setRelativeSize(0.28F);
        planetSiriusB.setRelativeDistanceFromCenter(new CelestialBody.ScalableDistance(0.42F, 0.42F));
        planetSiriusB.setPhaseShift(0.15F);
        // 50.1 years is Sirius A/B's real orbital period; this is a scaled
        // visual period used by the GalaxySpace map rather than a destination.
        planetSiriusB.setRelativeOrbitTime(50.1F);
        planetSiriusB.setTierRequired(Integer.MAX_VALUE);
        planetSiriusB.setUnreachable();
        GalaxyRegistry.registerPlanet(planetSiriusB);
        BodiesHelper.registerBodyWithClass(planetSiriusB,
                new BodiesHelper.BodiesData(BodiesHelper.white + " " + BodiesHelper.dwarf,
                        0.0F, 0, 25000.0F, 0.0F, 0L, false, false));
        System.out.println("[SU-Limited] GalaxySpace binary Sirius A/B system registered.");
    }
}
