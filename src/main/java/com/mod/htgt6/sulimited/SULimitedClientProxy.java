package com.mod.htgt6.sulimited;

import com.mod.htgt6.sulimited.client.CelestialTexturePainter;
import com.mod.htgt6.sulimited.client.GalaxyMapStartHandler;
import com.mod.htgt6.sulimited.client.GalaxyMapCatalog;
import com.mod.htgt6.sulimited.client.GlieseHabitableZoneRenderer;
import com.mod.htgt6.sulimited.client.GlieseCelestialRotationHandler;
import com.mod.htgt6.sulimited.client.SpaceUltimateF3Handler;
import com.mod.htgt6.sulimited.client.SpaceHabitableZoneRenderer;
import com.mod.htgt6.sulimited.client.SolarSystemBoundaryRenderer;
import com.mod.htgt6.sulimited.client.NeptuneAtmosphereRenderHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.common.MinecraftForge;

/** Client-only render and diagnostic hooks for SU-Limited. */
public final class SULimitedClientProxy extends SULimitedProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        com.mod.htgt6.sulimited.client.GlieseAquaticRenderer.register();
        GalaxyMapCatalog.registerInnerMapBackgrounds();
        CelestialTexturePainter.applyGliesePalette();
        CelestialTexturePainter.applyOuterSolPalettes();
        MinecraftForge.EVENT_BUS.register(new GalaxyMapStartHandler());
        MinecraftForge.EVENT_BUS.register(new GlieseHabitableZoneRenderer());
        MinecraftForge.EVENT_BUS.register(new SpaceHabitableZoneRenderer());
        MinecraftForge.EVENT_BUS.register(new SolarSystemBoundaryRenderer());
        GlieseCelestialRotationHandler rotation = new GlieseCelestialRotationHandler();
        FMLCommonHandler.instance().bus().register(rotation);
        MinecraftForge.EVENT_BUS.register(rotation);
        MinecraftForge.EVENT_BUS.register(new SpaceUltimateF3Handler());
        MinecraftForge.EVENT_BUS.register(new NeptuneAtmosphereRenderHandler());
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        super.loadComplete(event);
        // AmunRa registers Ra after SU-Limited init, so its indexed dynamic
        // textures must be attached only at the end of the shared lifecycle.
        CelestialTexturePainter.applyRaPalette();
    }
}
