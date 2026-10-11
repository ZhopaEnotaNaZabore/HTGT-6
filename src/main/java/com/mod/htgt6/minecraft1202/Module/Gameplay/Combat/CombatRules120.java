package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

/** Pure combat formulas copied from Java 1.20.2 CombatRules. */
public final class CombatRules120 {
    public static float damageAfterAbsorb(float damage, float armor, float toughness) {
        float divisor = 2.0F + toughness / 4.0F;
        float effectiveArmor = clamp(armor - damage / divisor, armor * 0.2F, 20.0F);
        return damage * (1.0F - effectiveArmor / 25.0F);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
    private CombatRules120() { }
}
