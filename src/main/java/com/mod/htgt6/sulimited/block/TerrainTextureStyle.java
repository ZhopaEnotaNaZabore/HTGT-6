package com.mod.htgt6.sulimited.block;

/** The complete shared set of neutral 64x64 terrain masks. */
public enum TerrainTextureStyle {
    SMOOTH_SANDY("smooth_sandy", 223),
    ROUGH_ROCKY("rough_rocky", 226),
    MAGMATIC("magmatic", 215),
    CHIPPED_STONE("chipped_stone", 229),
    SMOOTH_SNOWY("smooth_snowy", 225),
    BROKEN_ICE("broken_ice", 215),
    SUEDE_STONE("suede_stone", 224),
    ROUGH_REGOLITH("rough_regolith", 230),
    SMOOTH_REGOLITH("smooth_regolith", 224),
    DENSE_CORE("dense_core", 217);

    private final String texture;
    private final int averageLuminance;

    TerrainTextureStyle(String texture, int averageLuminance) {
        this.texture = texture;
        this.averageLuminance = averageLuminance;
    }

    public String textureName() {
        return "htgt6:index/" + texture;
    }

    /** Compensates for the mask's gray average so the old material RGB is retained. */
    public int tintForAverage(int colour) {
        int red = compensate(colour >>> 16 & 0xFF);
        int green = compensate(colour >>> 8 & 0xFF);
        int blue = compensate(colour & 0xFF);
        return red << 16 | green << 8 | blue;
    }

    private int compensate(int channel) {
        return Math.min(255, Math.round(channel * 255.0F / averageLuminance));
    }
}
