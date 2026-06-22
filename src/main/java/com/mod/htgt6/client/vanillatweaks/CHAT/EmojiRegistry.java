package com.mod.htgt6.client.vanillatweaks.CHAT;


import net.minecraft.util.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

public class EmojiRegistry {
    private static final Map<String, ResourceLocation> EMOJI_MAP = new HashMap<String, ResourceLocation>();

    static {
        // Register your discord emojis here
        register(":maintenance:", "htgt6", "textures/emojis/Maintenance.png");
        register(":ban:", "htgt6", "textures/emojis/Ban.png");
        register(":kekw:", "htgt6", "textures/emojis/kekw.png");
        register(":thinking:", "htgt6", "textures/emojis/KannaThinking.png");
        register(":Hi:", "htgt6", "textures/emojis/Hi.png" );

    }

    private static void register(String code, String domain, String path) {
        EMOJI_MAP.put(code, new ResourceLocation(domain, path));
    }

    public static ResourceLocation getEmojiTexture(String code) {
        return EMOJI_MAP.get(code);
    }

    public static Map<String, ResourceLocation> getRegisteredEmojis() {
        return EMOJI_MAP;
    }
}