package com.mod.htgt6.common.GalaxyServer.Glise792;

import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.WorldProviderGliese;
import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.WorldProviderGlieseC;
import com.mod.htgt6.common.GalaxyServer.teleporters.TeleportTypeGlieseC;
import com.mod.htgt6.common.GalaxyServer.teleporters.TeleportTypeGlieseb;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;
import net.minecraft.util.ResourceLocation;



public class sysGliese792 {

    public static SolarSystem solarGliese792;
    public static Star starGliese792;
    public static Planet planetGlieseB;
    public static Planet planetGlieseC;

    public static void init(FMLInitializationEvent event) {
        registerSolarSystem();
        registerTeleports();
    }

    private static void registerSolarSystem() {
        // 1. Initialize the Solar System
        solarGliese792 = new SolarSystem("Gliese792_system", "milkyWay");
        solarGliese792.setMapPosition(new Vector3(3.5F, -1.4F, 0.0F));

        // 2. Define the Star
        starGliese792 = new Star("Gliese792_star");
        starGliese792.setParentSolarSystem(solarGliese792);
        starGliese792.setRelativeSize(1.5F);
        starGliese792.setBodyIcon(new ResourceLocation("htgt6", "textures/gui/celestialbodies/star_Gliese792.png"));
        solarGliese792.setMainStar(starGliese792);

        // 3. Define the Planet
        planetGlieseB = new Planet("gliese_792_b"); // Internal Name
        planetGlieseB.setParentSolarSystem(solarGliese792); // Set the orbit center


        planetGlieseB.setRelativeDistanceFromCenter(new CelestialBody.ScalableDistance(1.4F, 1.4F));
        planetGlieseB.setRelativeOrbitTime(15.5F);
        planetGlieseB.setRelativeSize(0.8F);

        // Gameplay Properties
        planetGlieseB.setTierRequired(2);              // Rocket tier needed
        planetGlieseB.setDimensionInfo(-81, WorldProviderGliese.class);

        // Planet Icon
        planetGlieseB.setBodyIcon(new ResourceLocation("htgt6", "textures/gui/celestialbodies/planet_gliese_b.png"));

        // 4. Registry
        GalaxyRegistry.registerSolarSystem(solarGliese792);
        GalaxyRegistry.registerPlanet(planetGlieseB); // Register the planet

        System.out.println("[HTGT6] Gliese 792 System and Planet 'b' registered.");


        //GlieseC

        // =========================
// SECOND PLANET
// =========================

        planetGlieseC = new Planet("gliese_792_c");
        planetGlieseC.setParentSolarSystem(solarGliese792);
        planetGlieseC.setRelativeDistanceFromCenter(
                new CelestialBody.ScalableDistance(2.2F, 2.2F)
        );
        planetGlieseC.setRelativeOrbitTime(32.0F);
        planetGlieseC.setRelativeSize(1.1F);
        planetGlieseC.setTierRequired(2);

        planetGlieseC.atmosphereComponent(IAtmosphericGas.NITROGEN).atmosphereComponent(IAtmosphericGas.OXYGEN).atmosphereComponent( IAtmosphericGas.CO2).atmosphereComponent( IAtmosphericGas.HYDROGEN);

// NEW DIMENSION ID
        planetGlieseC.setDimensionInfo(-888, WorldProviderGlieseC.class
        );

// PLANET ICON
        planetGlieseC.setBodyIcon(
                new ResourceLocation(
                        "htgt6",
                        "textures/gui/celestialbodies/planet_gliese_c.png"
                )
        );

// HABITABLE ATMOSPHERE
       // planetGlieseC.atmosphereComponent(EnumAtmosphericGas.OXYGEN);
      //  planetGlieseC.atmosphereComponent(EnumAtmosphericGas.NITROGEN);

// REGISTER
        GalaxyRegistry.registerPlanet(planetGlieseC);
    }
    private static void registerTeleports () {
        GalacticraftRegistry.registerTeleportType(WorldProviderGliese.class, new TeleportTypeGlieseb());
        GalacticraftRegistry.registerTeleportType(WorldProviderGlieseC.class, new TeleportTypeGlieseC());
    }

}
