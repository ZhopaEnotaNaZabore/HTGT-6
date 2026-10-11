package com.mod.htgt6.sulimited.galacticraft.Glise792;

import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.WorldProviderGliese;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.WorldProviderGlieseC;
import com.mod.htgt6.sulimited.galacticraft.teleporters.TeleportTypeGlieseC;
import com.mod.htgt6.sulimited.galacticraft.teleporters.TeleportTypeGlieseb;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import galaxyspace.api.BodiesHelper;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;
import net.minecraft.util.ResourceLocation;



public class sysGliese792 {

    public static SolarSystem solarGliese792;
    public static Star starGliese792;
    public static Planet planetGlieseB;
    public static Planet planetGlieseC;

    public static void init(FMLInitializationEvent event) {
        registerWorldProviders();
        registerSolarSystem();
        registerTeleports();
    }

    /**
     * Galacticraft sends planet dimension IDs to the client by provider-list
     * index.  BodiesHelper only assigns dimension info to a planet; it does
     * not register those provider types in GalacticraftRegistry.  Registering
     * both providers here keeps the server/client indices identical and lets
     * the client recreate Gliese C (-888) during the join packet.
     */
    private static void registerWorldProviders() {
        /*
         * BodiesHelper.registerPlanet() registers the provider type through
         * the Galacticraft body registration.  Registering it here as well
         * makes GalacticraftCore try to register -81/-888 a second time in
         * post-init.  Besides the noisy collision, that can leave the global
         * provider list inconsistent when AmunRa is present.
         */
    }

    private static void registerSolarSystem() {
        // GalaxySpace owns construction/metadata; the legacy GC registry is
        // retained below so existing celestial and dimension keys do not move.
        solarGliese792 = BodiesHelper.registerSolarSystem(
                "htgt6", "Gliese792_system", BodiesHelper.Galaxies.MILKYWAY,
                new Vector3(3.5F, -1.4F, 0.0F), "Gliese792_star", 1.5F);
        starGliese792 = solarGliese792.getMainStar();
        starGliese792.setBodyIcon(new ResourceLocation("htgt6", "textures/gui/celestialbodies/star_Gliese792.png"));
        // BodiesHelper uses: phase, size, distance, orbit time.  The inner
        // volcanic world visibly completes an orbit faster than oceanic C.
        planetGlieseB = BodiesHelper.registerPlanet(solarGliese792, "gliese_792_b", "htgt6",
                WorldProviderGliese.class, -81, 6, 0.75F, 0.8F, 1.4F, 4.0F);

        // Planet Icon
        planetGlieseB.setBodyIcon(new ResourceLocation("htgt6", "textures/gui/celestialbodies/planet_gliese_b.png"));

        // 4. Registry
        GalaxyRegistry.registerSolarSystem(solarGliese792);
        System.out.println("[HTGT6] Gliese 792 System and Planet 'b' registered.");


        planetGlieseC = BodiesHelper.registerPlanet(solarGliese792, "gliese_792_c", "htgt6",
                WorldProviderGlieseC.class, -888, 6, 3.85F, 1.1F, 2.2F, 9.0F);

        planetGlieseC.atmosphereComponent(IAtmosphericGas.NITROGEN).atmosphereComponent(IAtmosphericGas.OXYGEN).atmosphereComponent( IAtmosphericGas.CO2).atmosphereComponent( IAtmosphericGas.HYDROGEN);

// PLANET ICON
        planetGlieseC.setBodyIcon(
                new ResourceLocation(
                        "htgt6",
                        "textures/gui/celestialbodies/planet_gliese_c.png"
                )
        );

        registerGalaxySpaceData();
    }

    private static void registerGalaxySpaceData() {
        BodiesHelper.registerBody(planetGlieseB,
                // 65 atm intentionally exceeds GalaxySpace's 45 atm threshold.
                // Its pressure event therefore damages an unprotected player.
                new BodiesHelper.BodiesData(BodiesHelper.terra, 0.058F, 65, 620.0F, 0.0F, 24660L, false, true), true);
        BodiesHelper.registerBody(planetGlieseC,
                new BodiesHelper.BodiesData(BodiesHelper.oceanide, 0.058F, 1, 0.0F, 0.3F, 24000L, true, true), true);
        BodiesHelper.registerBodyWithClass(starGliese792,
                new BodiesHelper.BodiesData(BodiesHelper.red + " " + BodiesHelper.dwarf, 0.0F, 0, 3500.0F, 0.0F, 0L, false, false));
    }
    private static void registerTeleports () {
        GalacticraftRegistry.registerTeleportType(WorldProviderGliese.class, new TeleportTypeGlieseb());
        GalacticraftRegistry.registerTeleportType(WorldProviderGlieseC.class, new TeleportTypeGlieseC());
    }

}
