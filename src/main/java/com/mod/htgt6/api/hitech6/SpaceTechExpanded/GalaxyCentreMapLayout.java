package com.mod.htgt6.api.hitech6.SpaceTechExpanded;

import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import com.mod.htgt6.sulimited.galacticraft.GalaxyCentreStarRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Keeps visual Galacticraft systems clear of SpaceTech galaxy centres.
 * Map positions are presentation-only; this never changes dimensions, IDs,
 * planets or any persisted world data.
 */
public final class GalaxyCentreMapLayout {
    private static final double CENTRE_CLEAR_RADIUS = 1.25D;
    private static final double RELOCATED_RADIUS = 6.0D;

    private GalaxyCentreMapLayout() {}

    /** Moves only systems that would otherwise overlap a registered centre. */
    public static synchronized int reserveCentres() {
        SpaceTechExpandedRegistry.bootstrapDefaults();
        List<SolarSystem> systems = new ArrayList<SolarSystem>(GalaxyRegistry.getRegisteredSolarSystems().values());
        Collections.sort(systems, new Comparator<SolarSystem>() {
            @Override
            public int compare(SolarSystem first, SolarSystem second) {
                return first.getName().compareTo(second.getName());
            }
        });

        int relocated = 0;
        int slot = 0;
        for (SolarSystem system : systems) {
            if (GalaxyCentreStarRegistry.isGalaxyCentreSystem(system)) continue;
            GalaxyCentre centre = SpaceTechExpandedRegistry.getGalaxyCentre(system.getUnlocalizedParentGalaxyName());
            Vector3 position = system.getMapPosition();
            if (centre == null || position == null || !overlapsCentre(position, centre)) continue;

            // Deterministic positions make client/server maps agree.  A wide
            // radius leaves the centre and its HUD visually unobstructed.
            double angle = Math.PI + slot * (Math.PI * 2.0D / 7.0D);
            system.setMapPosition(new Vector3(
                    centre.mapX + Math.cos(angle) * RELOCATED_RADIUS,
                    centre.mapY + Math.sin(angle) * RELOCATED_RADIUS,
                    position.z));
            slot++;
            relocated++;
        }
        return relocated;
    }

    private static boolean overlapsCentre(Vector3 position, GalaxyCentre centre) {
        double dx = position.x - centre.mapX;
        double dy = position.y - centre.mapY;
        return dx * dx + dy * dy < CENTRE_CLEAR_RADIUS * CENTRE_CLEAR_RADIUS;
    }
}
