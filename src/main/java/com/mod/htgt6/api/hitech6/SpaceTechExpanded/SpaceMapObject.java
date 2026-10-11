package com.mod.htgt6.api.hitech6.SpaceTechExpanded;

/** Immutable description of an object shown on the SpaceTech galaxy map. */
public final class SpaceMapObject {
    public final String id;
    public final String galaxyKey;
    public final String translationKey;
    public final SpaceMapObjectType type;
    public final GalaxyMapForm form;
    /** Null when the object has no central black hole to display. */
    public final String blackHoleName;
    public final float mapX;
    public final float mapY;
    public final int destinationDimension;

    public SpaceMapObject(String id, String galaxyKey, String translationKey,
                          SpaceMapObjectType type, float mapX, float mapY,
                          int destinationDimension) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("Space map id is required");
        if (translationKey == null || translationKey.isEmpty()) throw new IllegalArgumentException("Translation key is required");
        this.id = id;
        this.galaxyKey = galaxyKey;
        this.translationKey = translationKey;
        this.type = type;
        this.mapX = Math.max(0.0F, Math.min(1.0F, mapX));
        this.mapY = Math.max(0.0F, Math.min(1.0F, mapY));
        this.destinationDimension = destinationDimension;
        this.form = type == SpaceMapObjectType.NEBULA ? GalaxyMapForm.NEBULA
                : type == SpaceMapObjectType.GALAXY ? GalaxyMapForm.SPIRAL : GalaxyMapForm.OTHER;
        this.blackHoleName = null;
    }

    public SpaceMapObject(String id, String galaxyKey, String translationKey,
                          SpaceMapObjectType type, GalaxyMapForm form, String blackHoleName,
                          float mapX, float mapY, int destinationDimension) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("Space map id is required");
        if (translationKey == null || translationKey.isEmpty()) throw new IllegalArgumentException("Translation key is required");
        this.id = id;
        this.galaxyKey = galaxyKey;
        this.translationKey = translationKey;
        this.type = type;
        this.form = form == null ? GalaxyMapForm.OTHER : form;
        this.blackHoleName = blackHoleName == null || blackHoleName.isEmpty() ? null : blackHoleName;
        this.mapX = Math.max(0.0F, Math.min(1.0F, mapX));
        this.mapY = Math.max(0.0F, Math.min(1.0F, mapY));
        this.destinationDimension = destinationDimension;
    }

    public boolean opensGalaxyMap() {
        return galaxyKey != null && !galaxyKey.isEmpty();
    }

    public boolean hasDestination() {
        return destinationDimension != Integer.MIN_VALUE;
    }
}
