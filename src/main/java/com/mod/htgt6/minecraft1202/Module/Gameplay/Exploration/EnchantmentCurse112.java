package com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.item.ItemStack;

/** One-level treasure curse using the final 1.12 enchantment contract. */
public final class EnchantmentCurse112 extends Enchantment {
    public EnchantmentCurse112(int id, EnumEnchantmentType type, String name) {
        super(id, 1, type);
        setName(name);
    }

    @Override public int getMinEnchantability(int level) { return 25; }
    @Override public int getMaxEnchantability(int level) { return 50; }
    @Override public int getMaxLevel() { return 1; }
    @Override public String getTranslatedName(int level) {
        return EnumChatFormatting.RED + StatCollector.translateToLocal(getName());
    }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return false; }
}
