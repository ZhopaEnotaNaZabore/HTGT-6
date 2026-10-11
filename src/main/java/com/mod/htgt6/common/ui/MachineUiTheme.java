package com.mod.htgt6.common.ui;

import com.mod.htgt6.asm.HTGT6Config;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Locale;

/**
 * Client-selectable palette for ModularUI machine terminals. The selected
 * palette is visual only and is deliberately not sent to a server.
 */
public enum MachineUiTheme {
    TURQUOISE("Turquoise", 0xFF252A2F, 0xFF0B1116, 0xFF65727B, 0xFFE5E5E5,
            0xFF8ED6FF, 0xFF9AF7A3, 0xFFFFD479, 0xFFB6C5CC),
    RED("Red", 0xFF302326, 0xFF170B0E, 0xFF99545A, 0xFFFFE5E5,
            0xFFFF9B9B, 0xFFB8F59A, 0xFFFFDB8A, 0xFFD2B8BC),
    BLUE("Blue", 0xFF202A38, 0xFF09111C, 0xFF4C78A8, 0xFFE5F1FF,
            0xFF8CBFFF, 0xFF9AE4F7, 0xFFFFE08A, 0xFFB5C9DF),
    AMBER("Amber", 0xFF302B20, 0xFF171208, 0xFF9A7A3E, 0xFFFFF2D5,
            0xFFFFCE77, 0xFFBFF79A, 0xFFFFE08A, 0xFFD5C9AD);

    private static final String CATEGORY = "machine_ui";
    private static final String KEY = "theme";
    private static Configuration configuration;
    private static MachineUiTheme active = TURQUOISE;

    private final String displayName;
    private final int panelColor;
    private final int terminalColor;
    private final int frameColor;
    private final int titleColor;
    private final int primaryColor;
    private final int energyColor;
    private final int productionColor;
    private final int labelColor;

    MachineUiTheme(String displayName, int panelColor, int terminalColor, int frameColor, int titleColor,
                   int primaryColor, int energyColor, int productionColor, int labelColor) {
        this.displayName = displayName;
        this.panelColor = panelColor;
        this.terminalColor = terminalColor;
        this.frameColor = frameColor;
        this.titleColor = titleColor;
        this.primaryColor = primaryColor;
        this.energyColor = energyColor;
        this.productionColor = productionColor;
        this.labelColor = labelColor;
    }

    public String getDisplayName() { return displayName; }
    public int getPanelColor() { return panelColor; }
    public int getTerminalColor() { return terminalColor; }
    public int getFrameColor() { return frameColor; }
    public int getTitleColor() { return titleColor; }
    public int getPrimaryColor() { return primaryColor; }
    public int getEnergyColor() { return energyColor; }
    public int getProductionColor() { return productionColor; }
    public int getLabelColor() { return labelColor; }

    public static MachineUiTheme getActive() { return active; }

    public static void load(File configDirectory) {
        configuration = new Configuration(HTGT6Config.getConfigFile(configDirectory, "HiTech6-client.cfg"));
        configuration.load();
        MenuTheme.load(configuration);
        active = fromId(configuration.getString(KEY, CATEGORY, TURQUOISE.name(),
                "Machine terminal colour theme: TURQUOISE, RED, BLUE or AMBER."));
        saveIfChanged();
    }

    public static void setActive(MachineUiTheme theme) {
        if (theme == null) return;
        active = theme;
        if (configuration != null) {
            configuration.get(CATEGORY, KEY, TURQUOISE.name()).set(theme.name());
            saveIfChanged();
        }
    }

    private static MachineUiTheme fromId(String value) {
        if (value != null) {
            try { return valueOf(value.trim().toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ignored) { }
        }
        return TURQUOISE;
    }

    private static void saveIfChanged() {
        if (configuration != null && configuration.hasChanged()) configuration.save();
    }
}
