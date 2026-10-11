package com.mod.htgt6.client.menu;

import com.mod.htgt6.asm.HTGT6Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;

/** Replaces only vanilla's root options screen, leaving every child screen intact. */
public final class HTGT6OptionsMenuHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!HTGT6Config.ENABLE_CUSTOM_OPTIONS_MENU || event.gui == null
                || event.gui.getClass() != GuiOptions.class) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        GuiScreen parent = minecraft.currentScreen;
        event.gui = new GuiHTGT6Options(parent, minecraft.gameSettings);
    }
}
