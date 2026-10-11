package com.mod.htgt6.api.hitech6.SpaceTechExpanded;

/**
 * Describes a binary stellar pair independently of Galacticraft's one-main-
 * star limitation.  The companion can still use a non-travel celestial body
 * as its renderer anchor without becoming a planet or a destination.
 */
public final class BinaryStarSystemData {
    public final String solarSystemName;
    public final String primaryBodyName;
    public final String companionBodyName;
    public final float visualSeparation;
    public final float orbitalPeriodYears;
    public final float primaryLuminosity;
    public final float companionLuminosity;

    public BinaryStarSystemData(String solarSystemName, String primaryBodyName, String companionBodyName,
                                float visualSeparation, float orbitalPeriodYears) {
        this(solarSystemName, primaryBodyName, companionBodyName,
                visualSeparation, orbitalPeriodYears, 1.0F, 1.0F);
    }

    public BinaryStarSystemData(String solarSystemName, String primaryBodyName, String companionBodyName,
                                float visualSeparation, float orbitalPeriodYears,
                                float primaryLuminosity, float companionLuminosity) {
        if (solarSystemName == null || primaryBodyName == null || companionBodyName == null) {
            throw new IllegalArgumentException("Binary system identifiers are required");
        }
        if (primaryLuminosity < 0.0F || companionLuminosity < 0.0F) {
            throw new IllegalArgumentException("Stellar luminosity cannot be negative");
        }
        this.solarSystemName = solarSystemName;
        this.primaryBodyName = primaryBodyName;
        this.companionBodyName = companionBodyName;
        this.visualSeparation = visualSeparation;
        this.orbitalPeriodYears = orbitalPeriodYears;
        this.primaryLuminosity = primaryLuminosity;
        this.companionLuminosity = companionLuminosity;
    }

    public boolean containsBody(String bodyName) {
        return primaryBodyName.equalsIgnoreCase(bodyName) || companionBodyName.equalsIgnoreCase(bodyName);
    }

    public boolean isCompanion(String bodyName) {
        return companionBodyName.equalsIgnoreCase(bodyName);
    }

    public float getLuminosity(String bodyName) {
        if (primaryBodyName.equalsIgnoreCase(bodyName)) return primaryLuminosity;
        if (companionBodyName.equalsIgnoreCase(bodyName)) return companionLuminosity;
        return 0.0F;
    }

    public float getCombinedLuminosity() {
        return primaryLuminosity + companionLuminosity;
    }
}
