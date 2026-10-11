package com.mod.htgt6.common.ui;

import com.mod.htgt6.api.hitech6.fuelconfigs.FluidFuelAccounting;

public final class FluidFuelAccountingCheck {
    public static void main(String[] args) {
        for (int value : new int[] {1, 15, 22, 45, 320, 14893, 12000000, Integer.MAX_VALUE}) {
            for (int efficiency : new int[] {1, 33, 80, 100, 125, 175, 10000}) {
                for (int amount : new int[] {1, 2, 3, 17, 128000}) {
                    check(value, efficiency, amount);
                }
            }
        }
        FluidFuelAccounting.Step blocked = FluidFuelAccounting.plan(57, 3, 15, 100, 32, 0);
        require(blocked.consumedMb == 0 && blocked.generatedEu == 0 && blocked.reserveHundredths == 57, "full buffer");
        FluidFuelAccounting.Step tail = FluidFuelAccounting.plan(0, 3, 15, 100, 512, 512);
        require(tail.consumedMb == 3 && tail.generatedEu == 45, "last 3 mB");
        FluidFuelAccounting.Step rich = FluidFuelAccounting.plan(0, 1, 14893, 100, 32, 32);
        require(rich.consumedMb == 1 && rich.generatedEu == 32 && rich.reserveHundredths == 1486100, "rich fuel at LV");
        for (long voltage : new long[] {32, 128, 512, 34359738368L}) {
            int remaining = 1000;
            long reserve = 0, total = 0;
            for (int tick = 0; tick < 10000 && (remaining > 0 || reserve > 0); tick++) {
                FluidFuelAccounting.Step step = FluidFuelAccounting.plan(reserve, remaining, 200, 100, voltage, voltage);
                remaining -= step.consumedMb;
                reserve = step.reserveHundredths;
                total += step.generatedEu;
            }
            require(remaining == 0 && reserve == 0 && total == 200000, "1000 L = 200000 EU at every tier");
        }
        System.out.println("Fluid fuel conservation checks passed");
    }

    private static void check(int value, int efficiency, int amount) {
        long reserve = 0, generated = 0, credited = 0;
        int remaining = amount;
        // Vary tier, efficiency and buffer space; emulate save/reload of the exact ledger.
        for (int tick = 0; tick < 400 && (remaining > 0 || reserve >= 100); tick++) {
            long voltage = tick % 3 == 0 ? 32 : 34359738368L;
            long free = tick % 7 == 0 ? 0 : voltage;
            int percent = tick % 2 == 0 ? efficiency : 100;
            FluidFuelAccounting.Step step = FluidFuelAccounting.plan(reserve, remaining, value, percent, voltage, free);
            require(step.consumedMb >= 0 && step.consumedMb <= remaining, "fuel limit");
            require(step.generatedEu <= voltage && step.generatedEu <= free, "tick limit");
            credited += (long) step.consumedMb * value * percent;
            generated += step.generatedEu;
            remaining -= step.consumedMb;
            reserve = FluidFuelAccounting.sanitize(step.reserveHundredths);
            require(generated * 100L + reserve == credited, "conservation across tier change/reload");
        }
        // Drain the remaining reserve with a large budget, then verify no fuel remains stranded.
        for (int i = 0; i < 10000 && (remaining > 0 || reserve >= 100); i++) {
            FluidFuelAccounting.Step step = FluidFuelAccounting.plan(reserve, remaining, value, efficiency,
                    FluidFuelAccounting.MAX_RESERVE / 100L, FluidFuelAccounting.MAX_RESERVE / 100L);
            credited += (long) step.consumedMb * value * efficiency;
            remaining -= step.consumedMb;
            generated += step.generatedEu;
            reserve = step.reserveHundredths;
        }
        require(remaining == 0 && reserve < 100 && generated * 100L + reserve == credited, "final balance");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
