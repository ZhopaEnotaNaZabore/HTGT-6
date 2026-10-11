package com.mod.htgt6.client.render;

/** Normalized recipe-driven visual path; no world entities or client-side processing. */
public final class IndustrialCrusherCycle {
    private IndustrialCrusherCycle() { }
    public static int component(float progress, int count) {
        return Math.min(Math.max(0, count - 1), (int) (Math.max(0, progress) / .45F * Math.max(1, count)));
    }
    public static double inputZ(float progress, int count) {
        double phase = Math.min(.999999, Math.max(0, progress / .45D)) * Math.max(1, count);
        return .7 + (phase - Math.floor(phase)) * 5.5;
    }
    public static double outputZ(float progress) {
        return 8.7 + Math.max(0, Math.min(1, (progress - .65) / .35)) * 6.0;
    }
}
