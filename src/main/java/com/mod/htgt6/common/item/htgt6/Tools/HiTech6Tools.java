package com.mod.htgt6.common.item.htgt6.Tools;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.item.Item;

public class HiTech6Tools extends Item {

    public static Item ThermometreNEW;


    public static void InitHiTech6Tools() {

        //reg
        ThermometreNEW = register(new ItemThermometreNEW(), "ThermometreNEW");

        //name
        ThermometreNEW.setUnlocalizedName("ThermometreNEW");
        //texture
        ThermometreNEW.setTextureName(HTGT6.MOD_ID+":ThermometreNEW");

        //...



    }
    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
}
