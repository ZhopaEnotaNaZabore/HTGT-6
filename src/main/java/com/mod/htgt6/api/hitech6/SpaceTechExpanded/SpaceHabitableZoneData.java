package com.mod.htgt6.api.hitech6.SpaceTechExpanded;

/** Visual habitable-zone band attached to a reference orbit in a solar system. */
public final class SpaceHabitableZoneData {
    public final String solarSystemName;
    public final String referenceBodyName;
    public final float innerOrbitFactor;
    public final float outerOrbitFactor;

    public SpaceHabitableZoneData(String solarSystemName, String referenceBodyName,
                                  float innerOrbitFactor, float outerOrbitFactor) {
        if (solarSystemName == null || referenceBodyName == null) {
            throw new IllegalArgumentException("Habitable-zone identifiers are required");
        }
        if (innerOrbitFactor <= 0.0F || outerOrbitFactor <= innerOrbitFactor) {
            throw new IllegalArgumentException("Invalid habitable-zone boundaries");
        }
        this.solarSystemName = solarSystemName;
        this.referenceBodyName = referenceBodyName;
        this.innerOrbitFactor = innerOrbitFactor;
        this.outerOrbitFactor = outerOrbitFactor;
    }
}
