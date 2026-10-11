package com.mod.htgt6.minecraft1202.Module.Events.Exploration;

import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Small hooks used where Forge 1.7 exposes no event for the 1.11 rule. */
public final class ExplorationHooks112 {
    public static boolean canTakeArmorStack(Slot slot, EntityPlayer player) {
        ItemStack stack = slot == null ? null : slot.getStack();
        return player != null && (player.capabilities.isCreativeMode || stack == null
                || EnchantmentHelper.getEnchantmentLevel(
                ExplorationContent112.bindingCurse.effectId, stack) == 0);
    }

    public static boolean hasVanishing(ItemStack stack) {
        return stack != null && EnchantmentHelper.getEnchantmentLevel(
                ExplorationContent112.vanishingCurse.effectId, stack) > 0;
    }

    private ExplorationHooks112() { }
}
