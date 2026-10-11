package com.mod.htgt6.client.vanillatweaks.CHAT;


import net.minecraft.util.ResourceLocation;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmojiRegistry {
    /** Stable order is required because the compact network markers use the entry index. */
    private static final Map<String, ResourceLocation> EMOJI_MAP = new LinkedHashMap<String, ResourceLocation>();

    static {
        // Register your discord emojis here
        register(":maintenance:", "htgt6", "textures/emojis/Maintenance.png");
        register(":ban:", "htgt6", "textures/emojis/Ban.png");
        register(":kekw:", "htgt6", "textures/emojis/kekw.png");
        register(":thinking:", "htgt6", "textures/emojis/KannaThinking.png");
        register(":Hi:", "htgt6", "textures/emojis/Hi.png" );
        register(":0TPS:", "htgt6", "textures/emojis/riptps.png" );
        register(":fu:", "htgt6", "textures/emojis/Fu.png" );
        register(":Pivo:", "htgt6", "textures/emojis/Pivo.png");
        register(":Raid:", "htgt6", "textures/emojis/Raid.png");
        register(":Gimme:", "htgt6", "textures/emojis/Gimme.png");
        register(":Motivated:", "htgt6", "textures/emojis/Motivated.png");
        register(":Ofc:", "htgt6", "textures/emojis/Ofc.png");
        register(":No:", "htgt6", "textures/emojis/No.png");
        register(":Mining:", "htgt6", "textures/emojis/Mining.png");
        register(":Wait:", "htgt6", "textures/emojis/Wait.png");
        register(":bug:", "htgt6", "textures/emojis/Bug.png");
        register(":fubug:", "htgt6", "textures/emojis/FuBug.png");
        register(":sip:", "htgt6", "textures/emojis/Sip.png");
        register(":cookie:", "htgt6", "textures/emojis/Cookie.png");
        register(":morning:", "htgt6", "textures/emojis/Morning.png");
        register(":dinner:", "htgt6", "textures/emojis/Dinner.png");
        register(":uberi:", "htgt6", "textures/emojis/Uberi.png");



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

    public static String marker(int index) {
        return "§e§e§" + (char) ('a' + index) + "§r    ";
    }

    /** Converts human-readable sticker codes into the markers understood by both chat renderers. */
    public static String encodeEmojis(String text) {
        if (text == null || text.isEmpty()) return text;
        int index = 0;
        for (String code : EMOJI_MAP.keySet()) {
            text = text.replace(code, marker(index++));
        }
        return text;
    }
}
