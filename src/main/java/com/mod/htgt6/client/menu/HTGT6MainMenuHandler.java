package com.mod.htgt6.client.menu;

import com.mod.htgt6.asm.HTGT6Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;

/** Replaces only vanilla's root main menu; child screens retain normal parents. */
public final class HTGT6MainMenuHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (HTGT6Config.ENABLE_CUSTOM_MAIN_MENU && event.gui != null
                && event.gui.getClass() == GuiMainMenu.class) {
            event.gui = new GuiHTGT6MainMenu();
        }
    }
}
