package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

/** Stable registry identity: never silently allocate another ID and reinterpret saved enchantments. */
public final class CrossbowEnchantments140 {
    public static Enchantment quickCharge, multishot, piercing;
    private CrossbowEnchantments140() {}
    public static synchronized void init() {
        if (quickCharge != null && multishot != null && piercing != null) return;
        int quick = HTGT6Config.QUICK_CHARGE_ENCHANTMENT_ID;
        int multi = HTGT6Config.MULTISHOT_ENCHANTMENT_ID;
        int pierce = HTGT6Config.PIERCING_ENCHANTMENT_ID;
        if (quick == multi || quick == pierce || multi == pierce) throw new IllegalStateException("Crossbow enchantment IDs must be unique");
        validateId(quick, "QuickCharge");
        validateId(multi, "Multishot");
        validateId(pierce, "Piercing");
        quickCharge = new QuickCharge(quick);
        multishot = new Multishot(multi);
        piercing = new Piercing(pierce);
    }
    private static void validateId(int id, String name) {
        if (id < 0 || id >= Enchantment.enchantmentsList.length)
            throw new IllegalStateException(name + " enchantment ID outside registry: " + id);
        if (Enchantment.enchantmentsList[id] != null)
            throw new IllegalStateException(name + " enchantment ID " + id + " occupied by "
                    + Enchantment.enchantmentsList[id].getName()
                    + "; configure the same free village_enchantment_ids." + name + " on client and server");
    }
    private static final class Piercing extends Enchantment {
        Piercing(int id) { super(id,10,EnumEnchantmentType.bow); setName("piercing"); }
        @Override public int getMaxLevel() { return 4; }
        @Override public int getMinEnchantability(int level) { return 1 + (level - 1) * 10; }
        @Override public int getMaxEnchantability(int level) { return 50; }
        @Override public boolean canApply(ItemStack stack) { return stack != null && stack.getItem() instanceof ItemCrossbow; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return canApply(stack); }
        @Override public boolean canApplyTogether(Enchantment other) { return super.canApplyTogether(other) && other != multishot; }
    }
    private static final class Multishot extends Enchantment {
        @Override public boolean canApplyTogether(Enchantment other) { return super.canApplyTogether(other) && other != piercing; }
        Multishot(int id) { super(id,2,EnumEnchantmentType.bow); setName("multishot"); }
        @Override public int getMaxLevel() { return 1; }
        @Override public int getMinEnchantability(int level) { return 20; }
        @Override public int getMaxEnchantability(int level) { return 50; }
        @Override public boolean canApply(ItemStack stack) {
            return stack != null && stack.getItem() instanceof ItemCrossbow;
        }
        @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return canApply(stack); }
    }
    private static final class QuickCharge extends Enchantment {
        QuickCharge(int id) { super(id,5,EnumEnchantmentType.bow); setName("quick_charge"); }
        @Override public int getMaxLevel() { return 3; }
        @Override public int getMinEnchantability(int level) { return 12 + (level - 1) * 20; }
        @Override public int getMaxEnchantability(int level) { return 50; }
        @Override public boolean canApply(ItemStack stack) {
            return stack != null && stack.getItem() instanceof ItemCrossbow;
        }
        @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return canApply(stack); }
    }
}
