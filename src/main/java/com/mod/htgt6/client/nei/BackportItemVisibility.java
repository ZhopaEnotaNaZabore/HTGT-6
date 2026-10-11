package com.mod.htgt6.client.nei;

import com.mod.htgt6.minecraft1202.Minecraft1202;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Technical;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** The creative catalog is authoritative; block-state metadata is not an item catalog. */
public final class BackportItemVisibility {
    private BackportItemVisibility() { }
    public static boolean isBackport(Item item) {
        String name = Item.itemRegistry.getNameForObject(item);
        return name != null && name.startsWith(Minecraft1202.MOD_ID + ":");
    }
    public static boolean isTechnical(Item item) {
        return item instanceof ItemBlockBackport120Technical || item.getCreativeTab() == null;
    }
    public static List<ItemStack> publicVariants(Item item) {
        List<ItemStack> variants = new ArrayList<ItemStack>();
        if (!isTechnical(item)) item.getSubItems(item, item.getCreativeTab(), variants);
        return variants;
    }
}
