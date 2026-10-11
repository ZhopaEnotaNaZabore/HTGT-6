package com.mod.htgt6.api.hitech6.Misc;

import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Single authoritative definition of HTGT6 machine tiers and transformer limits. */
public final class MachineTierSystem {
    public static final int LV = 1;
    public static final int MAX_TIER = 16;

    public static final long[] VOLTAGES = {
            32L, 128L, 512L, 2048L,
            8192L, 32768L, 131072L, 524288L,
            2097152L, 8388608L, 33554432L, 134217728L,
            536870912L, 2147483648L, 8589934592L, 34359738368L
    };

    private MachineTierSystem() { }

    public static long voltage(int tier) {
        int index = Math.max(0, Math.min(tier - 1, VOLTAGES.length - 1));
        return VOLTAGES[index];
    }

    public static int moduleTier(Item item) {
        if (item == Tmodules.TierModuleMV) return 2;
        if (item == Tmodules.TierModuleHV) return 3;
        if (item == Tmodules.TierModuleEV) return 4;
        if (item == Tmodules.TierModuleIV) return 5;
        if (item == Tmodules.TierModuleLuV) return 6;
        if (item == Tmodules.TierModuleZPM) return 7;
        if (item == Tmodules.TierModuleUV) return 8;
        if (item == Tmodules.TierModulePUV1) return 9;
        if (item == Tmodules.TierModuleUX) return 10;
        if (item == Tmodules.TierModuleOLV) return 11;
        if (item == Tmodules.TierModuleOMV) return 12;
        if (item == Tmodules.TierModuleOHV) return 13;
        if (item == Tmodules.TierModuleOEV) return 14;
        if (item == Tmodules.TierModuleOIV) return 15;
        if (item == Tmodules.TierModuleMAX) return 16;
        return 0;
    }

    /**
     * Transformer progression: MV-EV, EV-ZPM, UV-OLV, OMV-OIV, then MAX.
     * Every transformer also accepts every tier below its displayed range.
     */
    public static int transformerMaxTier(Item item) {
        if (item == Tmodules.LowLevelTransformer) return 4;
        if (item == Tmodules.MediumLevelTransformer) return 7;
        if (item == Tmodules.HighLevelTransformer) return 11;
        if (item == Tmodules.UltimateLevelTransformer) return 15;
        if (item == Tmodules.OmegaTransformer) return 16;
        return 0;
    }

    public static int transformerMaxTier(ItemStack stack) {
        return stack == null ? 0 : transformerMaxTier(stack.getItem());
    }

    public static int resolveMachineTier(ItemStack module, ItemStack transformer) {
        if (module == null || transformer == null) return LV;
        int requested = moduleTier(module.getItem());
        return requested > 0 && requested <= transformerMaxTier(transformer) ? requested : LV;
    }

    public static long overclockEUt(long baseEUt, int recipeTier, int machineTier) {
        long result = Math.max(1L, baseEUt);
        int difference = Math.max(0, machineTier - recipeTier);
        for (int i = 0; i < difference; i++) {
            if (result > Long.MAX_VALUE / 4L) return Long.MAX_VALUE;
            result *= 4L;
        }
        return result;
    }
}
