package com.mod.htgt6.common.item.htgt6.circuits;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class CP2 extends Item {

    public static Item itemWafer1;
    public static Item itemWafer2;
    public static Item itemWafer3;
    public static Item itemWafer4;
    public static Item itemWaferRAM;
    public static Item itemWaferCPU;
    public static Item itemWaferIC;
    public static Item itemRAM;
    public static Item itemCPU;
    public static Item itemIC;
    public static Item itemWaferNOR, itemNOR;
    public static Item itemWaferXNOR, itemXNOR;
    public static Item itemWaferNAND, itemNAND;
    public static Item itemWaferQbit, itemQbit;
    public static Item itemWaferNCPU, itemNCPU;
    public static Item itemWaferLPIC, itemLPIC;
    public static Item itemWaferPIC, itemPIC;
    public static Item itemWaferHPIC, itemHPIC;
    public static Item itemWaferUHPIC, itemUHPIC;

    public static void logisticsCP2 () {

        //REGISTER
        itemWafer1 = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWafer1");
        itemWafer2 = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWafer2");
        itemWafer3 = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWafer3");
        itemWafer4 = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWafer4");
        itemWaferRAM = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferRAM");
        itemRAM = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemRAM");
        itemWaferCPU = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferCPU");
        itemCPU = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemCPU");
        itemWaferIC = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferIC");
        itemIC = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemIC");

        // Append new components after the existing registrations; keep legacy names.
        itemWaferNOR = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferNOR");
        itemNOR = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemNOR");
        itemWaferXNOR = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferXNOR");
        itemXNOR = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemXNOR");
        itemWaferNAND = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferNAND");
        itemNAND = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemNAND");
        itemWaferQbit = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferQbit");
        itemQbit = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemQbit");
        itemWaferNCPU = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferNCPU");
        itemNCPU = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemNCPU");
        itemWaferLPIC = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferLPIC");
        itemLPIC = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemLPIC");
        itemWaferPIC = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferPIC");
        itemPIC = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemPIC");
        itemWaferHPIC = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferHPIC");
        itemHPIC = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemHPIC");
        itemWaferUHPIC = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemWaferUHPIC");
        itemUHPIC = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemUHPIC");



        //NAME
        itemWafer1.setUnlocalizedName("itemWafer1");
        itemWafer2.setUnlocalizedName("itemWafer2");
        itemWafer3.setUnlocalizedName("itemWafer3");
        itemWafer4.setUnlocalizedName("itemWafer4");
        itemWaferRAM.setUnlocalizedName("itemWaferRAM");
        itemRAM.setUnlocalizedName("itemRAM");
        itemWaferCPU.setUnlocalizedName("itemWaferCPU");
        itemCPU.setUnlocalizedName("itemCPU");
        itemWaferIC.setUnlocalizedName("itemWaferIC");
        itemIC.setUnlocalizedName("itemIC");

        //TEXTURE
        itemWafer1.setTextureName(HTGT6.MOD_ID+":itemWafer1");
        itemWafer2.setTextureName(HTGT6.MOD_ID+":itemWafer2");
        itemWafer3.setTextureName(HTGT6.MOD_ID+":itemWafer3");
        itemWafer4.setTextureName(HTGT6.MOD_ID+":itemWafer4");
        itemWaferRAM.setTextureName(HTGT6.MOD_ID+":itemWaferRAM");
        itemRAM.setTextureName(HTGT6.MOD_ID+":itemRAM");
        itemWaferCPU.setTextureName(HTGT6.MOD_ID+":itemWaferCPU");
        itemCPU.setTextureName(HTGT6.MOD_ID+":itemCPU");
        itemWaferIC.setTextureName(HTGT6.MOD_ID+":itemWaferIC");
        itemIC.setTextureName(HTGT6.MOD_ID+":itemIC");
    }

    public static Item register(Item item, String name) {
        item.setUnlocalizedName(name);
        item.setTextureName(HTGT6.MOD_ID + ":" + name);
        item.setCreativeTab(ModTab.INSTANCE);
        GameRegistry.registerItem(item, name);
        return item;
    }
}
