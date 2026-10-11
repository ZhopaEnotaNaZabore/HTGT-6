package com.mod.htgt6.api.hitech6.SpaceTechExpanded;

/**
 * Non-travel celestial feature placed at the origin of a galaxy map.
 * It is deliberately independent from Galacticraft CelestialBody so it
 * cannot accidentally become a teleport destination.
 */
public final class GalaxyCentre {
    public final String galaxyKey;
    public final String blackHoleName;
    public final float mapX;
    public final float mapY;

    public GalaxyCentre(String galaxyKey, String blackHoleName, float mapX, float mapY) {
        this.galaxyKey = galaxyKey;
        this.blackHoleName = blackHoleName;
        this.mapX = mapX;
        this.mapY = mapY;
    }

    public boolean isPresent() {
        return blackHoleName != null && !blackHoleName.isEmpty();
    }
}
