package com.mod.htgt6.sulimited.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import galaxyspace.core.client.gui.screen.GSGuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import net.minecraftforge.client.event.GuiOpenEvent;

/**
 * Starts every GalaxySpace map in a galaxy-only view while retaining the
 * original GalaxySpace map renderer and navigation after a selection.
 */
public final class GalaxyMapStartHandler {
    @SubscribeEvent
    public void onGalaxyMapOpened(GuiOpenEvent event) {
        if (event.gui instanceof GuiGalaxyCatalogMap) return;
        if (event.gui instanceof GSGuiCelestialSelection) {
            GSGuiCelestialSelection map = (GSGuiCelestialSelection) event.gui;
            event.gui = new GuiGalaxyCatalogMap(readMapMode(map), map.possibleBodiesGS,
                    new Integer[] { Integer.valueOf(map.currenttier), Integer.valueOf(readFuelRocketLevel(map)) });
            return;
        }

        /*
         * Do not replace AmunRa's base selection GUI.  It owns mothership
         * state and the rocket tier checks; recreating it as a GalaxySpace
         * screen made valid AmunRa launch destinations unavailable.  The
         * black-hole marker is added as a non-invasive overlay instead.
         */
    }

    private static boolean readMapMode(Object map) {
        Object value = readField(map, "mapMode");
        return value instanceof Boolean && ((Boolean) value).booleanValue();
    }

    private static int readFuelRocketLevel(Object map) {
        Object value = readField(map, "fuelRocketLevel");
        return value instanceof Integer ? ((Integer) value).intValue() : 0;
    }

    private static Object readField(Object map, String name) {
        for (Class<?> type = map.getClass(); type != null; type = type.getSuperclass()) {
            try {
                java.lang.reflect.Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(map);
            } catch (NoSuchFieldException ignored) {
                // Some state belongs to GalaxyCraft's parent screen.
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }
}
