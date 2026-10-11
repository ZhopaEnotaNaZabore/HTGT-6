package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

/** Shared Minecraft 1.12 attack-speed and cooled-attack calculations. */
public final class AttackCooldown112 {
    private static final float TICKS_PER_SECOND = 20.0F;

    public static float getCooldownTicks(ItemStack stack) {
        return TICKS_PER_SECOND / getAttackSpeed(stack);
    }

    public static float getCooledStrength(long elapsedTicks, ItemStack stack) {
        return getCooledStrength((float) elapsedTicks, stack);
    }

    public static float getCooledStrength(float elapsedTicks, ItemStack stack) {
        if (elapsedTicks < 0.0F) return 1.0F;
        return Math.min(1.0F, elapsedTicks / getCooldownTicks(stack));
    }

    public static float getAttackSpeed(ItemStack stack) {
        if (stack == null) return 4.0F;
        Item item = stack.getItem();
        if (item instanceof ItemSword) return 1.6F;
        if (item instanceof ItemSpade) return 1.0F;
        if (item instanceof ItemPickaxe) return 1.2F;
        if (item instanceof ItemAxe) {
            String material = getToolMaterialName((ItemTool) item);
            if ("WOOD".equals(material) || "STONE".equals(material)) return 0.8F;
            if ("IRON".equals(material)) return 0.9F;
            return 1.0F; // Diamond and gold axes.
        }
        if (item instanceof ItemHoe) {
            String material = ((ItemHoe) item).getToolMaterialName();
            if ("STONE".equals(material)) return 2.0F;
            if ("IRON".equals(material)) return 3.0F;
            if ("EMERALD".equals(material)) return 4.0F;
            return 1.0F; // Wood and gold hoes.
        }
        return 4.0F;
    }

    /** Total main-hand damage shown by vanilla 1.12 (including the player's base point). */
    public static float getVanillaAttackDamage(ItemStack stack) {
        if (!isVanillaTool(stack)) return Float.NaN;
        Item item = stack.getItem();
        String material = getVanillaMaterialName(item);
        float bonus = getMaterialDamage(material);
        if (item instanceof ItemSword) return 4.0F + bonus;
        if (item instanceof ItemSpade) return 2.5F + bonus;
        if (item instanceof ItemPickaxe) return 2.0F + bonus;
        if (item instanceof ItemAxe) {
            if ("WOOD".equals(material) || "GOLD".equals(material)) return 7.0F;
            return 9.0F;
        }
        return 1.0F; // All vanilla hoes.
    }

    public static boolean isVanillaTool(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        if (!(item instanceof ItemSword) && !(item instanceof ItemTool) && !(item instanceof ItemHoe)) return false;
        Object name = Item.itemRegistry.getNameForObject(item);
        return name != null && name.toString().startsWith("minecraft:");
    }

    private static float getMaterialDamage(String material) {
        if ("STONE".equals(material)) return 1.0F;
        if ("IRON".equals(material)) return 2.0F;
        if ("EMERALD".equals(material)) return 3.0F;
        return 0.0F;
    }

    private static String getVanillaMaterialName(Item item) {
        Object registered = Item.itemRegistry.getNameForObject(item);
        String name = registered == null ? "" : registered.toString();
        int separator = name.indexOf(':');
        if (separator >= 0) name = name.substring(separator + 1);
        if (name.startsWith("wooden_")) return "WOOD";
        if (name.startsWith("stone_")) return "STONE";
        if (name.startsWith("iron_")) return "IRON";
        if (name.startsWith("diamond_")) return "EMERALD";
        if (name.startsWith("golden_")) return "GOLD";
        return "";
    }

    private static String getToolMaterialName(ItemTool tool) {
        Item.ToolMaterial material = tool.func_150913_i();
        return material == null ? "" : material.name();
    }

    private AttackCooldown112() { }
}
