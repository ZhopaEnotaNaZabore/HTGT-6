package com.mod.htgt6.common.item.htgt6.weapons;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;

public class HiTech6Weapons extends Item {

    public  static Item itemDildo;

 public static void weapons () {
     itemDildo = register(new Item(), "itemDildo");
     itemDildo.setTextureName(HTGT6.MOD_ID+":itemDildo");
     itemDildo.setUnlocalizedName("itemDildo");
     itemDildo.isDamageable();
     itemDildo.setMaxDamage(1000);

 }
    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
    }

