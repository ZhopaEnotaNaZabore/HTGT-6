package com.mod.htgt6.common.item.greg.technological.sieves;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class sieves extends Item {

    public static Item basicSieve;

    public static Item advSieve;

    public static void Sieves() {
        basicSieve = register(new Item(), "basicSieve");
        basicSieve.setUnlocalizedName("basicSieve");
        basicSieve.setTextureName(HTGT6.MOD_ID+":basicMesh");
        advSieve = register(new Item(), "advSieve");
        advSieve.setUnlocalizedName("advSieve");
        advSieve.setTextureName(HTGT6.MOD_ID+":advancedMesh");

    }
    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
}}
