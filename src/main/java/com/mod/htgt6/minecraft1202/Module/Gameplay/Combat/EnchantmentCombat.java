package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;

public class EnchantmentCombat extends Enchantment {
    private final int max;
    private final int minimum;
    private final int perLevel;
    private final int maximumSpan;
    private final boolean treasure;
    private final int incompatibleId;

    public EnchantmentCombat(int id, int weight, EnumEnchantmentType type, String name, int maxLevel,
            int minimum, int perLevel, int maximumSpan, boolean treasure, int incompatibleId) {
        super(id, weight, type);
        max = maxLevel;
        this.minimum = minimum;
        this.perLevel = perLevel;
        this.maximumSpan = maximumSpan;
        this.treasure = treasure;
        this.incompatibleId = incompatibleId;
        setName(name);
    }
    @Override public int getMaxLevel() { return max; }
    @Override public int getMinEnchantability(int level) { return minimum + (level - 1) * perLevel; }
    @Override public int getMaxEnchantability(int level) { return getMinEnchantability(level) + maximumSpan; }
    @Override public boolean canApplyAtEnchantingTable(net.minecraft.item.ItemStack stack) {
        return !treasure && super.canApplyAtEnchantingTable(stack);
    }
    @Override public boolean canApplyTogether(Enchantment other) {
        return other != null && other.effectId != incompatibleId && super.canApplyTogether(other);
    }
    public boolean isTreasure() { return treasure; }
}
