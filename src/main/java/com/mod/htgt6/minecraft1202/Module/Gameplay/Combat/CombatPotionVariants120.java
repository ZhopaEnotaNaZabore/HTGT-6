package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;

/** The complete unique 1.7 potion-bit set used as the carrier for 1.9 tipped arrows and clouds. */
public final class CombatPotionVariants120 {
    private static int[] values;

    @SuppressWarnings("unchecked")
    public static synchronized int[] values() {
        if (values != null) return values.clone();
        Map<List<PotionEffect>, Integer> unique = new LinkedHashMap<List<PotionEffect>, Integer>();
        for (int base = 0; base <= 15; ++base) {
            for (int modifier : new int[]{0, 32, 64}) {
                int metadata = 8192 | base | modifier;
                List<PotionEffect> effects = PotionHelper.getPotionEffects(metadata, false);
                if (effects != null && !effects.isEmpty()) unique.put(effects, Integer.valueOf(metadata));
            }
        }
        values = new int[unique.size()];
        int index = 0;
        for (Integer metadata : unique.values()) values[index++] = metadata.intValue();
        return values.clone();
    }
    private CombatPotionVariants120() { }
}
