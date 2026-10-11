package com.mod.htgt6.client.vanillatweaks.CHAT;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.GuiOpenEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class ChatGuiInterceptor {
    private static final Field DEFAULT_TEXT = findDefaultTextField();

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        // Intercept vanilla chat screen open requests
        if (event.gui instanceof GuiChat && !(event.gui instanceof chatNEW)) {
            event.setCanceled(true);
            ChatModularScreen.open(readDefaultText((GuiChat) event.gui));
        }
    }

    private static Field findDefaultTextField() {
        try {
            return ReflectionHelper.findField(GuiChat.class, "defaultInputFieldText", "field_146409_v");
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String readDefaultText(GuiChat chat) {
        if (DEFAULT_TEXT == null) return "";
        try {
            Object value = DEFAULT_TEXT.get(chat);
            return value instanceof String ? (String) value : "";
        } catch (IllegalAccessException ignored) {
            return "";
        }
    }
}
