package com.mod.htgt6.sulimited.galacticraft;

import com.mod.htgt6.api.hitech6.SpaceTechExpanded.GalaxyCentreMapLayout;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;

/** Keeps late-loading Galacticraft add-ons out of reserved galaxy centres. */
public final class GalaxyCentreMapLayoutHandler {
    @SubscribeEvent
    public void onSolarSystemRegistered(GalaxyRegistry.SolarSystemRegisterEvent event) {
        GalaxyCentreMapLayout.reserveCentres();
    }
}
