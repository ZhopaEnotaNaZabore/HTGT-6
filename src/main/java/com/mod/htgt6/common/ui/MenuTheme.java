package com.mod.htgt6.common.ui;

import java.util.Locale;
import net.minecraftforge.common.config.Configuration;

/** Client-only preference stored alongside (but independently of) machine colours. */
public enum MenuTheme {
    MOONLIGHT, INDUSTRIAL;

    private static final MenuTheme DEFAULT = INDUSTRIAL;
    private static MenuTheme active = DEFAULT;
    private static Configuration configuration;

    public static MenuTheme getActive() { return active; }
    public static boolean isIndustrial() { return active == INDUSTRIAL; }
    public String translationKey() { return "htgt6.theme." + name().toLowerCase(Locale.ROOT); }

    public static void load(Configuration config) {
        configuration = config;
        String value = config.getString("theme", "menu_ui", DEFAULT.name(),
                "Menu appearance: MOONLIGHT (blue/violet) or INDUSTRIAL (black/orange). Client only.");
        try { active = valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ignored) { active = DEFAULT; }
        config.get("menu_ui", "theme", DEFAULT.name()).set(active.name());
        if (config.hasChanged()) config.save();
    }

    public static void setActive(MenuTheme theme) {
        if (theme == null) return;
        active = theme;
        if (configuration != null) {
            configuration.get("menu_ui", "theme", DEFAULT.name()).set(theme.name());
            if (configuration.hasChanged()) configuration.save();
        }
    }

    /** Explicit paired colours retain alpha and allow immediate switching in open screens. */
    public static int color(int moonlight, int industrial) {
        return isIndustrial() ? industrial : moonlight;
    }
}
