package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

/** The four 1.20.2 trident enchantment contracts adapted to 1.7 item slots. */
public final class EnchantmentAquaticTrident extends Enchantment {
    enum Kind { LOYALTY, IMPALING, RIPTIDE, CHANNELING }

    private final Kind kind;

    public EnchantmentAquaticTrident(int id, int weight, Kind kind) {
        super(id, weight, EnumEnchantmentType.weapon);
        this.kind = kind;
    }

    @Override public int getMinLevel() { return 1; }
    @Override public int getMaxLevel() {
        return kind == Kind.IMPALING ? 5 : kind == Kind.CHANNELING ? 1 : 3;
    }
    @Override public int getMinEnchantability(int level) {
        switch (kind) {
            case IMPALING: return 1 + (level - 1) * 8;
            case CHANNELING: return 25;
            case RIPTIDE: return 10 + level * 7;
            default: return 12 + (level - 1) * 7;
        }
    }
    @Override public int getMaxEnchantability(int level) {
        return kind == Kind.IMPALING ? getMinEnchantability(level) + 20 : 50;
    }
    @Override public boolean canApply(ItemStack stack) {
        return stack != null && stack.getItem() == OceanContent.trident;
    }
    @Override public boolean canApplyTogether(Enchantment other) {
        if (!super.canApplyTogether(other)) return false;
        if (kind == Kind.RIPTIDE) {
            return other != OceanContent.loyalty && other != OceanContent.channeling;
        }
        return other != OceanContent.riptide
                || kind != Kind.LOYALTY && kind != Kind.CHANNELING;
    }
}
