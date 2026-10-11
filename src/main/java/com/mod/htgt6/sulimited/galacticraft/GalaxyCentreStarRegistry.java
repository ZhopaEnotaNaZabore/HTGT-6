package com.mod.htgt6.sulimited.galacticraft;

import com.mod.htgt6.api.hitech6.SpaceTechExpanded.GalaxyCentre;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceMapObject;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceTechExpandedRegistry;
import galaxyspace.api.BodiesHelper;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import net.minecraft.util.ResourceLocation;

/** Registers a non-travel Galacticraft star at each spiral galaxy centre. */
public final class GalaxyCentreStarRegistry {
    private static final String SYSTEM_PREFIX = "htgt6_galaxy_centre_";

    private GalaxyCentreStarRegistry() {}

    public static void registerCentres() {
        SpaceTechExpandedRegistry.bootstrapDefaults();
        for (SpaceMapObject object : SpaceTechExpandedRegistry.getObjects()) {
            GalaxyCentre centre = SpaceTechExpandedRegistry.getGalaxyCentre(object.galaxyKey);
            if (centre == null || !centre.isPresent()) continue;

            String systemName = SYSTEM_PREFIX + object.id;
            if (GalaxyRegistry.getRegisteredSolarSystems().containsKey(systemName)) continue;

            String galaxyName = object.galaxyKey;
            if (galaxyName != null && galaxyName.startsWith("galaxy.")) {
                galaxyName = galaxyName.substring("galaxy.".length());
            }

            // GalaxySpace owns the registration and map integration.  The
            // star is only a visual anchor: it has no dimension or destination.
            SolarSystem system = BodiesHelper.registerSolarSystem(
                    "htgt6", systemName, galaxyName,
                    new Vector3(centre.mapX, centre.mapY, 0.0D),
                    "black_hole_" + object.id, 1.35F);
            Star blackHole = new AnimatedBlackHoleStar("black_hole_" + object.id);
            blackHole.setParentSolarSystem(system);
            system.setMainStar(blackHole);
            blackHole.setTierRequired(Integer.MAX_VALUE);
            blackHole.setUnreachable();
            GalaxyRegistry.registerSolarSystem(system);
            BodiesHelper.registerBodyWithClass(blackHole,
                    new BodiesHelper.BodiesData(BodiesHelper.brown + " " + BodiesHelper.dwarf,
                            0.0F, 0, 0.0F, 0.0F, 0L, false, false));
        }
    }

    public static boolean isGalaxyCentreSystem(SolarSystem system) {
        return system != null && system.getName().startsWith(SYSTEM_PREFIX);
    }

    public static boolean isBlackHole(Star star) {
        return star != null && star.getName().startsWith("black_hole_");
    }

    /** Uses independent texture frames because standalone star textures do not honour .mcmeta animations. */
    private static final class AnimatedBlackHoleStar extends Star {
        private static final ResourceLocation[] FRAMES = {
                new ResourceLocation("htgt6", "textures/sulimited/celestialbodies/black_hole_0.png"),
                new ResourceLocation("htgt6", "textures/sulimited/celestialbodies/black_hole_1.png"),
                new ResourceLocation("htgt6", "textures/sulimited/celestialbodies/black_hole_2.png"),
                new ResourceLocation("htgt6", "textures/sulimited/celestialbodies/black_hole_3.png")
        };

        private AnimatedBlackHoleStar(String name) {
            super(name);
        }

        @Override
        public ResourceLocation getBodyIcon() {
            return FRAMES[(int) ((System.currentTimeMillis() / 120L) % FRAMES.length)];
        }
    }
}
