package com.mod.htgt6.api.hitech6.fuelconfigs;

/** Exact hundredths-of-EU ledger. Efficiency is applied once, when fuel is consumed. */
public final class FluidFuelAccounting {
    public static final long MAX_RESERVE = 1L << 52;
    private FluidFuelAccounting() {}

    public static final class Step {
        public final int consumedMb;
        public final long generatedEu, reserveHundredths;
        private Step(int consumedMb, long generatedEu, long reserveHundredths) {
            this.consumedMb = consumedMb;
            this.generatedEu = generatedEu;
            this.reserveHundredths = reserveHundredths;
        }
    }

    public static long sanitize(long reserve) {
        return Math.max(0L, Math.min(MAX_RESERVE, reserve));
    }

    public static Step plan(long reserve, int availableMb, int euPerMb, int efficiencyPercent,
                            long voltage, long freeBuffer) {
        reserve = sanitize(reserve);
        long target = Math.max(0L, Math.min(Math.min(voltage, freeBuffer), MAX_RESERVE / 100L));
        int consumed = 0;
        long value = (long) Math.max(0, euPerMb) * Math.max(0, Math.min(10000, efficiencyPercent));
        long missing = target * 100L - reserve;
        if (target > 0 && missing > 0 && value > 0 && availableMb > 0) {
            consumed = (int) Math.min((long) availableMb, 1L + (missing - 1L) / value);
            reserve += consumed * value;
        }
        long generated = Math.min(target, reserve / 100L);
        return new Step(consumed, generated, reserve - generated * 100L);
    }
}
