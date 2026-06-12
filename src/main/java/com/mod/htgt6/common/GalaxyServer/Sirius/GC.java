package com.mod.htgt6.common.GalaxyServer.Sirius;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import net.minecraft.util.ResourceLocation;

public class GC {
    public static SolarSystem solarSystemSirius;
    public static Star starSiriusA;

    public GC() {
    }

    public static void init(FMLInitializationEvent event) {
        registerSolarSystem();
    }

    private static void registerSolarSystem() {
        solarSystemSirius = new SolarSystem("sirius_system", "milkyWay");
        solarSystemSirius.setMapPosition(new Vector3(12.5, -1.2000000476837158, 0.0));
        starSiriusA = new Star("sirius_star");
        starSiriusA.setParentSolarSystem(solarSystemSirius);
        starSiriusA.setRelativeSize(1.5F);
        starSiriusA.setBodyIcon(new ResourceLocation("htgt6", "textures/gui/celestialbodies/star_sirius.png"));
        solarSystemSirius.setMainStar(starSiriusA);
        GalaxyRegistry.registerSolarSystem(solarSystemSirius);
        System.out.println("[HTGT6] Sirius System and Star registered via SolarSystem registry.");
    }
}
















