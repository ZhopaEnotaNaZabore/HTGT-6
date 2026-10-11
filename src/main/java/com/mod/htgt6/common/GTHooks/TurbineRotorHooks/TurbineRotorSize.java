package com.mod.htgt6.common.GTHooks.TurbineRotorHooks;

/** The four GT5-compatible turbine rotor envelopes. */
public enum TurbineRotorSize {
    SMALL("small", 1.0D, 1.0D, 0.0D, 4, 16L, 160L),
    NORMAL("normal", 2.0D, 2.0D, 2.5D, 8, 64L, 320L),
    LARGE("large", 3.0D, 3.0D, 5.0D, 12, 256L, 640L),
    HUGE("huge", 4.0D, 4.0D, 7.5D, 16, 1024L, 1280L);

    public final String key;
    public final double speedMultiplier;
    public final double durabilityMultiplier;
    public final double baseDamage;
    public final int componentCount;
    public final long assemblyEUt;
    public final long assemblyDuration;

    TurbineRotorSize(String key, double speedMultiplier, double durabilityMultiplier,
                     double baseDamage, int componentCount, long assemblyEUt, long assemblyDuration) {
        this.key = key;
        this.speedMultiplier = speedMultiplier;
        this.durabilityMultiplier = durabilityMultiplier;
        this.baseDamage = baseDamage;
        this.componentCount = componentCount;
        this.assemblyEUt = assemblyEUt;
        this.assemblyDuration = assemblyDuration;
    }

    public static TurbineRotorSize fromMetadata(int metadata) {
        TurbineRotorSize[] values = values();
        return values[Math.max(0, Math.min(metadata, values.length - 1))];
    }
}
