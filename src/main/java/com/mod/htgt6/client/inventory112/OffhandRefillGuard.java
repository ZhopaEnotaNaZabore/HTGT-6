package com.mod.htgt6.client.inventory112;

/** Client-thread transaction guard. Inventory Tweaks still refreshes its baseline while suppressed. */
public final class OffhandRefillGuard {
    private static final int[] pending = new int[9];
    private static final boolean[] acknowledged = new boolean[9];
    public static void begin(int slot) { pending[slot]++; }
    public static void acknowledge(int slot) {
        if (slot < 0 || slot >= 9) return;
        pending[slot] = Math.max(0, pending[slot] - 1);
        acknowledged[slot] = true;
    }
    public static boolean suppress(int slot) {
        if (slot < 0 || slot >= 9) return false;
        boolean result = pending[slot] > 0 || acknowledged[slot];
        acknowledged[slot] = false;
        return result;
    }
    public static void reset() {
        java.util.Arrays.fill(pending, 0);
        java.util.Arrays.fill(acknowledged, false);
    }
    private OffhandRefillGuard() { }
}
