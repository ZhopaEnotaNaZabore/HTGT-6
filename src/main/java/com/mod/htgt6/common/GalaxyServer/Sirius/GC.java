package com.mod.htgt6.common.GalaxyServer.Sirius;

import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class GC {

    public static SolarSystem solarSystemSirius;
    public static Star starSiriusA;

    public static void init(FMLInitializationEvent event) {
        registerSolarSystem();
    }

    private static void registerSolarSystem() {
        // 1. Initialize the Solar System
        // Default galaxy for 1.7.10 is "milkyWay"
        solarSystemSirius = new SolarSystem("sirius_system", "milkyWay");

        // 2. Set coordinates on the Star Map
        solarSystemSirius.setMapPosition(new Vector3(1.5F, -1.2F, 0.0F));

        // 3. Define the Star
        starSiriusA = new Star("sirius_star");
        starSiriusA.setParentSolarSystem(solarSystemSirius);
        starSiriusA.setRelativeSize(1.5F); // Sirius is larger than our Sun

        // 4. Assign Texture
        starSiriusA.setBodyIcon(new ResourceLocation("htgt6", "textures/gui/celestialbodies/star_sirius.png"));


        solarSystemSirius.setMainStar(starSiriusA);


        GalaxyRegistry.registerSolarSystem(solarSystemSirius);

        System.out.println("[HTGT6] Sirius System and Star registered via SolarSystem registry.");
    }
}

