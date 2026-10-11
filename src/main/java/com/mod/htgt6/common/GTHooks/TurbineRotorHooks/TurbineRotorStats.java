package com.mod.htgt6.common.GTHooks.TurbineRotorHooks;

import gregapi.oredict.OreDictMaterial;

/**
 * Independent HTGT6 port of the GT5 turbine rotor formulae.
 *
 * GT6 materials do not expose GT5's per-fuel rotor multipliers, therefore the
 * common material speed is used for steam and gas; plasma keeps GT5's x42
 * flow scale.  Consumers can apply their own fuel multiplier through
 * {@link #optimalFlow(Fuel, double)}.
 */
public final class TurbineRotorStats {
    public enum Fuel { STEAM, GAS, PLASMA }

    public final TurbineRotorSize size;
    public final OreDictMaterial material;
    public final long maxDurability;
    public final double baseEfficiency;
    public final int overflowMultiplier;

    private TurbineRotorStats(TurbineRotorSize size, OreDictMaterial material) {
        this.size = size;
        this.material = material;
        this.maxDurability = saturatedMultiply(
                Math.max(1L, material.mToolDurability),
                Math.round(100.0D * size.durabilityMultiplier));
        this.baseEfficiency = 0.5D + (0.5D + size.baseDamage + material.mToolQuality) * 0.1D;
        this.overflowMultiplier = 1 + Math.min(2, Math.max(0, material.mToolQuality) / 3);
    }

    public static TurbineRotorStats of(TurbineRotorSize size, OreDictMaterial material) {
        if (size == null) throw new IllegalArgumentException("Rotor size cannot be null");
        if (material == null) throw new IllegalArgumentException("Rotor material cannot be null");
        return new TurbineRotorStats(size, material);
    }

    public long optimalFlow(Fuel fuel) {
        return optimalFlow(fuel, 1.0D);
    }

    public long optimalFlow(Fuel fuel, double materialFuelMultiplier) {
        double flow = Math.max(0.0D, material.mToolSpeed) * 50.0D * size.speedMultiplier
                * Math.max(0.0D, materialFuelMultiplier);
        if (fuel == Fuel.PLASMA) flow *= 42.0D;
        return Math.max(0L, Math.round(flow));
    }

    public long optimalEUt(Fuel fuel) {
        return optimalEUt(fuel, 1.0D);
    }

    public long optimalEUt(Fuel fuel, double materialFuelMultiplier) {
        double factor = fuel == Fuel.STEAM ? 0.5D : 1.0D;
        return Math.max(0L, Math.round(optimalFlow(fuel, materialFuelMultiplier) * baseEfficiency * factor));
    }

    /** GT5 loose-fit efficiency; plasma intentionally keeps the unmodified base value. */
    public double looseEfficiency(Fuel fuel) {
        double looseBase = -0.2D + Math.round(baseEfficiency * 85.0D) * 0.01D;
        if (fuel == Fuel.STEAM) return looseBase * 0.90D;
        if (fuel == Fuel.GAS) return looseBase * 0.95D;
        return looseBase;
    }

    public long looseOptimalFlow(Fuel fuel) {
        double base = optimalFlow(fuel);
        double result;
        if (fuel == Fuel.STEAM) result = 3.0D * base * Math.pow(1.10D, (baseEfficiency - 0.8D) * 20.0D);
        else if (fuel == Fuel.GAS) result = 2.0D * base * Math.pow(1.05D, (baseEfficiency - 0.8D) * 20.0D);
        else result = 2.0D * base * Math.pow(1.03D, (baseEfficiency - 0.8D) * 20.0D);
        return Math.max(0L, Math.round(result));
    }

    /** Efficiency curve around optimal flow, matching the three GT5 turbine variants. */
    public double flowEfficiency(Fuel fuel, long actualFlow) {
        long optimal = optimalFlow(fuel);
        if (optimal <= 0L || actualFlow <= 0L) return 0.0D;
        if (actualFlow <= optimal) return Math.max(0.0D, 1.0D - (optimal - actualFlow) / (double) optimal);
        double overflow;
        if (fuel == Fuel.STEAM) overflow = overflowMultiplier + 1.0D;
        else if (fuel == Fuel.GAS) overflow = 3.0D * overflowMultiplier - 1.0D;
        else overflow = 3.0D * overflowMultiplier + 1.0D;
        return Math.max(0.0D, 1.0D - (actualFlow - optimal) / (optimal * overflow));
    }

    private static long saturatedMultiply(long value, long multiplier) {
        if (value <= 0L || multiplier <= 0L) return 1L;
        if (value > Long.MAX_VALUE / multiplier) return Long.MAX_VALUE;
        return value * multiplier;
    }
}
