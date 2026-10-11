package com.mod.htgt6.client.GUI;

import com.mod.htgt6.client.menu.GuiHiTechAppearance;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;

/** Opens shared HiTech appearance settings from the existing video-settings entry. */
public final class MachineUiThemeSettingsHandler {
    private static final int THEME_BUTTON_ID = 0x48544736;

    @SubscribeEvent
    public void onVideoSettingsInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.gui instanceof GuiVideoSettings)) return;
        event.buttonList.add(new GuiButton(THEME_BUTTON_ID, event.gui.width / 2 - 100,
                event.gui.height - 52, 200, 20, getButtonText()));
    }

    @SubscribeEvent
    public void onVideoSettingsAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (!(event.gui instanceof GuiVideoSettings) || event.button.id != THEME_BUTTON_ID) return;
        Minecraft.getMinecraft().displayGuiScreen(new GuiHiTechAppearance(event.gui));
        event.setCanceled(true);
    }

    private static String getButtonText() {
        return I18n.format("htgt6.theme.settings");
    }

}
