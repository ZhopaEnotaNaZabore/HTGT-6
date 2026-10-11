package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus;

import galaxyspace.api.BodiesHelper;
import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.core.dimension.TeleportTypeMoon;
import net.minecraft.world.WorldProvider;

/** Adds gameplay dimensions to GalaxySpace's existing Uranian moon cards. */
public final class UranusMoonWorlds {
    public static Moon umbriel, titania, oberon;
    private UranusMoonWorlds() { }
    public static void init(int umbrielDimensionId,int titaniaDimensionId,int oberonDimensionId) {
        umbriel=require(SolarSystemBodies.umbrielUranus,"umbrielUranus");
        attach(umbriel,umbrielDimensionId,WorldProviderUmbriel.class,0.045F,-205.0F,358000L);
        titania=require(SolarSystemBodies.titaniaUranus,"titaniaUranus");
        attach(titania,titaniaDimensionId,WorldProviderTitania.class,0.040F,-203.0F,752000L);
        oberon=require(SolarSystemBodies.oberonUranus,"oberonUranus");
        attach(oberon,oberonDimensionId,WorldProviderOberon.class,0.042F,-205.0F,1163000L);
    }
    private static void attach(Moon moon,int dimensionId,Class<? extends WorldProvider> provider,float gravity,float temperature,long dayLength){moon.setDimensionInfo(dimensionId,provider).setTierRequired(6);BodiesHelper.registerBodyWithClass(moon,new BodiesHelper.BodiesData(BodiesHelper.iceworld,gravity,0,temperature,0.0F,dayLength,false,true));GalacticraftRegistry.registerTeleportType(provider,new TeleportTypeMoon());}
    private static Moon require(Moon moon,String field){if(moon==null)throw new IllegalStateException("GalaxySpace did not register SolarSystemBodies."+field);return moon;}
}
