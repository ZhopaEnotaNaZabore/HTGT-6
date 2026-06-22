package com.mod.htgt6.client.vanillatweaks;

import com.mod.htgt6.client.vanillatweaks.SSUI.GuiModernServerSelect;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiOpenEvent;

public class VanillaTweaksMultiplayerHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null) {
            return;
        }

        // Catch the multiplayer screen only
        if (event.gui.getClass() == GuiMultiplayer.class) {
            System.out.println("Replacing multiplayer selection GUI via dedicated handler.");
            event.gui = new GuiModernServerSelect(new GuiMainMenu());
        }
    }
}