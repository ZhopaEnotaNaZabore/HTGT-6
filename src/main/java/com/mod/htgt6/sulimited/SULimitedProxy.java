package com.mod.htgt6.sulimited;

import com.mod.htgt6.sulimited.config.SULimitedConfig;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.GalaxyCentreMapLayout;
import com.mod.htgt6.sulimited.galacticraft.Glise792.sysGliese792;
import com.mod.htgt6.sulimited.galacticraft.GalaxyCentreMapLayoutHandler;
import com.mod.htgt6.sulimited.galacticraft.GalaxyCentreStarRegistry;
import com.mod.htgt6.sulimited.galacticraft.CataloguedStarRegistry;
import com.mod.htgt6.sulimited.galacticraft.AmunRaGalaxySpaceBridge;
import com.mod.htgt6.sulimited.galacticraft.Sirius.SiriusSystem;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysExpanded;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.charon.blocks.CharonBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.proteus.blocks.ProteusBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune.NeptuneContent;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune.NeptuneStormHandler;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn.SaturnMoonBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn.SaturnMoonWorlds;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars.MarsMoonBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.VenusSpaceStationFix;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus.UranusMoonBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus.UranusMoonWorlds;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor.MinorWorldBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor.MinorWorlds;
import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import com.mod.htgt6.sulimited.world.GT6PlanetOreGeneration;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;

/** Common lifecycle for the SU-Limited child mod. */
public class SULimitedProxy {
    public void preInit(FMLPreInitializationEvent event) {
        SULimitedConfig.load(event.getModConfigurationDirectory());
        CharonBlocks.register();
        ProteusBlocks.register();
        NeptuneContent.register();
        SaturnMoonBlocks.register();
        MarsMoonBlocks.register();
        UranusMoonBlocks.register();
        MinorWorldBlocks.register();
        com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology.register();
        // Also covers systems registered by add-ons after SU-Limited.
        MinecraftForge.EVENT_BUS.register(new GalaxyCentreMapLayoutHandler());
    }

    public void init(FMLInitializationEvent event) {
        // Dimension IDs and celestial registry keys intentionally stay intact.
        SiriusSystem.init(event);
        sysGliese792.init(event);
        GalaxyCentreStarRegistry.registerCentres();
        CataloguedStarRegistry.registerStars();
        BiomeGenSpaceUltimate.init(SULimitedConfig.spaceUltimateBiomeId);
        BiomeGenSpaceUltimate.initGlieseC(SULimitedConfig.gliese792CBiomeId);
        BiomeGenSpaceUltimate.initRaBiomes(SULimitedConfig.raHorusBiomeId, SULimitedConfig.raNeperBiomeId,
                SULimitedConfig.raMaahesBiomeId, SULimitedConfig.raSethBiomeId,
                SULimitedConfig.raAnubisBiomeId);
        BiomeGenSpaceUltimate.initCharon(SULimitedConfig.charonBiomeId);
        BiomeGenSpaceUltimate.initProteus(SULimitedConfig.proteusBiomeId);
        BiomeGenSpaceUltimate.initNeptune(SULimitedConfig.neptuneBiomeId);
        BiomeGenSpaceUltimate.initSaturnMoons(SULimitedConfig.mimasBiomeId, SULimitedConfig.tethysBiomeId,
                SULimitedConfig.dioneBiomeId, SULimitedConfig.rheaBiomeId, SULimitedConfig.iapetusBiomeId);
        BiomeGenSpaceUltimate.initMarsMoons(SULimitedConfig.phobosBiomeId, SULimitedConfig.deimosBiomeId);
        BiomeGenSpaceUltimate.initUranusMoons(SULimitedConfig.umbrielBiomeId,
                SULimitedConfig.titaniaBiomeId, SULimitedConfig.oberonBiomeId);
        BiomeGenSpaceUltimate.initOuterSolWorlds(SULimitedConfig.arielBiomeId,
                SULimitedConfig.haumeaBiomeId, SULimitedConfig.makemakeBiomeId,
                SULimitedConfig.erisBiomeId, SULimitedConfig.deeDeeBiomeId);
        SolSysExpanded.init(SULimitedConfig.charonDimensionId, SULimitedConfig.proteusDimensionId,
                SULimitedConfig.neptuneDimensionId, SULimitedConfig.phobosDimensionId,
                SULimitedConfig.deimosDimensionId);
        SaturnMoonWorlds.init(SULimitedConfig.mimasDimensionId, SULimitedConfig.tethysDimensionId,
                SULimitedConfig.dioneDimensionId, SULimitedConfig.rheaDimensionId,
                SULimitedConfig.iapetusDimensionId);
        VenusSpaceStationFix.register();
        UranusMoonWorlds.init(SULimitedConfig.umbrielDimensionId,
                SULimitedConfig.titaniaDimensionId, SULimitedConfig.oberonDimensionId);
        MinorWorlds.init(SULimitedConfig.arielDimensionId, SULimitedConfig.haumeaDimensionId,
                SULimitedConfig.makemakeDimensionId, SULimitedConfig.erisDimensionId,
                SULimitedConfig.deeDeeDimensionId);
        NeptuneStormHandler neptuneWeather = new NeptuneStormHandler();
        FMLCommonHandler.instance().bus().register(neptuneWeather);
    }

    public void postInit(FMLPostInitializationEvent event) {
        // GregTech finalizes its ore blocks during its lifecycle.  Register
        // Gliese's custom stone only after that block registry is available.
        GT6PlanetOreGeneration.registerOreHosts();
        int relocatedSystems = GalaxyCentreMapLayout.reserveCentres();
        if (relocatedSystems > 0) {
            System.out.println("[SU-Limited] Relocated " + relocatedSystems
                    + " solar system(s) away from galaxy centres.");
        }
    }

    /** Runs after all add-ons, including AmunRa, have registered their systems. */
    public void loadComplete(FMLLoadCompleteEvent event) {
        // AmunRa finishes its original registrations after SU-Limited init.
        // Attach GalaxySpace metadata only then; IDs and providers stay intact.
        AmunRaGalaxySpaceBridge.register();
        int relocatedSystems = GalaxyCentreMapLayout.reserveCentres();
        if (relocatedSystems > 0) {
            System.out.println("[SU-Limited] Final galaxy-centre layout moved "
                    + relocatedSystems + " solar system(s).");
        }
    }
}
