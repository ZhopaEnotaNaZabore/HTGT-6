package com.mod.htgt6.client.inventory112;

import codechicken.nei.api.API;
import codechicken.nei.api.INEIGuiAdapter;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;

/** Reserves the complete responsive 1.12 inventory surface in NEI's item panel. */
public final class Inventory112NEIGuiHandler extends INEIGuiAdapter {
    private static boolean registered;

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        API.registerNEIGuiHandler(new Inventory112NEIGuiHandler());
    }

    @Override public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int width, int height) {
        List<Rectangle> areas;
        if (gui instanceof GuiInventory112) areas = ((GuiInventory112) gui).neiOccupiedAreas();
        else if (gui instanceof GuiCrafting112) areas = ((GuiCrafting112) gui).neiOccupiedAreas();
        else areas = Collections.emptyList();
        for (Rectangle area : areas)
            if (overlaps(area, x, y, width, height)) return true;
        return false;
    }

    private static boolean overlaps(Rectangle area, int x, int y, int width, int height) {
        return width > 0 && height > 0 && x < area.x + area.width && x + width > area.x
                && y < area.y + area.height && y + height > area.y;
    }

    private Inventory112NEIGuiHandler() { }
}
