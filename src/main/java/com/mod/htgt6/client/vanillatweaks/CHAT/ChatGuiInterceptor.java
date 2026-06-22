package com.mod.htgt6.client.vanillatweaks.CHAT;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.GuiOpenEvent;

public class ChatGuiInterceptor {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        // Intercept vanilla chat screen open requests
        if (event.gui instanceof GuiChat && !(event.gui instanceof chatNEW)) {
            event.setCanceled(true);
            // Display our custom chat box safely
            Minecraft.getMinecraft().displayGuiScreen(new chatNEW());
        }
    }
}
