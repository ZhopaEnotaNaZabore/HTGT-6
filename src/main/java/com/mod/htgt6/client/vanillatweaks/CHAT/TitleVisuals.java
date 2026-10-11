package com.mod.htgt6.client.vanillatweaks.CHAT;

/** Client-only animated formatting for titles, shared by CHAT and TAB. */
public final class TitleVisuals {
    private TitleVisuals() {}
    public static String animate(String text, long tick) {
        text = replace(text, "[InfinityHorizons]", tick, "b39fff");
        text = replace(text, "[Maiden]", tick, "cce4c");
        text = replace(text, "[Eternity]", tick, "19db5");
        text = replace(text, "[GMC]", tick, "4c6c");
        // A distinctive cyan-to-light-purple sweep for the closed beta tester title.
        text = replace(text, "[CBT-DEV]", tick, "b9d5f", true);
        return text;
    }
    private static String replace(String text, String title, long tick, String colors) {
        return replace(text, title, tick, colors, false);
    }
    private static String replace(String text, String title, long tick, String colors, boolean bold) {
        int at = text.indexOf(title);
        if (at < 0) return text;
        StringBuilder animated = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            char c = title.charAt(i);
            animated.append('§').append(colors.charAt((int) ((tick / 3 + i) % colors.length())));
            if (bold) animated.append("§l");
            animated.append(c);
        }
        return text.substring(0, at) + animated.append("§r").toString() + text.substring(at + title.length());
    }
}
