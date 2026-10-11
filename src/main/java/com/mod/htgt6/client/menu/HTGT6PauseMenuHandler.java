package com.mod.htgt6.client.menu;

import com.mod.htgt6.asm.HTGT6Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraftforge.client.event.GuiOpenEvent;

/** Replaces only vanilla's pause screen and suppresses third-party overlays tied to it. */
public final class HTGT6PauseMenuHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (HTGT6Config.ENABLE_CUSTOM_PAUSE_MENU && event.gui != null
                && event.gui.getClass() == GuiIngameMenu.class) {
            event.gui = new GuiHTGT6PauseMenu();
        }
    }
}
