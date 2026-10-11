package com.mod.htgt6.api.hitech6.SpaceTechExpanded;

/** High-range stellar data for map HUDs; independent from planet-only data. */
public final class SpaceStarData {
    public final String starName;
    public final String typeTranslationKey;
    public final double temperatureKelvin;

    public SpaceStarData(String starName, String typeTranslationKey, double temperatureKelvin) {
        if (starName == null || starName.isEmpty()) throw new IllegalArgumentException("Star name is required");
        this.starName = starName;
        this.typeTranslationKey = typeTranslationKey;
        this.temperatureKelvin = temperatureKelvin;
    }
}
