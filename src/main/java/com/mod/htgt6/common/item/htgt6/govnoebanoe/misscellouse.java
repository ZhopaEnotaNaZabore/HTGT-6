package com.mod.htgt6.common.item.htgt6.govnoebanoe;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class misscellouse extends Item {

    public static Item boronGlassDust;
    public static Item boronGlassPane;

    public static void GVN() {
        boronGlassDust = register(new Item(), "BoronGlassDust1");
        boronGlassDust.setTextureName(HTGT6.MOD_ID+":BoronGlassDust1");
        boronGlassDust.setUnlocalizedName("BoronGlassDust1");
        boronGlassPane = register(new Item(), "BoronGlassPane1");
        boronGlassPane.setTextureName(HTGT6.MOD_ID+":BoronGlassPane1");
        boronGlassPane.setUnlocalizedName("BoronGlassPane1");


    }
    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
}
