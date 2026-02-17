package com.mod.htgt6.common.item.htgt6.circuits;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class CP2 extends Item {

    public static Item itemWafer1;
    public static Item itemWafer2;
    public static Item itemWafer3;
    public static Item itemWafer4;


    public static void logisticsCP2 () {

        //REGISTER
        itemWafer1 = register( new Item(), "itemWafer1");
        itemWafer2 = register( new Item(), "itemWafer2");
        itemWafer3 = register( new Item(), "itemWafer3");
        itemWafer4 = register( new Item(), "itemWafer4");


        //NAME
        itemWafer1.setUnlocalizedName("itemWafer1");
        itemWafer2.setUnlocalizedName("itemWafer2");
        itemWafer3.setUnlocalizedName("itemWafer3");
        itemWafer4.setUnlocalizedName("itemWafer4");


        //TEXTURE
        itemWafer1.setTextureName(HTGT6.MOD_ID+":itemWafer1");
        itemWafer2.setTextureName(HTGT6.MOD_ID+":itemWafer2");
        itemWafer3.setTextureName(HTGT6.MOD_ID+":itemWafer3");
        itemWafer4.setTextureName(HTGT6.MOD_ID+":itemWafer4");
    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
}
