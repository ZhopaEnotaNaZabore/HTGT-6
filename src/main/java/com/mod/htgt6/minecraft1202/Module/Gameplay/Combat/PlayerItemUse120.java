package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** The 1.7 getters are client-only; their backing fields exist on both sides. */
public final class PlayerItemUse120 {
    private static final Field ITEM = ReflectionHelper.findField(EntityPlayer.class,
            "itemInUse", "field_71074_e");
    private static final Field COUNT = ReflectionHelper.findField(EntityPlayer.class,
            "itemInUseCount", "field_71072_f");

    public static ItemStack item(EntityPlayer player) {
        try { return (ItemStack) ITEM.get(player); }
        catch (IllegalAccessException failure) {
            throw new IllegalStateException("Cannot read player item-use state", failure);
        }
    }

    public static int duration(EntityPlayer player) {
        ItemStack stack = item(player);
        if (stack == null) return 0;
        try { return stack.getMaxItemUseDuration() - COUNT.getInt(player); }
        catch (IllegalAccessException failure) {
            throw new IllegalStateException("Cannot read player item-use timer", failure);
        }
    }

    private PlayerItemUse120() { }
}
