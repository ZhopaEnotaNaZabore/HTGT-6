package com.mod.htgt6.common.item.IC2;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class miscIC2 extends Item {

    public static Item BatteryHull;
    public static Item BronzeBatterHull;

    public static void IC2miscItems() {
        BatteryHull = register(new Item(), "hullBattery");
        BatteryHull.setUnlocalizedName("HullBatteryIC2");
        BatteryHull.setTextureName(HTGT6.MOD_ID+":HullBatteryIC2");
        BronzeBatterHull = register(new Item(), "hullBattery2");
        BronzeBatterHull.setUnlocalizedName("HullBatteryIC2_2");
        BronzeBatterHull.setTextureName(HTGT6.MOD_ID+":HullBatteryIC2_2");

    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
}}
